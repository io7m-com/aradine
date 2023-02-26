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
 * <p>This uses 4x oversampling to improve stability.</p>
 *
 * @see "https://www.earlevel.com/main/2003/03/02/the-digital-state-variable-filter/"
 */

public final class ARSV1Filter4x implements ARSV1FilterType
{
  private final ARSV1Filter filter;

  /**
   * Construct a new filter.
   */

  public ARSV1Filter4x()
  {
    this.filter = new ARSV1Filter();
  }

  @Override
  public void setCutoff(
    @ARNormalizedUnsigned final double newCutoff)
  {
    this.filter.setCutoff(newCutoff);
  }

  @Override
  public void setQ(final double newQ)
  {
    this.filter.setQ(newQ);
  }

  @Override
  public double cutoff()
  {
    return this.filter.cutoff();
  }

  @Override
  public double q()
  {
    return this.filter.q();
  }

  @Override
  public void reset()
  {
    this.filter.reset();
  }

  @Override
  public double lowPassOutput()
  {
    return this.filter.lowPassOutput();
  }

  @Override
  public double highPassOutput()
  {
    return this.filter.highPassOutput();
  }

  @Override
  public double bandPassOutput()
  {
    return this.filter.bandPassOutput();
  }

  @Override
  public double bandRejectOutput()
  {
    return this.filter.bandRejectOutput();
  }

  @Override
  public void processOneFrame(
    final double input)
  {
    this.filter.processOneFrame(input);
    this.filter.processOneFrame(input);
    this.filter.processOneFrame(input);
    this.filter.processOneFrame(input);
  }
}
