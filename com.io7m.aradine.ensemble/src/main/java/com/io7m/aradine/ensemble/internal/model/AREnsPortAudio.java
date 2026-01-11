/*
 * Copyright © 2026 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

package com.io7m.aradine.ensemble.internal.model;

import com.io7m.aradine.api.ports.ARPortDescription;

import java.util.Objects;

/**
 * An audio port.
 */

public final class AREnsPortAudio
  implements AREnsPortInstanceType
{
  private final AREnsInstrumentContext context;
  private final ARPortDescription port;
  private volatile double[] buffer;

  AREnsPortAudio(
    final AREnsInstrumentContext inContext,
    final ARPortDescription inPort,
    final int initialBufferSize)
  {
    this.context =
      Objects.requireNonNull(inContext, "Context");
    this.port =
      Objects.requireNonNull(inPort, "Port");
    this.buffer =
      new double[initialBufferSize];
  }

  /**
   * Write a value at the given frame.
   *
   * @param frame The frame index
   * @param value The value
   */

  public void write(
    final int frame,
    final double value)
  {
    this.buffer[frame] = value;
  }

  /**
   * Read a value at the given frame.
   *
   * @param frame The frame index
   *
   * @return The value in the buffer
   */

  public double read(
    final int frame)
  {
    return this.buffer[frame];
  }

  /**
   * @return The port buffer
   */

  public double[] buffer()
  {
    return this.buffer;
  }

  /**
   * Set the buffer size.
   *
   * @param newSize The new size
   */

  public void setBufferSize(
    final int newSize)
  {
    this.buffer = new double[newSize];
  }

  @Override
  public ARPortDescription portDescription()
  {
    return this.port;
  }
}
