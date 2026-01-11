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

import com.io7m.aradine.ensemble.internal.events.AREnsEventInstrumentType;
import com.io7m.aradine.ensemble.internal.events.AREnsEventNoteOff;
import com.io7m.aradine.ensemble.internal.events.AREnsEventNoteOn;
import com.io7m.aradine.ensemble.internal.events.AREnsEventNotePitchBend;
import com.io7m.aradine.ensemble.internal.events.AREnsEventNoteType;
import com.io7m.aradine.ensemble.internal.model.AREnsPortNote;
import com.io7m.aradine.instrument.spi1.ARI1EventNoteOff;
import com.io7m.aradine.instrument.spi1.ARI1EventNoteOn;
import com.io7m.aradine.instrument.spi1.ARI1EventNotePitchBend;
import com.io7m.aradine.instrument.spi1.ARI1EventNoteType;
import com.io7m.aradine.instrument.spi1.ARI1PortNumber;
import com.io7m.aradine.instrument.spi1.ARI1PortSourceNoteType;
import com.io7m.aradine.instrument.spi1.ARI1PortTargetNoteType;

import java.util.List;
import java.util.Objects;

/**
 * An SPI1 note port adapter. This wraps a core API note port and exposes
 * the API required by SPI1 instruments.
 */

public final class AREnsSPI1PortNoteAdapter
  implements ARI1PortSourceNoteType, ARI1PortTargetNoteType
{
  private final AREnsSPI1InstrumentContextAdapter context;
  private final AREnsPortNote port;
  private final ARI1PortNumber number;

  AREnsSPI1PortNoteAdapter(
    final AREnsSPI1InstrumentContextAdapter inContext,
    final AREnsPortNote inPort)
  {
    this.context =
      Objects.requireNonNull(inContext, "Context");
    this.port =
      Objects.requireNonNull(inPort, "Port");
    this.number =
      new ARI1PortNumber(inPort.portDescription().number().value());
  }

  private static ARI1EventNoteType convertEvent(
    final AREnsEventInstrumentType e)
  {
    return switch (e) {
      case final AREnsEventNoteType note -> {
        yield switch (note) {
          case final AREnsEventNoteOff off -> {
            yield new ARI1EventNoteOff(
              off.timeOffsetInFrames(),
              off.note(),
              off.velocity()
            );
          }
          case final AREnsEventNoteOn on -> {
            yield new ARI1EventNoteOn(
              on.timeOffsetInFrames(),
              on.note(),
              on.velocity()
            );
          }
          case final AREnsEventNotePitchBend pitchBend -> {
            yield new ARI1EventNotePitchBend(
              pitchBend.timeOffsetInFrames(),
              pitchBend.pitch()
            );
          }
        };
      }
    };
  }

  /**
   * Add an event to be processed in the next processing period.
   *
   * @param event The event
   */

  public void eventPut(
    final AREnsEventInstrumentType event)
  {
    this.port.eventPut(event);
  }

  @Override
  public List<? extends ARI1EventNoteType>
  eventsTake(
    final int frameIndex)
  {
    return this.port.eventsTake(frameIndex)
      .stream()
      .map(AREnsSPI1PortNoteAdapter::convertEvent)
      .toList();
  }

  @Override
  public ARI1PortNumber id()
  {
    return this.number;
  }
}
