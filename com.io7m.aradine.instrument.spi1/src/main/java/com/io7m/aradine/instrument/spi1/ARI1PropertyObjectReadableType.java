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


package com.io7m.aradine.instrument.spi1;

/**
 * <p>An _attribute_ is an observable value to which one can subscribe and
 * receive updates each time the underlying value changes.</p>
 *
 * <p>Subscribing to an attribute creates a _subscription_, and this
 * subscription must be closed when no longer required. Subscriptions are strong
 * references, and so are capable of preventing attributes from being garbage
 * collected.</p>
 *
 * <p>Attributes are thread-safe and can be read from and written to by any
 * number of threads. Care should be taken to avoid updating an attribute from a
 * subscriber of that attribute; the result will be an infinite loop.</p>
 *
 * @param <T> The type of values
 */

public interface ARI1PropertyObjectReadableType<T>
{
  /**
   * @return The current value
   */

  T get();

  /**
   * Subscribe to the attribute. The given receiver function will be evaluated
   * once upon subscription, and then evaluated each time the attribute's value
   * changes. If the receiver function throws an exception, the subscription is
   * automatically closed.
   *
   * @param onUpdate The receiver function
   *
   * @return A subscription
   */

  ARI1SubscriptionType subscribe(
    ARI1PropertyObjectConsumerType<T> onUpdate);
}
