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

/**
 * An entry in a sample map.
 */

public interface ARI1SampleMapEntryType
{
  /**
   * @return The length of the sample in frames
   */

  long frames();

  /**
   * @return The number of channels in the sample
   */

  int channels();

  /**
   * @return The base playback rate for the sample
   */

  double playbackRate();

  /**
   * Evaluate the sample at frame index {@code frameIndex} and velocity
   * {@code velocity}. The output frame {@code output} must be at least as long
   * as {@link #channels()}.
   *
   * @param frameIndex The frame index
   * @param velocity   The velocity
   * @param output     The output frame
   */

  void evaluate(
    long frameIndex,
    double velocity,
    double[] output
  );
}
