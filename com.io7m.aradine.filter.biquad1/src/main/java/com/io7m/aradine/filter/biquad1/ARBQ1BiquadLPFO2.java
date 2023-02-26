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
 * <p>A simple biquad low-pass filter of order 2 (Two zeroes, two poles).</p>
 *
 * @see "https://www.earlevel.com/main/2012/11/26/biquad-c-source-code/"
 */

public final class ARBQ1BiquadLPFO2 implements ARBQ1BiquadWithQType
{
  private double a0;
  private double a1;
  private double a2;
  private double b1;
  private double b2;
  private double cutoff;
  private double previous_m0;
  private double previous_m1;
  private double q;

  /**
   * Create a new filter.
   */

  public ARBQ1BiquadLPFO2()
  {
    this.cutoff = 0.5;
    this.q = 0.70710678;
  }

  @Override
  public void setCutoff(
    final @ARNormalizedUnsigned double newCutoff)
  {
    this.cutoff = Math.min(0.5, Math.max(0.0, newCutoff));
    this.recalculateCoefficients();
  }

  @Override
  public void setQ(
    final double newQ)
  {
    this.q = newQ;
    this.recalculateCoefficients();
  }

  private void recalculateCoefficients()
  {
    final var k = StrictMath.tan(StrictMath.PI * this.cutoff);
    final var ks = k * k;
    final var norm = 1.0 / (1.0 + (k / this.q) + ks);

    this.a0 = ks * norm;
    this.a1 = 2.0 * this.a0;
    this.a2 = this.a0;
    this.b1 = 2.0 * (ks - 1.0) * norm;
    this.b2 = ((1.0 - (k / this.q)) + ks) * norm;
  }

  @Override
  public double processOneFrame(
    final double input)
  {
    final var out = (input * this.a0) + this.previous_m0;
    this.previous_m0 = ((input * this.a1) + this.previous_m1) - (this.b1 * out);
    this.previous_m1 = (input * this.a2) - (this.b2 * out);
    return out;
  }
}
