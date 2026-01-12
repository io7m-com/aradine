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

import com.io7m.aradine.api.ports.ARPortDescription;
import com.io7m.aradine.ensemble.internal.events.AREnsEventInstrumentType;

import java.util.List;
import java.util.Objects;

/**
 * A note port.
 */

public final class AREnsPortNote
  implements AREnsPortType
{
  private final AREnsInstrumentContext context;
  private final AREnsEventBuffer<AREnsEventInstrumentType> eventBuffer;
  private final ARPortDescription port;

  AREnsPortNote(
    final AREnsInstrumentContext inContext,
    final ARPortDescription inPort)
  {
    this.context =
      Objects.requireNonNull(inContext, "Context");
    this.port =
      Objects.requireNonNull(inPort, "Port");
    this.eventBuffer =
      new AREnsEventBuffer<>();
  }

  /**
   * Take all events from the port at the given frame index.
   *
   * @param frameIndex The frame index
   *
   * @return The events at the given time
   */

  public List<? extends AREnsEventInstrumentType>
  eventsTake(
    final int frameIndex)
  {
    return this.eventBuffer.eventsTake(frameIndex);
  }

  /**
   * Add an event to be processed in the next processing period.
   *
   * @param event The event
   */

  public void eventPut(
    final AREnsEventInstrumentType event)
  {
    this.eventBuffer.eventAdd(event);
  }

  @Override
  public ARPortDescription portDescription()
  {
    return this.port;
  }
}
