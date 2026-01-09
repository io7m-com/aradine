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

package com.io7m.aradine.tests.instrument.sine0;

import com.io7m.aradine.ensemble.internal.v1.context.AREns1InstrumentContext;
import com.io7m.aradine.ensemble.internal.v1.context.AREns1PortSourceAudio;
import com.io7m.aradine.ensemble.internal.v1.context.AREns1PortTargetNote;
import com.io7m.aradine.instrument.sine0.ARSine0SynthFactory;
import com.io7m.aradine.instrument.spi1.ARI1EventNoteOff;
import com.io7m.aradine.instrument.spi1.ARI1EventNoteOn;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentDescription;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentType;
import com.io7m.aradine.instrument.spi1.ARI1PortNumber;
import com.io7m.aradine.instrument.spi1.ARI1PortSourceAudioType;
import com.io7m.aradine.instrument.spi1.ARI1PortTargetNoteType;
import com.io7m.aradine.instrument.spi1.json_data.ARI1InstrumentParsers;
import com.io7m.aradine.tests.ARAudioSystemAttributes;
import com.io7m.aradine.tests.AROutputCharting;
import com.io7m.aradine.tests.ARTestFrequencyAnalysis;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.DoubleBuffer;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class ARSine0SynthTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ARSine0SynthTest.class);

  private AREns1InstrumentContext context;
  private ARSine0SynthFactory factory;
  private ARAudioSystemAttributes audioSystem;
  private ARI1InstrumentDescription description;
  private ARI1InstrumentType synth;
  private AREns1PortTargetNote notePort;
  private AREns1PortSourceAudio outputL;
  private AREns1PortSourceAudio outputR;

  @BeforeEach
  public void setup()
    throws Exception
  {
    this.factory =
      new ARSine0SynthFactory();
    this.description =
      new ARI1InstrumentParsers()
        .parse(
          URI.create("urn:input"),
          this.factory.openInstrumentDescription()
        );
    this.audioSystem =
      new ARAudioSystemAttributes();
    this.context =
      AREns1InstrumentContext.create(
        this.description,
        this.audioSystem
      );

    this.synth =
      this.factory.createInstrument(this.context);

    this.notePort =
      (AREns1PortTargetNote) this.context.declaredPort(
        new ARI1PortNumber(2L),
        ARI1PortTargetNoteType.class
      );
    this.outputL =
      (AREns1PortSourceAudio) this.context.declaredPort(
        new ARI1PortNumber(0),
        ARI1PortSourceAudioType.class
      );
    this.outputR =
      (AREns1PortSourceAudio) this.context.declaredPort(
        new ARI1PortNumber(1),
        ARI1PortSourceAudioType.class
      );
  }

  @AfterEach
  public void tearDown(
    final TestInfo info)
    throws IOException
  {
    final var fileName =
      "%s_%s.png".formatted(
        this.getClass().getSimpleName(),
        info.getDisplayName().replaceAll("\\(\\)", "")
      );

    final var outputFile =
      Paths.get(fileName);

    AROutputCharting.chart(
      this.outputL.buffer(),
      this.outputR.buffer(),
      outputFile
    );

    LOG.debug("Output chart: {}", outputFile);
  }

  @Test
  public void testNoNotes()
  {
    this.synth.process(this.context);

    final var size = this.audioSystem.bufferSize().get().intValue();
    for (var index = 0; index < size; ++index) {
      assertEquals(0.0, this.outputL.read(index));
      assertEquals(0.0, this.outputR.read(index));
    }
  }

  @Test
  public void testMiddleC()
    throws Exception
  {
    this.notePort.eventPut(
      new ARI1EventNoteOn(0, 60, 1.0));
    this.notePort.eventPut(
      new ARI1EventNoteOff(512, 60, 0.0));

    this.synth.process(this.context);

    assertEquals(1.0, this.outputL.read(0), 0.01);
    assertEquals(1.0, this.outputR.read(0), 0.01);
    assertEquals(0.0, this.outputL.read(512));
    assertEquals(0.0, this.outputR.read(512));

    final double rmsL = rmsOf(this.outputL.buffer());
    final double rmsR = rmsOf(this.outputR.buffer());
    assertEquals(0.707, rmsL, 0.01);
    assertEquals(0.707, rmsR, 0.01);
  }

  private static double rmsOf(
    final double[] buffer)
  {
    double sum = 0.0;
    for (var index = 0; index < 512; ++index) {
      sum += buffer[index] * buffer[index];
    }
    return Math.sqrt(sum / 512);
  }
}
