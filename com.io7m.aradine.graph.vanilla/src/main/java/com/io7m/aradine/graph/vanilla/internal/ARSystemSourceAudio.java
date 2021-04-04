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

import com.io7m.aradine.graph.api.ARAudioGraphConnectionType;
import com.io7m.aradine.graph.api.ARAudioGraphPortSourceAudioType;
import com.io7m.aradine.graph.api.ARAudioGraphProcessingType;
import com.io7m.aradine.graph.api.ARAudioGraphSettings;
import com.io7m.aradine.graph.api.ARAudioGraphSystemSourceAudioType;
import com.io7m.aradine.graph.vanilla.ARAudioGraph;
import com.io7m.aradine.instrument.metadata.ARInstrumentPortID;

import java.nio.FloatBuffer;
import java.util.Set;
import java.util.UUID;

public final class ARSystemSourceAudio
  extends ARGraphNode implements ARAudioGraphSystemSourceAudioType
{
  private final ARPortSourceAudio port;

  public ARSystemSourceAudio(
    final ARAudioGraph inGraph,
    final UUID inId)
  {
    super(inGraph, inId);

    this.port =
      new ARPortSourceAudio(
        inGraph,
        this,
        ARInstrumentPortID.of("source")
      );
  }

  @Override
  public String toString()
  {
    return String.format("[ARSystemSourceAudio %s]", this.id());
  }

  @Override
  public void settingsUpdate(
    final ARAudioGraphSettings newSettings)
  {
    this.port.updateSettings(newSettings);
  }

  @Override
  public void process(
    final ARAudioGraphProcessingType context)
  {

  }

  @Override
  public ARAudioGraphPortSourceAudioType port()
  {
    return this.port;
  }

  @Override
  public void copyIn(
    final FloatBuffer input)
  {
    this.port.copyIn(input);
  }

  @Override
  public void copyIn(
    final double[] input)
  {
    this.port.copyIn(input);
  }

  @Override
  public void onIncomingConnectionsChanged(
    final Set<ARAudioGraphConnectionType> connections)
  {

  }
}
