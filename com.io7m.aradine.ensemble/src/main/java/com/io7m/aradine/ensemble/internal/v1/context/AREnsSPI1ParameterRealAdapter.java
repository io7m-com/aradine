/*
 * Copyright © 2026 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

package com.io7m.aradine.ensemble.internal.v1.context;

import com.io7m.aradine.ensemble.internal.model.AREnsParameterReal;
import com.io7m.aradine.instrument.spi1.ARI1ParameterNumber;
import com.io7m.aradine.instrument.spi1.ARI1ParameterRealType;

import java.util.Objects;

/**
 * An SPI1 adapter for a core API real parameter. This wraps a core
 * parameter and exposes the API required by SPI1 instruments.
 */

public final class AREnsSPI1ParameterRealAdapter
  implements ARI1ParameterRealType
{
  private final AREnsSPI1InstrumentContextAdapter context;
  private final AREnsParameterReal parameter;
  private final ARI1ParameterNumber number;

  /**
   * Wrap a parameter.
   *
   * @param inContext   The context
   * @param inParameter The parameter
   */

  public AREnsSPI1ParameterRealAdapter(
    final AREnsSPI1InstrumentContextAdapter inContext,
    final AREnsParameterReal inParameter)
  {
    this.context =
      Objects.requireNonNull(inContext, "Context");
    this.parameter =
      Objects.requireNonNull(inParameter, "Port");
    this.number =
      new ARI1ParameterNumber(inParameter.description().number().value());
  }

  @Override
  public double valueMinimum()
  {
    return this.parameter.description().valueMinimum();
  }

  @Override
  public double valueMaximum()
  {
    return this.parameter.description().valueMaximum();
  }

  @Override
  public double value(
    final int frameIndex)
  {
    return this.parameter.value(frameIndex);
  }

  @Override
  public ARI1ParameterNumber id()
  {
    return this.number;
  }

  @Override
  public String label()
  {
    return this.parameter.description().label();
  }

  /**
   * Change the parameter value at the given time.
   *
   * @param time  The time
   * @param value The value
   */

  public void valueChange(
    final int time,
    final double value)
  {
    this.parameter.valueChange(time, value);
  }

  /**
   * Clear all value changes for this processing period.
   */

  public void valueChangesClear()
  {
    this.parameter.valueChangesClear();
  }
}
