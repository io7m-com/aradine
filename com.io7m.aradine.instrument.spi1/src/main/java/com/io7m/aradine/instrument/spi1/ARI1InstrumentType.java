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

import org.osgi.annotation.versioning.ProviderType;

/**
 * The interface implemented by instruments.
 */

@ProviderType
public interface ARI1InstrumentType
{
  /**
   * Execute audio processing for one processing period.
   *
   * @param context The audio processing context
   */

  void process(
    ARI1InstrumentServicesType context);

  /**
   * Receive and buffer an event for processing during the next processing
   * period.
   *
   * @param context The audio processing context
   * @param event   The event
   */

  void receiveEvent(
    ARI1InstrumentServicesType context,
    ARI1ControlEventType event);
}
