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

package com.io7m.aradine.filter.biquad1;

import com.io7m.aradine.annotations.ARNormalizedUnsigned;

import java.nio.DoubleBuffer;

/**
 * The type of biquad filters.
 */

public interface ARBQ1BiquadType
{
  /**
   * <p>Specify a new frequency cutoff value.</p>
   *
   * @param newCutoff The cutoff
   */

  void setCutoff(
    @ARNormalizedUnsigned double newCutoff);

  /**
   * Apply a filter to the given frame in the input buffer, writing the filtered
   * frame to the corresponding frame of the output buffer.
   *
   * @param frame  The frame index
   * @param input  The input buffer
   * @param output The output buffer
   */

  default void processOneFrameBuffers(
    final int frame,
    final DoubleBuffer input,
    final DoubleBuffer output)
  {
    output.put(frame, this.processOneFrame(input.get(frame)));
  }

  /**
   * Process a single input frame.
   *
   * @param input The input frame
   *
   * @return The output frame
   */

  double processOneFrame(
    double input);
}
