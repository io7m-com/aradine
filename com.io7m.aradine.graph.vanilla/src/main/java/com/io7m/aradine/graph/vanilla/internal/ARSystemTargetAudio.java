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
import com.io7m.aradine.graph.api.ARAudioGraphType;
import com.io7m.aradine.instrument.metadata.ARInstrumentPortID;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ARSystemTargetAudio
  extends ARGraphNode implements ARAudioGraphSystemTargetAudioType
{
  private final ARPortTargetAudio port;
  private final double[] sumBuffer;
  private final CopyOnWriteArrayList<ARAudioGraphConnectionType> incomingConnections;

  public ARSystemTargetAudio(
    final ARAudioGraphType inGraph,
    final UUID inId)
  {
    super(inId);

    this.port =
      new ARPortTargetAudio(
        inGraph,
        this,
        ARInstrumentPortID.of("target")
      );

    this.sumBuffer = new double[inGraph.settings().bufferSamples()];
    this.incomingConnections = new CopyOnWriteArrayList<>();
  }

  @Override
  public String toString()
  {
    return String.format("[ARSystemOutput %s]", this.id());
  }

  @Override
  public void updateSettings(
    final ARAudioGraphSettings newSettings)
  {
    this.port.updateSettings(newSettings);
  }

  @Override
  public void process(
    final ARAudioGraphProcessingType context)
  {
    Arrays.fill(this.sumBuffer, 0.0);

    for (int index = 0; index < this.incomingConnections.size(); ++index) {
      final var connection = this.incomingConnections.get(index);
      if (connection instanceof ARAudioGraphConnectionAudio) {
        final var audioConnection = (ARAudioGraphConnectionAudio) connection;
        final var source = audioConnection.sourcePort();
        source.read(data -> {
          for (int i = 0; i < data.length; ++i) {
            this.sumBuffer[i] += data[i];
          }
        });
      }
    }

    this.port.copyIn(this.sumBuffer);
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
  public void onIncomingConnectionsChanged(
    final Set<ARAudioGraphConnectionType> connections)
  {
    this.incomingConnections.clear();
    this.incomingConnections.addAll(connections);
  }
}
