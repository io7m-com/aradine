/*
 * Copyright © 2021 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.aradine.graph.vanilla;

import com.io7m.aradine.graph.api.ARAudioGraphConnectionAudio;
import com.io7m.aradine.graph.api.ARAudioGraphConnectionType;
import com.io7m.aradine.graph.api.ARAudioGraphListenerType;
import com.io7m.aradine.graph.api.ARAudioGraphLoopbackPair;
import com.io7m.aradine.graph.api.ARAudioGraphPortSourceAudioType;
import com.io7m.aradine.graph.api.ARAudioGraphPortTargetAudioType;
import com.io7m.aradine.graph.api.ARAudioGraphProcessingType;
import com.io7m.aradine.graph.api.ARAudioGraphSettings;
import com.io7m.aradine.graph.api.ARAudioGraphStringsType;
import com.io7m.aradine.graph.api.ARAudioGraphSystemSourceAudioType;
import com.io7m.aradine.graph.api.ARAudioGraphSystemTargetAudioType;
import com.io7m.aradine.graph.api.ARAudioGraphType;
import com.io7m.aradine.graph.vanilla.internal.ARGraphNode;
import com.io7m.aradine.graph.vanilla.internal.ARObjectMap;
import com.io7m.aradine.graph.vanilla.internal.ARSystemSourceAudio;
import com.io7m.aradine.graph.vanilla.internal.ARSystemTargetAudio;
import com.io7m.aradine.services.api.ARServiceDirectoryType;
import com.io7m.junreachable.UnimplementedCodeException;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class ARAudioGraph implements ARAudioGraphType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ARAudioGraph.class);

  private final CopyOnWriteArrayList<ARAudioGraphListenerType> listeners;
  private final DirectedAcyclicGraph<ARGraphNode, ARAudioGraphConnectionType> graphWorking;
  private final ARObjectMap objects;
  private volatile ARAudioGraphSettings settings;
  private volatile DirectedAcyclicGraph<ARGraphNode, ARAudioGraphConnectionType> graphCurrent;

  private ARAudioGraph(
    final ARAudioGraphStringsType strings,
    final ARAudioGraphSettings inSettings)
  {
    this.objects =
      new ARObjectMap(strings);
    this.settings =
      Objects.requireNonNull(inSettings, "settings");
    this.graphWorking =
      new DirectedAcyclicGraph<>(ARAudioGraphConnectionType.class);
    this.graphCurrent =
      new DirectedAcyclicGraph<>(ARAudioGraphConnectionType.class);
    this.listeners =
      new CopyOnWriteArrayList<>();
  }

  public static ARAudioGraphType create(
    final ARServiceDirectoryType services,
    final ARAudioGraphSettings settings)
  {
    Objects.requireNonNull(services, "services");
    Objects.requireNonNull(settings, "settings");

    return new ARAudioGraph(
      services.requireService(ARAudioGraphStringsType.class),
      settings
    );
  }

  @Override
  public ARAudioGraphSettings settings()
  {
    return this.settings;
  }

  @Override
  public void updateSettings(
    final ARAudioGraphSettings newSettings)
  {
    this.settings = Objects.requireNonNull(newSettings, "newSettings");
  }

  @Override
  public ARAudioGraphSystemSourceAudioType createSystemSourceAudio()
  {
    return this.objects.withFresh(this::createSystemInputActual);
  }

  @Override
  public ARAudioGraphSystemSourceAudioType createSystemSourceAudio(
    final UUID id)
  {
    Objects.requireNonNull(id, "id");
    return this.objects.withSpecific(id, this::createSystemInputActual);
  }

  @Override
  public ARAudioGraphSystemTargetAudioType createSystemTargetAudio()
  {
    return this.objects.withFresh(this::createSystemOutputActual);
  }

  @Override
  public ARAudioGraphSystemTargetAudioType createSystemTargetAudio(
    final UUID id)
  {
    Objects.requireNonNull(id, "id");
    return this.objects.withSpecific(id, this::createSystemOutputActual);
  }

  @Override
  public ARAudioGraphLoopbackPair createLoopbackPair(
    final UUID source,
    final UUID target)
  {
    Objects.requireNonNull(source, "input");
    Objects.requireNonNull(target, "output");

    throw new UnimplementedCodeException();
  }

  @Override
  public ARAudioGraphLoopbackPair createLoopbackPair()
  {
    throw new UnimplementedCodeException();
  }

  @Override
  public void addListener(
    final ARAudioGraphListenerType listener)
  {
    this.listeners.add(Objects.requireNonNull(listener, "listener"));
  }

  @Override
  public void removeListener(
    final ARAudioGraphListenerType listener)
  {
    this.listeners.remove(Objects.requireNonNull(listener, "listener"));
  }

  @Override
  public ARAudioGraphConnectionAudio connectAudio(
    final ARAudioGraphPortSourceAudioType source,
    final ARAudioGraphPortTargetAudioType target)
  {
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(target, "target");

    final var sourceVertex = (ARGraphNode) source.owner();
    final var targetVertex = (ARGraphNode) target.owner();

    final var connection =
      ARAudioGraphConnectionAudio.builder()
        .setSource(sourceVertex)
        .setSourcePort(source)
        .setTarget(targetVertex)
        .setTargetPort(target)
        .build();

    this.graphWorking.addEdge(sourceVertex, targetVertex, connection);

    for (final var listener : this.listeners) {
      try {
        listener.onConnect(connection);
      } catch (final Exception exception) {
        LOG.error("listener raised exception: ", exception);
      }
    }

    targetVertex.onIncomingConnectionsChanged(
      this.graphWorking.incomingEdgesOf(targetVertex)
    );

    this.graphCurrent = (DirectedAcyclicGraph<ARGraphNode, ARAudioGraphConnectionType>) this.graphWorking.clone();
    return connection;
  }

  @Override
  public void execute(
    final Consumer<ARAudioGraphProcessingType> process)
  {
    Objects.requireNonNull(process, "process");
    process.accept(new Context(this, this.graphCurrent));
  }

  private ARAudioGraphSystemSourceAudioType createSystemInputActual(
    final UUID uuid)
  {
    final var node = new ARSystemSourceAudio(this, uuid);
    this.graphWorking.addVertex(node);
    this.graphCurrent = (DirectedAcyclicGraph<ARGraphNode, ARAudioGraphConnectionType>) this.graphWorking.clone();
    return node;
  }

  private ARAudioGraphSystemTargetAudioType createSystemOutputActual(
    final UUID uuid)
  {
    final var node = new ARSystemTargetAudio(this, uuid);
    this.graphWorking.addVertex(node);
    this.graphCurrent = (DirectedAcyclicGraph<ARGraphNode, ARAudioGraphConnectionType>) this.graphWorking.clone();
    return node;
  }

  private static final class Context implements ARAudioGraphProcessingType
  {
    private final DirectedAcyclicGraph<ARGraphNode, ARAudioGraphConnectionType> graph;
    private final ARAudioGraph audioGraph;

    private Context(
      final ARAudioGraph inAudioGraph,
      final DirectedAcyclicGraph<ARGraphNode, ARAudioGraphConnectionType> inGraph)
    {
      this.audioGraph =
        Objects.requireNonNull(inAudioGraph, "audioGraph");
      this.graph =
        Objects.requireNonNull(inGraph, "graph");
    }

    @Override
    public void process()
    {
      final var iter = new TopologicalOrderIterator<>(this.graph);
      while (iter.hasNext()) {
        final var node = iter.next();
        final var listeners = this.audioGraph.listeners;
        if (!listeners.isEmpty()) {
          for (final var listener : this.audioGraph.listeners) {
            try {
              listener.onProcess(node);
            } catch (final Exception exception) {
              LOG.error("listener raised exception: ", exception);
            }
          }
        }
        node.process(this);
      }
    }
  }
}
