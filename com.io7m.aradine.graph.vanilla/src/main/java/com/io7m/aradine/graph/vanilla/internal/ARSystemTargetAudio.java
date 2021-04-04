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
import com.io7m.aradine.graph.api.ARAudioGraphPortTargetAudioType;
import com.io7m.aradine.graph.api.ARAudioGraphProcessingType;
import com.io7m.aradine.graph.api.ARAudioGraphSettings;
import com.io7m.aradine.graph.api.ARAudioGraphSystemTargetAudioType;
import com.io7m.aradine.graph.vanilla.ARAudioGraph;
import com.io7m.aradine.instrument.metadata.ARInstrumentPortID;
import com.io7m.jaffirm.core.Preconditions;

import java.nio.FloatBuffer;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ARSystemTargetAudio
  extends ARGraphNode implements ARAudioGraphSystemTargetAudioType
{
  private final ARPortTargetAudio port;
  private final CopyOnWriteArrayList<ARAudioGraphConnectionType> incomingConnections;
  private volatile double[] buffer;

  public ARSystemTargetAudio(
    final ARAudioGraph inGraph,
    final UUID inId)
  {
    super(inGraph, inId);

    this.port =
      new ARPortTargetAudio(
        inGraph,
        this,
        ARInstrumentPortID.of("target")
      );

    this.buffer = new double[inGraph.settings().bufferSamples()];
    this.incomingConnections = new CopyOnWriteArrayList<>();
  }

  @Override
  public String toString()
  {
    return String.format("[ARSystemTargetAudio %s]", this.id());
  }

  @Override
  public void settingsUpdate(
    final ARAudioGraphSettings newSettings)
  {
    this.port.updateSettings(newSettings);
    this.buffer = new double[newSettings.bufferSamples()];
  }

  @Override
  public void process(
    final ARAudioGraphProcessingType context)
  {
    if (!this.incomingConnections.isEmpty()) {
      final var audioConnection =
        (ARAudioGraphConnectionAudio) this.incomingConnections.get(0);
      final var source =
        audioConnection.sourcePort();

      final var bufferRef = this.buffer;
      source.read(data -> {
        System.arraycopy(data, 0, bufferRef, 0, bufferRef.length);
      });
    }

    this.port.copyIn(this.buffer);
  }

  @Override
  public ARAudioGraphPortTargetAudioType port()
  {
    return this.port;
  }

  @Override
  public void copyOut(
    final FloatBuffer output)
  {
    this.port.copyOut(output);
  }

  @Override
  public void copyOut(
    final double[] output)
  {
    this.port.copyOut(output);
  }

  @Override
  public void onIncomingConnectionsChanged(
    final Set<ARAudioGraphConnectionType> connections)
  {
    Preconditions.checkPreconditionI(
      connections.size(),
      x -> x <= 1,
      x -> "Must have at most one connection"
    );

    this.incomingConnections.clear();
    this.incomingConnections.addAll(connections);
  }
}
