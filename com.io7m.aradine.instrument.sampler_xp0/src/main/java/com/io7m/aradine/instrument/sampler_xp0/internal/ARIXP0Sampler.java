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

package com.io7m.aradine.instrument.sampler_xp0.internal;

import com.io7m.aradine.instrument.spi1.ARI1ControlEventBufferType;
import com.io7m.aradine.instrument.spi1.ARI1ControlEventNoteOff;
import com.io7m.aradine.instrument.spi1.ARI1ControlEventNoteOn;
import com.io7m.aradine.instrument.spi1.ARI1ControlEventParameterSetInteger;
import com.io7m.aradine.instrument.spi1.ARI1ControlEventParameterSetReal;
import com.io7m.aradine.instrument.spi1.ARI1ControlEventParameterSetSampleMap;
import com.io7m.aradine.instrument.spi1.ARI1ControlEventPitchBend;
import com.io7m.aradine.instrument.spi1.ARI1ControlEventType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentServiceImplementationObjectsType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentServicesType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentType;
import com.io7m.aradine.instrument.spi1.ARI1IntMapMutableType;
import com.io7m.aradine.instrument.spi1.ARI1SampleMapType;

import java.net.URI;
import java.util.List;
import java.util.Objects;

/**
 * A polyphonic sampler.
 */

public final class ARIXP0Sampler
  implements ARI1InstrumentType
{
  private final ARI1IntMapMutableType<ARIXP0SampleState> samplesPlaying;
  private final Parameters parameters;
  private final Ports ports;
  private final double[] frameSum;
  private final ARI1ControlEventBufferType eventBuffer;
  private final double[] frame;
  private double pitchBend;
  private volatile ARI1SampleMapType sampleMap;

  /**
   * A polyphonic sampler.
   *
   * @param inObjects     A supplier of objects
   * @param inSampleMap   The initial sample map
   * @param inEventBuffer The event buffer
   * @param inParameters  The parameters
   * @param inPorts       The ports
   */

  public ARIXP0Sampler(
    final ARI1InstrumentServiceImplementationObjectsType inObjects,
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
      Objects.requireNonNull(inParameters, "parameters");
    this.ports =
      Objects.requireNonNull(inPorts, "ports");

    this.samplesPlaying =
      inObjects.createIntMap(128);

    this.frame = new double[2];
    this.frameSum = new double[2];
  }

  @Override
  public void process(
    final ARI1InstrumentServicesType context)
  {
    final var frames = context.statusCurrentBufferSize().get();
    for (int frameIndex = 0; frameIndex < frames; ++frameIndex) {
      this.processEventsForFrame(
        context,
        this.eventBuffer.eventsTake(frameIndex)
      );

      this.frame[0] = 0.0;
      this.frame[1] = 0.0;
      this.frameSum[0] = 0.0;
      this.frameSum[1] = 0.0;

      for (final var playing : this.samplesPlaying.values()) {
        playing.evaluate(this.pitchBend, this.frame);
        this.frameSum[0] += this.frame[0] * playing.velocity();
        this.frameSum[1] += this.frame[1] * playing.velocity();
      }

      this.ports.outputL0.write(frameIndex, this.frameSum[0]);
      this.ports.outputR1.write(frameIndex, this.frameSum[1]);
    }

    this.eventBuffer.eventsClear();
  }

  private void processEventsForFrame(
    final ARI1InstrumentServicesType context,
    final List<ARI1ControlEventType> events)
  {
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

    if (event instanceof ARI1ControlEventNoteOff eventNoteOff) {
      this.processEventNoteOff(eventNoteOff);
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

    if (event instanceof ARI1ControlEventParameterSetInteger eventSet) {
      this.processEventParameterSetInteger(context, eventSet);
      return;
    }

    if (event instanceof ARI1ControlEventParameterSetReal eventSet) {
      this.processEventParameterSetReal(context, eventSet);
      return;
    }

    context.eventUnhandled(event);
  }

  private void processEventParameterSetReal(
    final ARI1InstrumentServicesType context,
    final ARI1ControlEventParameterSetReal eventSet)
  {
    final var loopPointId = this.parameters.loopPoint1.id();
    if (Objects.equals(eventSet.parameter(), loopPointId)) {
      for (final var playing : this.samplesPlaying.values()) {
        playing.setLoopPoint(eventSet.value());
      }
      return;
    }
    context.eventUnhandled(eventSet);
  }

  private void processEventParameterSetInteger(
    final ARI1InstrumentServicesType context,
    final ARI1ControlEventParameterSetInteger eventSet)
  {
    final var pitchBendId = this.parameters.pitchBendRange2.id();
    if (Objects.equals(eventSet.parameter(), pitchBendId)) {
      for (final var playing : this.samplesPlaying.values()) {
        playing.setPitchBendRange((int) eventSet.value());
      }
      return;
    }
    context.eventUnhandled(eventSet);
  }

  private void processEventParameterSetSampleMap(
    final ARI1InstrumentServicesType context,
    final ARI1ControlEventParameterSetSampleMap eventSet)
  {
    final var sampleMapId = this.parameters.samples0.id();
    if (Objects.equals(eventSet.parameter(), sampleMapId)) {
      this.openSampleMap(context, eventSet.location());
      return;
    }
    context.eventUnhandled(eventSet);
  }

  private void processEventPitchBend(
    final ARI1ControlEventPitchBend eventPitchBend)
  {
    this.pitchBend = eventPitchBend.pitch();
  }

  private void processEventNoteOff(
    final ARI1ControlEventNoteOff eventNoteOff)
  {
    this.samplesPlaying.remove(eventNoteOff.note());
  }

  private void processEventNoteOn(
    final ARI1ControlEventNoteOn eventNoteOn)
  {
    final var noteIndex = eventNoteOn.note();
    this.samplesPlaying.put(
      noteIndex,
      new ARIXP0SampleState(
        this.sampleMap.forNote(noteIndex),
        eventNoteOn.velocity()
      )
    );
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
