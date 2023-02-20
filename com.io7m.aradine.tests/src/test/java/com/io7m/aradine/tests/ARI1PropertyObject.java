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

import com.io7m.aradine.instrument.spi1.ARI1PropertyObjectConsumerType;
import com.io7m.aradine.instrument.spi1.ARI1PropertyObjectType;
import com.io7m.aradine.instrument.spi1.ARI1SubscriptionType;
import com.io7m.jattribute.core.AttributeType;

public final class ARI1PropertyObject<T>
  implements ARI1PropertyObjectType<T>
{
  private final AttributeType<T> attribute;

  public ARI1PropertyObject(
    final AttributeType<T> inAttribute)
  {
    this.attribute = inAttribute;
  }

  @Override
  public T get()
  {
    return this.attribute.get();
  }

  @Override
  public ARI1SubscriptionType subscribe(
    final ARI1PropertyObjectConsumerType<T> onUpdate)
  {
    return new ARI1Subscription(
      this.attribute.subscribe(onUpdate::onUpdate)
    );
  }

  @Override
  public void set(
    final T value)
  {
    this.attribute.set(value);
  }
}
