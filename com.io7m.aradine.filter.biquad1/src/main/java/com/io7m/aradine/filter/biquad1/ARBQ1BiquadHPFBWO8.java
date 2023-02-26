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
 * <p>A biquad high-pass filter of order 8, implemented as two biquad filters in
 * series. Q values for each stages are chosen to give a Butterworth-like
 * response.</p>
 *
 * @see "https://www.earlevel.com/main/2012/11/26/biquad-c-source-code/"
 */

public final class ARBQ1BiquadHPFBWO8 implements ARBQ1BiquadType
{
  private final ARBQ1BiquadHPFO2 stage0;
  private final ARBQ1BiquadHPFO2 stage1;
  private final ARBQ1BiquadHPFO2 stage2;
  private final ARBQ1BiquadHPFO2 stage3;

  /**
   * Create a new filter.
   */

  public ARBQ1BiquadHPFBWO8()
  {
    this.stage0 =
      new ARBQ1BiquadHPFO2();
    this.stage1 =
      new ARBQ1BiquadHPFO2();
    this.stage2 =
      new ARBQ1BiquadHPFO2();
    this.stage3 =
      new ARBQ1BiquadHPFO2();

    final var qs = ARBQ1BiquadQs.butterworthStyleCascadedQValues(8);
    this.stage0.setQ(qs[0]);
    this.stage1.setQ(qs[1]);
    this.stage2.setQ(qs[2]);
    this.stage3.setQ(qs[3]);
  }

  @Override
  public void setCutoff(
    final @ARNormalizedUnsigned double newCutoff)
  {
    this.stage0.setCutoff(newCutoff);
    this.stage1.setCutoff(newCutoff);
    this.stage2.setCutoff(newCutoff);
    this.stage3.setCutoff(newCutoff);
  }

  @Override
  public double processOneFrame(
    final double input)
  {
    final var s0 =
      this.stage0.processOneFrame(input);
    final var s1 =
      this.stage1.processOneFrame(s0);
    final var s2 =
      this.stage2.processOneFrame(s1);

    return this.stage3.processOneFrame(s2);
  }
}
