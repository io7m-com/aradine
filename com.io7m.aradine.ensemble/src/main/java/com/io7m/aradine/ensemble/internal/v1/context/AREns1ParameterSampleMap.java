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

import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionSampleMap;
import com.io7m.aradine.instrument.spi1.ARI1ParameterNumber;
import com.io7m.aradine.instrument.spi1.ARI1SampleMapID;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;

import java.util.Objects;

/**
 * A sample map parameter.
 */

public final class AREns1ParameterSampleMap
  implements AREns1ParameterSampleMapType
{
  private final AREns1InstrumentContext context;
  private final ARI1ParameterDescriptionSampleMap description;
  private final Int2ObjectRBTreeMap<ARI1SampleMapID> valueByTime;

  /**
   * The value of this parameter at the start of the processing period. This is
   * either the value upon which the last period ended, or the default value if
   * no value has ever been set.
   */

  private ARI1SampleMapID valueAtPeriodStart;

  /**
   * The time of the latest received change in the current period.
   */

  private int valueLatestTime;

  /**
   * The value that this parameter will have at the endMilliseconds of the processing
   * period, assuming that no more events show up at a later time.
   */

  private ARI1SampleMapID valueAtPeriodEnd;

  /**
   * A sample map parameter.
   *
   * @param inContext     The context
   * @param inDescription The description
   * @param valueDefault  The default value
   */

  public AREns1ParameterSampleMap(
    final AREns1InstrumentContext inContext,
    final ARI1ParameterDescriptionSampleMap inDescription,
    final ARI1SampleMapID valueDefault)
  {
    this.context =
      Objects.requireNonNull(inContext, "Context");
    this.description =
      Objects.requireNonNull(inDescription, "description");

    Objects.requireNonNull(valueDefault, "valueDefault");
    this.valueByTime = new Int2ObjectRBTreeMap<ARI1SampleMapID>();
    this.valueLatestTime = 0;
    this.valueAtPeriodEnd = valueDefault;
    this.valueAtPeriodStart = valueDefault;
  }

  @Override
  public void valueChangesClear()
  {
    this.valueByTime.clear();
    this.valueLatestTime = 0;
    this.valueAtPeriodStart = this.valueAtPeriodEnd;
  }

  @Override
  public void valueChange(
    final int time,
    final ARI1SampleMapID value)
  {
    this.valueByTime.put(time, value);
    if (time >= this.valueLatestTime) {
      this.valueLatestTime = time;
      this.valueAtPeriodEnd = value;
    }
  }

  @Override
  public ARI1ParameterNumber id()
  {
    return this.description.id();
  }

  @Override
  public String label()
  {
    return this.description.label();
  }

  @Override
  public ARI1SampleMapID value(
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
