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

package com.io7m.aradine.instrument.sampler_m0.internal;

import com.io7m.aradine.instrument.spi1.ARI1ControlEventBufferType;
import com.io7m.aradine.instrument.spi1.ARI1ControlEventNoteOff;
import com.io7m.aradine.instrument.spi1.ARI1ControlEventNoteOn;
import com.io7m.aradine.instrument.spi1.ARI1ControlEventParameterSetSampleMap;
import com.io7m.aradine.instrument.spi1.ARI1ControlEventPitchBend;
import com.io7m.aradine.instrument.spi1.ARI1ControlEventType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentServicesType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentType;
import com.io7m.aradine.instrument.spi1.ARI1SampleMapType;

import java.net.URI;
import java.util.Objects;

/**
 * A monophonic sampler.
 */

public final class ARIM0Sampler
  implements ARI1InstrumentType
{
  private final ARI1ControlEventBufferType eventBuffer;
  private final Parameters parameters;
  private final Ports ports;
  private final double[] frame;
  private double pitchBend;
  private double velocity;
  private int notes;
  private volatile ARI1SampleMapType sampleMap;
  private volatile ARIM0SampleState samplePlaying;

  /**
   * A monophonic sampler.
   *
   * @param inSampleMap   The initial sample map
   * @param inEventBuffer The event buffer
   * @param inParameters The parameters
   * @param inPorts The ports
   */

  public ARIM0Sampler(
    final ARI1SampleMapType inSampleMap,
    final ARI1ControlEventBufferType inEventBuffer,
    final Parameters inParameters,
    final Ports inPorts)
  {
    this.sampleMap =
      Objects.requireNonNull(inSampleMap, "inSampleMap");
    this.eventBuffer =
      Objects.requireNonNull(inEventBuffer, "eventBuffer");
    this.parameters =
      Objects.requireNonNull(inParameters, "inParameters");
    this.ports =
      Objects.requireNonNull(inPorts, "inPorts");

    this.notes = 0;
    this.frame = new double[2];
    this.velocity = 1.0;
  }

  @Override
  public void process(
    final ARI1InstrumentServicesType context)
  {
    final var frames = context.statusCurrentBufferSize().get();
    for (int frameIndex = 0; frameIndex < frames; ++frameIndex) {
      this.processEventsForFrame(context, frameIndex);

      final var playing = this.samplePlaying;
      if (playing != null) {
        playing.evaluate(this.pitchBend, this.frame);
      } else {
        this.frame[0] = 0.0;
        this.frame[1] = 0.0;
      }

      this.frame[0] = this.frame[0] * this.velocity;
      this.frame[1] = this.frame[1] * this.velocity;

      this.ports.outputL0.write(frameIndex, this.frame[0]);
      this.ports.outputR1.write(frameIndex, this.frame[1]);
    }

    this.eventBuffer.eventsClear();
  }

  private void processEventsForFrame(
    final ARI1InstrumentServicesType context,
    final int frameIndex)
  {
    final var events = this.eventBuffer.eventsTake(frameIndex);
    for (final var event : events) {
      this.processEventForFrame(context, event);
    }
  }

  private void processEventForFrame(
    final ARI1InstrumentServicesType context,
    final ARI1ControlEventType event)
  {
    if (event instanceof ARI1ControlEventNoteOn eventNoteOn) {
      this.processEventNoteOn(eventNoteOn);
      return;
    }

    if (event instanceof ARI1ControlEventNoteOff) {
      this.processEventNoteOff();
      return;
    }

    if (event instanceof ARI1ControlEventPitchBend eventPitchBend) {
      this.processEventPitchBend(eventPitchBend);
      return;
    }

    if (event instanceof ARI1ControlEventParameterSetSampleMap eventSet) {
      this.processEventParameterSetSampleMap(context, eventSet);
      return;
    }

    context.eventUnhandled(event);
  }

  private void processEventParameterSetSampleMap(
    final ARI1InstrumentServicesType context,
    final ARI1ControlEventParameterSetSampleMap event)
  {
    if (Objects.equals(event.parameter(), this.parameters.samples0.id())) {
      this.openSampleMap(context, event.location());
      return;
    }
    context.eventUnhandled(event);
  }

  private void processEventPitchBend(
    final ARI1ControlEventPitchBend event)
  {
    this.pitchBend = event.pitch();
  }

  private void processEventNoteOff()
  {
    this.notes = Math.max(0, this.notes - 1);
    if (this.notes == 0) {
      this.samplePlaying = null;
    }
  }

  private void processEventNoteOn(
    final ARI1ControlEventNoteOn event)
  {
    this.velocity = event.velocity();
    this.samplePlaying =
      new ARIM0SampleState(
        this.sampleMap.forNote(event.note()),
        this.velocity
      );
    ++this.notes;
  }

  @Override
  public void receiveEvent(
    final ARI1InstrumentServicesType context,
    final ARI1ControlEventType event)
  {
    this.eventBuffer.eventAdd(event);
  }

  private void openSampleMap(
    final ARI1InstrumentServicesType context,
    final URI source)
  {
    context.sampleMapOpen(source)
      .whenComplete((samples, throwable) -> {
        if (samples != null) {
          this.sampleMap = samples;
        }
      });
  }
}
