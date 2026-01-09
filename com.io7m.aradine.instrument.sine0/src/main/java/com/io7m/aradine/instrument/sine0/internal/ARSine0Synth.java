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

package com.io7m.aradine.instrument.sine0.internal;

import com.io7m.aradine.instrument.spi1.ARI1EventBufferType;
import com.io7m.aradine.instrument.spi1.ARI1EventConfigurationBufferSizeChanged;
import com.io7m.aradine.instrument.spi1.ARI1EventConfigurationParameterChanged;
import com.io7m.aradine.instrument.spi1.ARI1EventConfigurationSampleRateChanged;
import com.io7m.aradine.instrument.spi1.ARI1EventConfigurationType;
import com.io7m.aradine.instrument.spi1.ARI1EventNoteOff;
import com.io7m.aradine.instrument.spi1.ARI1EventNoteOn;
import com.io7m.aradine.instrument.spi1.ARI1EventNotePitchBend;
import com.io7m.aradine.instrument.spi1.ARI1EventNoteType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentContextType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentType;

import java.util.Objects;

/**
 * A monophonic synth.
 */

public final class ARSine0Synth
  implements ARI1InstrumentType
{
  private final ARI1EventBufferType<ARI1EventConfigurationType> eventBuffer;
  private final Parameters parameters;
  private final Ports ports;
  private final double[] frame;
  private int notes;
  private double velocity;
  private double notePhase;
  private double notePhaseIncrement;

  /**
   * A monophonic synth.
   *
   * @param inEventBuffer The event buffer
   * @param inParameters  The parameters
   * @param inPorts       The ports
   */

  public ARSine0Synth(
    final ARI1EventBufferType<ARI1EventConfigurationType> inEventBuffer,
    final Parameters inParameters,
    final Ports inPorts)
  {
    this.eventBuffer =
      Objects.requireNonNull(inEventBuffer, "EventBuffer");
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
    final ARI1InstrumentContextType context)
  {
    final var frames = context.statusCurrentBufferSize();
    for (int frameIndex = 0; frameIndex < frames; ++frameIndex) {
      this.processEventsForFrame(context, frameIndex);

      if (this.notes > 0) {
        this.notePhase += this.notePhaseIncrement;
        if (this.notePhase > Math.PI * 2.0) {
          this.notePhase -= Math.PI * 2.0;
        }

        this.frame[0] =
          StrictMath.sin(this.notePhase) * this.velocity;
        this.frame[1] =
          StrictMath.sin(this.notePhase) * this.velocity;
      } else {
        this.frame[0] = 0.0;
        this.frame[1] = 0.0;
      }

      this.ports.outputL0.write(frameIndex, this.frame[0]);
      this.ports.outputR1.write(frameIndex, this.frame[1]);
    }

    this.eventBuffer.eventsClear();
  }

  @Override
  public void receiveEvent(
    final ARI1InstrumentContextType context,
    final ARI1EventConfigurationType event)
  {
    this.eventBuffer.eventAdd(event);
  }

  private void processEventsForFrame(
    final ARI1InstrumentContextType context,
    final int frameIndex)
  {
    final var events = this.eventBuffer.eventsTake(frameIndex);
    for (final var event : events) {
      this.processEventConfigurationForFrame(context, event);
    }

    final var noteEvents = this.ports.noteInput2.eventsTake(frameIndex);
    for (final var event : noteEvents) {
      this.processEventNoteForFrame(context, event);
    }
  }

  private void processEventNoteForFrame(
    final ARI1InstrumentContextType context,
    final ARI1EventNoteType event)
  {
    switch (event) {
      case final ARI1EventNoteOn eventNoteOn -> {
        this.processEventNoteOn(context, eventNoteOn);
        return;
      }
      case ARI1EventNoteOff _ -> {
        this.processEventNoteOff();
        return;
      }
      case ARI1EventNotePitchBend _ -> {
        return;
      }
    }
  }

  private void processEventConfigurationForFrame(
    final ARI1InstrumentContextType context,
    final ARI1EventConfigurationType event)
  {
    switch (event) {
      case ARI1EventConfigurationBufferSizeChanged _,
           ARI1EventConfigurationSampleRateChanged _ -> {
        // Unused.
      }
      case final ARI1EventConfigurationParameterChanged eventSet -> {
        this.processEventParameterChanged(context, eventSet);
      }
    }
  }

  private void processEventParameterChanged(
    final ARI1InstrumentContextType context,
    final ARI1EventConfigurationParameterChanged eventSet)
  {
    context.eventUnhandled(eventSet);
  }

  private void processEventNoteOff()
  {
    this.notes = Math.max(0, this.notes - 1);
  }

  private void processEventNoteOn(
    final ARI1InstrumentContextType context,
    final ARI1EventNoteOn event)
  {
    this.velocity = event.velocity();

    final var power =
      ((double) event.note() - 69.0) / 12.0;
    final var newFrequency =
      440.0 * StrictMath.pow(2.0, power);
    final var sampleRate =
      (double) context.statusCurrentSampleRate();

    this.notePhase =
      Math.PI / 2.0;
    this.notePhaseIncrement =
      newFrequency * (Math.PI * 2.0) / sampleRate;

    ++this.notes;
  }
}
