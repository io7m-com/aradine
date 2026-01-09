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

import com.io7m.aradine.instrument.spi1.ARI1EventBufferType;
import com.io7m.aradine.instrument.spi1.ARI1EventNoteType;
import com.io7m.aradine.instrument.spi1.ARI1PortNumber;
import com.io7m.aradine.instrument.spi1.ARI1PortTargetNoteType;

import java.util.List;
import java.util.Objects;

/**
 * A target node port.
 */

public final class AREns1PortTargetNote
  implements ARI1PortTargetNoteType, AREns1PortType
{
  private final AREns1InstrumentContext context;
  private final ARI1PortNumber portNumber;
  private final ARI1EventBufferType<ARI1EventNoteType> eventBuffer;

  AREns1PortTargetNote(
    final AREns1InstrumentContext inContext,
    final ARI1PortNumber inPortNumber)
  {
    this.context =
      Objects.requireNonNull(inContext, "Context");
    this.portNumber =
      Objects.requireNonNull(inPortNumber, "PortNumber");
    this.eventBuffer =
      inContext.createEventBuffer();
  }

  @Override
  public ARI1PortNumber id()
  {
    return this.portNumber;
  }

  @Override
  public List<? extends ARI1EventNoteType>
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
    final ARI1EventNoteType event)
  {
    this.eventBuffer.eventAdd(event);
  }
}
