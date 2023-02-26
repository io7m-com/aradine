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


package com.io7m.aradine.filter.statevar1;

import com.io7m.aradine.annotations.ARNormalizedUnsigned;

/**
 * <p>A variable state filter providing simultaneous
 * low/high/bandreject/bandpass outputs.</p>
 *
 * <p>This filter benefits from oversampling to improve stability; call
 * {@link  #processOneFrame(double)} multiple times with the same input sample,
 * and discard all but the last output.</p>
 *
 * @see "https://www.earlevel.com/main/2003/03/02/the-digital-state-variable-filter/"
 */

public final class ARSV1Filter implements ARSV1FilterType
{
  private double cutoff;
  private double q;
  private double delay1;
  private double delay2;
  private double lowOut;
  private double highOut;
  private double bpOut;
  private double brOut;

  /**
   * Construct a new filter.
   */

  public ARSV1Filter()
  {
    this.setCutoff(1.0);
    this.q = 1.0;
  }

  @Override
  public void setCutoff(
    final @ARNormalizedUnsigned double newCutoff)
  {
    this.cutoff = Math.min(1.0, Math.max(0.0, newCutoff));
  }

  @Override
  public void setQ(
    final double newQ)
  {
    this.q = newQ;
  }

  @Override
  public double cutoff()
  {
    return this.cutoff;
  }

  @Override
  public double q()
  {
    return this.q;
  }

  @Override
  public void reset()
  {
    this.delay1 = 0.0;
    this.delay2 = 0.0;
    this.lowOut = 0.0;
    this.highOut = 0.0;
    this.bpOut = 0.0;
    this.brOut = 0.0;
  }

  @Override
  public double lowPassOutput()
  {
    return this.lowOut;
  }

  @Override
  public double highPassOutput()
  {
    return this.highOut;
  }

  @Override
  public double bandPassOutput()
  {
    return this.bpOut;
  }

  @Override
  public double bandRejectOutput()
  {
    return this.brOut;
  }

  @Override
  public void processOneFrame(
    final double input)
  {
    final double tmpLowOut = this.delay2 + (this.cutoff * this.delay1);
    final double tmpHighOut = input - tmpLowOut - (this.q * this.delay1);
    final double tmpBpOut = (this.cutoff * tmpHighOut) + this.delay1;
    final double tmpBrOut = tmpHighOut + tmpLowOut;

    this.delay1 = tmpBpOut;
    this.delay2 = tmpLowOut;

    this.lowOut = tmpLowOut;
    this.highOut = tmpHighOut;
    this.bpOut = tmpBpOut;
    this.brOut = tmpBrOut;
  }
}
