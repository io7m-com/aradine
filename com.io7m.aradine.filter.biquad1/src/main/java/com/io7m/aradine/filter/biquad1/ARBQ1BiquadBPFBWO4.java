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

/**
 * <p>A biquad band-pass filter of order 4, implemented as two biquad filters in
 * series. Q values for each stages are chosen to give a Butterworth-like
 * response.</p>
 *
 * @see "https://www.earlevel.com/main/2012/11/26/biquad-c-source-code/"
 */

public final class ARBQ1BiquadBPFBWO4 implements ARBQ1BiquadType
{
  private final ARBQ1BiquadBPFO2 stage0;
  private final ARBQ1BiquadBPFO2 stage1;

  /**
   * Create a new filter.
   */

  public ARBQ1BiquadBPFBWO4()
  {
    this.stage0 =
      new ARBQ1BiquadBPFO2();
    this.stage1 =
      new ARBQ1BiquadBPFO2();

    final var qs = ARBQ1BiquadQs.butterworthStyleCascadedQValues(4);
    this.stage0.setQ(qs[0]);
    this.stage1.setQ(qs[1]);
  }

  @Override
  public void setCutoff(
    final @ARNormalizedUnsigned double newCutoff)
  {
    this.stage0.setCutoff(newCutoff);
    this.stage1.setCutoff(newCutoff);
  }

  @Override
  public double processOneFrame(
    final double input)
  {
    return this.stage1.processOneFrame(this.stage0.processOneFrame(input));
  }
}
