/*
 * Copyright © 2022 Mark Raynsford <code@io7m.com> https://www.io7m.com
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


package com.io7m.aradine.tests;

import com.io7m.aradine.instrument.spi1.ARI1PortId;
import com.io7m.aradine.instrument.spi1.ARI1PortOutputAudioType;

import java.nio.DoubleBuffer;
import java.util.Objects;

public final class ARI1PortOutputAudio
  implements ARI1PortOutputAudioType
{
  private DoubleBuffer outputBuffer;
  private final ARI1PortId id;

  public ARI1PortOutputAudio(
    final ARI1PortId inId,
    final int sizeInitial)
  {
    this.id =
      Objects.requireNonNull(inId, "inName");
    this.outputBuffer =
      DoubleBuffer.allocate(sizeInitial);
  }

  @Override
  public void write(
    final int frame,
    final double value)
  {
    this.outputBuffer.put(frame, value);
  }

  /**
   * @return The current output buffer
   */

  public DoubleBuffer buffer()
  {
    return this.outputBuffer;
  }

  @Override
  public ARI1PortId id()
  {
    return this.id;
  }

  /**
   * Set a new buffer size.
   *
   * @param newValue The new size
   */

  public void setBufferSize(
    final int newValue)
  {
    this.outputBuffer = DoubleBuffer.allocate(newValue);
  }
}
