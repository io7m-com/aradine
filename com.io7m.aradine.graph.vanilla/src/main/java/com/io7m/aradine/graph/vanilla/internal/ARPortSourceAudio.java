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
import com.io7m.aradine.graph.api.ARAudioGraphException;
import com.io7m.aradine.graph.api.ARAudioGraphNodeType;
import com.io7m.aradine.graph.api.ARAudioGraphPortSourceAudioType;
import com.io7m.aradine.graph.api.ARAudioGraphPortTargetAudioType;
import com.io7m.aradine.graph.api.ARAudioGraphSettings;
import com.io7m.aradine.graph.api.ARAudioGraphType;
import com.io7m.aradine.instrument.metadata.ARInstrumentPortID;

import java.nio.FloatBuffer;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public final class ARPortSourceAudio
  extends ARPortSource implements ARAudioGraphPortSourceAudioType
{
  private final ReentrantReadWriteLock bufferLock;
  private volatile double[] buffer;

  public ARPortSourceAudio(
    final ARAudioGraphType inGraph,
    final ARAudioGraphNodeType inOwner,
    final ARInstrumentPortID inId)
  {
    super(inGraph, inOwner, inId);
    this.buffer = new double[inGraph.settings().bufferSamples()];
    this.bufferLock = new ReentrantReadWriteLock();
  }

  @Override
  public ARAudioGraphConnectionAudio connect(
    final ARAudioGraphPortTargetAudioType target)
    throws ARAudioGraphException
  {
    return super.graph().connectAudio(this, target);
  }

  @Override
  public void disconnect(
    final ARAudioGraphPortTargetAudioType target)
    throws ARAudioGraphException
  {
    super.graph().disconnectAudio(this, target);
  }

  @Override
  public String toString()
  {
    return String.format(
      "[ARPortSourceAudio %s]",
      this.id().value()
    );
  }

  @Override
  public void read(
    final Consumer<double[]> receiver)
  {
    final var readLock = this.bufferLock.readLock();

    readLock.lock();
    try {
      receiver.accept(this.buffer);
    } finally {
      readLock.unlock();
    }
  }

  public void updateSettings(
    final ARAudioGraphSettings newSettings)
  {
    final var writeLock = this.bufferLock.writeLock();
    writeLock.lock();
    try {
      this.buffer = new double[newSettings.bufferSamples()];
    } finally {
      writeLock.unlock();
    }
  }

  public void copyIn(
    final FloatBuffer input)
  {
    final var writeLock =
      this.bufferLock.writeLock();

    writeLock.lock();
    try {
      for (int index = 0; index < this.buffer.length; ++index) {
        this.buffer[index] = input.get(index);
      }
    } finally {
      writeLock.unlock();
    }
  }

  public void copyIn(
    final double[] input)
  {
    final var writeLock =
      this.bufferLock.writeLock();

    writeLock.lock();
    try {
      System.arraycopy(input, 0, this.buffer, 0, this.buffer.length);
    } finally {
      writeLock.unlock();
    }
  }
}
