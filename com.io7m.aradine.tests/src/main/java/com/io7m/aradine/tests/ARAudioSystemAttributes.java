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

package com.io7m.aradine.tests;

import com.io7m.aradine.api.system.ARAudioSystemAttributesType;
import com.io7m.jattribute.core.AttributeReadableType;
import com.io7m.jattribute.core.AttributeType;
import com.io7m.jattribute.core.Attributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  public ARAudioSystemAttributes()
  {
    this.sampleRate =
      ATTRIBUTES.withValue(48000);
    this.bufferSize =
      ATTRIBUTES.withValue(1024);
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
}
