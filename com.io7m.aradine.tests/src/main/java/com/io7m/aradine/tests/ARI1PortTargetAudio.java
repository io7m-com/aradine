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

import com.io7m.aradine.instrument.spi1.ARI1PortNumber;
import com.io7m.aradine.instrument.spi1.ARI1PortTargetAudioType;

import java.nio.DoubleBuffer;
import java.util.Objects;

public final class ARI1PortTargetAudio
  implements ARI1PortTargetAudioType
{
  private DoubleBuffer inputBuffer;
  private final ARI1PortNumber id;

  public ARI1PortTargetAudio(
    final ARI1PortNumber inId,
    final int sizeInitial)
  {
    this.id =
      Objects.requireNonNull(inId, "inName");
    this.inputBuffer =
      DoubleBuffer.allocate(sizeInitial);
  }

  @Override
  public double read(
    final int frame)
  {
    return this.inputBuffer.get(frame);
  }

  /**
   * @return The current input buffer
   */

  public DoubleBuffer buffer()
  {
    return this.inputBuffer;
  }

  @Override
  public ARI1PortNumber id()
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
    this.inputBuffer = DoubleBuffer.allocate(newValue);
  }
}
