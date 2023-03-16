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


package com.io7m.aradine.instrument.spi1;

import com.io7m.aradine.annotations.ARTimeFrames;
import com.io7m.aradine.annotations.ARTimeMilliseconds;

/**
 * Time services.
 */

public interface ARI1InstrumentServiceTimeType
{
  /**
   * @return The number of milliseconds in a single frame at the current sample
   * rate
   */

  @ARTimeMilliseconds double timeMillisecondsPerFrame();

  /**
   * Convert the given duration in milliseconds to the nearest equivalent number
   * of frames at the current sample rate.
   *
   * @param milliseconds The millisecond duration
   *
   * @return The number of frames
   */

  @ARTimeFrames long timeMillisecondsToFrames(
    @ARTimeMilliseconds double milliseconds);

  /**
   * Convert the given frame count to a duration in milliseconds based on the
   * current sample rate.
   *
   * @param frames The frame count
   *
   * @return The duration of the frames in milliseconds
   */

  default @ARTimeMilliseconds double timeFramesToMilliseconds(
    final @ARTimeFrames long frames)
  {
    return (double) frames * this.timeMillisecondsPerFrame();
  }
}
