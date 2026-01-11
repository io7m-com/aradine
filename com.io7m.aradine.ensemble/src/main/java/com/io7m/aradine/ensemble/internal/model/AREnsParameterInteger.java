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

package com.io7m.aradine.ensemble.internal.model;

import com.io7m.aradine.annotations.ARTimeFrames;
import com.io7m.aradine.api.parameters.ARParameterDescriptionInteger;
import it.unimi.dsi.fastutil.ints.Int2LongRBTreeMap;

import java.util.Objects;

/**
 * An integer parameter.
 */

public final class AREnsParameterInteger
  implements AREnsParameterType
{
  private final AREnsInstrumentContext context;
  private final ARParameterDescriptionInteger description;
  private final Int2LongRBTreeMap valueByTime;

  /**
   * The value of this parameter at the start of the processing period. This is
   * either the value upon which the last period ended, or the default value if
   * no value has ever been set.
   */

  private long valueAtPeriodStart;

  /**
   * The time of the latest received change in the current period.
   */

  private int valueLatestTime;

  /**
   * The value that this parameter will have at the endMilliseconds of the processing
   * period, assuming that no more events show up at a later time.
   */

  private long valueAtPeriodEnd;

  /**
   * Construct a parameter.
   *
   * @param inContext     The instrument context
   * @param inDescription The parameter description
   */

  public AREnsParameterInteger(
    final AREnsInstrumentContext inContext,
    final ARParameterDescriptionInteger inDescription)
  {
    this.context =
      Objects.requireNonNull(inContext, "Context");
    this.description =
      Objects.requireNonNull(inDescription, "description");

    this.valueByTime = new Int2LongRBTreeMap();
    this.valueLatestTime = 0;
    this.valueAtPeriodEnd = this.description.valueDefault();
    this.valueAtPeriodStart = this.description.valueDefault();
  }

  @Override
  public ARParameterDescriptionInteger description()
  {
    return this.description;
  }

  @Override
  public void valueChangesClear()
  {
    this.valueByTime.clear();
    this.valueLatestTime = 0;
    this.valueAtPeriodStart = this.valueAtPeriodEnd;
  }

  /**
   * Change the parameter value at the given time.
   *
   * @param time  The time
   * @param value The value
   */

  public void valueChange(
    @ARTimeFrames final int time,
    final long value)
  {
    this.valueByTime.put(time, value);
    if (time >= this.valueLatestTime) {
      this.valueLatestTime = time;
      this.valueAtPeriodEnd = value;
    }
  }

  /**
   * @param frameIndex The frame index
   *
   * @return The parameter value at the given time
   */

  public long value(
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
