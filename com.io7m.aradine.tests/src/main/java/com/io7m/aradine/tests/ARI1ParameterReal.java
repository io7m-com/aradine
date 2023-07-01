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

import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionRealType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterId;
import com.io7m.aradine.instrument.spi1.ARI1ParameterRealType;
import it.unimi.dsi.fastutil.ints.Int2DoubleRBTreeMap;

import java.util.Objects;

public final class ARI1ParameterReal
  implements ARI1ParameterRealType
{
  private final Int2DoubleRBTreeMap valueByTime;
  private final ARI1ParameterDescriptionRealType description;

  /**
   * The value of this parameter at the start of the processing period. This is
   * either the value upon which the last period ended, or the default value if
   * no value has ever been set.
   */

  private double valueAtPeriodStart;

  /**
   * The time of the latest received change in the current period.
   */

  private int valueLatestTime;

  /**
   * The value that this parameter will have at the endMilliseconds of the processing
   * period, assuming that no more events show up at a later time.
   */

  private double valueAtPeriodEnd;

  public ARI1ParameterReal(
    final ARI1ParameterDescriptionRealType inDescription)
  {
    this.description =
      Objects.requireNonNull(inDescription, "description");

    this.valueByTime = new Int2DoubleRBTreeMap();
    this.valueLatestTime = 0;
    this.valueAtPeriodEnd = this.description.valueDefault();
    this.valueAtPeriodStart = this.description.valueDefault();
  }

  public void valueChangesClear()
  {
    this.valueByTime.clear();
    this.valueLatestTime = 0;
    this.valueAtPeriodStart = this.valueAtPeriodEnd;
  }

  public void valueChange(
    final int time,
    final double value)
  {
    this.valueByTime.put(time, value);
    if (time >= this.valueLatestTime) {
      this.valueLatestTime = time;
      this.valueAtPeriodEnd = value;
    }
  }

  @Override
  public ARI1ParameterId id()
  {
    return this.description.id();
  }

  @Override
  public String label()
  {
    return this.description.label();
  }

  @Override
  public double valueMinimum()
  {
    return this.description.valueMinimum();
  }

  @Override
  public double valueMaximum()
  {
    return this.description.valueMaximum();
  }

  @Override
  public double value(
    final int frameIndex)
  {
    /*
     * Get the most recent events that occurred either before or exactly
     * on the current time.
     */

    final var relevantEvents =
      this.valueByTime.headMap(frameIndex + 1);

    /*
     * If there isn't a relevant event, then return the most recent
     * value (most likely set in the previous processing period).
     */

    if (relevantEvents.isEmpty()) {
      return this.valueAtPeriodStart;
    }

    /*
     * Return the value of the most recent change event.
     */

    return relevantEvents.get(relevantEvents.lastIntKey());
  }
}
