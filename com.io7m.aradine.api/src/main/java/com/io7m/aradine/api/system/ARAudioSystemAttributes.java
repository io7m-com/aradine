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

package com.io7m.aradine.api.system;

import com.io7m.aradine.annotations.ARTimeFrames;
import com.io7m.aradine.annotations.ARTimeMilliseconds;
import com.io7m.jattribute.core.AttributeReadableType;
import com.io7m.jattribute.core.AttributeSubscriptionType;
import com.io7m.jattribute.core.AttributeType;
import com.io7m.jattribute.core.Attributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The audio system attributes implementation.
 */

public final class ARAudioSystemAttributes
  implements ARAudioSystemAttributesType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ARAudioSystemAttributes.class);

  private static final Attributes ATTRIBUTES =
    Attributes.create(throwable -> {
      LOG.error("Uncaught attribute exception: ", throwable);
    });

  private final AttributeType<Integer> sampleRate;
  private final AttributeType<Integer> bufferSize;
  private final AttributeSubscriptionType sampleRateSubscription;
  private double millisecondsPerFrame;

  /**
   * The audio system attributes implementation.
   */

  public ARAudioSystemAttributes()
  {
    this.sampleRate =
      ATTRIBUTES.withValue(48000);
    this.bufferSize =
      ATTRIBUTES.withValue(1024);

    this.millisecondsPerFrame =
      1.0 / (this.sampleRate.get().doubleValue() * 1000.0);

    this.sampleRateSubscription =
      this.sampleRate.subscribe((_, newRate) -> {
        this.millisecondsPerFrame =
          1.0 / (newRate.doubleValue() * 1000.0);
      });
  }

  /**
   * Set the buffer size.
   *
   * @param size The new size
   */

  public void setBufferSize(
    final int size)
  {
    this.bufferSize.set(
      Integer.valueOf(Math.clamp(size, 1, Integer.MAX_VALUE))
    );
  }

  /**
   * Set the sample rate.
   *
   * @param rate The new rate
   */

  public void setSampleRate(
    final int rate)
  {
    this.sampleRate.set(
      Integer.valueOf(Math.clamp(rate, 1, Integer.MAX_VALUE))
    );
  }

  @Override
  public AttributeReadableType<Integer> bufferSize()
  {
    return this.bufferSize;
  }

  @Override
  public AttributeReadableType<Integer> sampleRate()
  {
    return this.sampleRate;
  }

  @Override
  public @ARTimeMilliseconds double timeMillisecondsPerFrame()
  {
    return this.millisecondsPerFrame;
  }

  @Override
  public @ARTimeFrames long timeMillisecondsToFrames(
    @ARTimeMilliseconds final double milliseconds)
  {
    final var rate = this.sampleRate().get().doubleValue();
    return Math.round((rate * (milliseconds / 1000.0)));
  }
}
