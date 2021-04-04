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

package com.io7m.aradine.graph.vanilla.internal;

import com.io7m.aradine.graph.api.ARAudioGraphConnectionAudio;
import com.io7m.aradine.graph.api.ARAudioGraphConnectionType;
import com.io7m.aradine.graph.api.ARAudioGraphException;
import com.io7m.aradine.graph.api.ARAudioGraphPortSourceAudioType;
import com.io7m.aradine.graph.api.ARAudioGraphPortTargetAudioType;
import com.io7m.aradine.graph.api.ARAudioGraphProcessingType;
import com.io7m.aradine.graph.api.ARAudioGraphSettings;
import com.io7m.aradine.graph.api.ARAudioGraphSumAudioType;
import com.io7m.aradine.graph.vanilla.ARAudioGraph;
import com.io7m.aradine.instrument.metadata.ARInstrumentPortID;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ARSumAudio
  extends ARGraphNode implements ARAudioGraphSumAudioType
{
  private final ConcurrentHashMap<ARInstrumentPortID, ARPortTargetAudio> portTargets;
  private final CopyOnWriteArrayList<ARAudioGraphConnectionType> connections;
  private final ARPortSourceAudio portSource;
  private volatile double[] sum;

  public ARSumAudio(
    final ARAudioGraph graph,
    final UUID uuid)
  {
    super(graph, uuid);

    this.sum =
      new double[graph.settings().bufferSamples()];
    this.portSource =
      new ARPortSourceAudio(graph, this, ARInstrumentPortID.of("sum"));
    this.portTargets =
      new ConcurrentHashMap<>();
    this.connections =
      new CopyOnWriteArrayList<>();
  }

  @Override
  public String toString()
  {
    return String.format("[ARSumAudio %s]", this.id());
  }

  @Override
  public void settingsUpdate(
    final ARAudioGraphSettings newSettings)
  {
    this.sum = new double[newSettings.bufferSamples()];
  }

  @Override
  public void process(
    final ARAudioGraphProcessingType context)
  {
    final var sumBuffer = this.sum;
    Arrays.fill(sumBuffer, 0.0);

    for (int index = 0; index < this.connections.size(); ++index) {
      final var connection = this.connections.get(index);
      if (connection instanceof ARAudioGraphConnectionAudio) {
        final var audioConnection = (ARAudioGraphConnectionAudio) connection;
        audioConnection.sourcePort().read(data -> {
          for (int i = 0; i < data.length; ++i) {
            sumBuffer[i] += data[i];
          }
        });
      }
    }

    this.portSource.copyIn(sumBuffer);
  }

  @Override
  public void onIncomingConnectionsChanged(
    final Set<ARAudioGraphConnectionType> newConnections)
  {
    this.connections.clear();
    this.connections.addAll(newConnections);
  }

  @Override
  public ARAudioGraphPortTargetAudioType createPortTarget(
    final ARInstrumentPortID portID)
    throws ARAudioGraphException
  {
    Objects.requireNonNull(portID, "portID");

    final var newPort =
      new ARPortTargetAudio(this.graph(), this, portID);
    final var target =
      this.portTargets.putIfAbsent(portID, newPort);

    if (target == null) {
      return newPort;
    }

    throw new ARAudioGraphException(
      "errorPortAlreadyExists",
      this.graph().strings()
        .format("errorPortAlreadyExists", this.id(), portID.value())
    );
  }

  @Override
  public ARAudioGraphPortSourceAudioType port()
  {
    return this.portSource;
  }
}
