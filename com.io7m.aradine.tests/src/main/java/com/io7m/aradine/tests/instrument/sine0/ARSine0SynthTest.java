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

import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.audiosystem.ARAudioSystemUsableType;
import com.io7m.aradine.api.instrument.ARInstrumentInstanceID;
import com.io7m.aradine.audiosystem.main.ARAudioSystem;
import com.io7m.aradine.audiosystem.zero.ARAudioBackendZeroProvider;
import com.io7m.aradine.ensemble.internal.events.AREnsEventNoteOff;
import com.io7m.aradine.ensemble.internal.events.AREnsEventNoteOn;
import com.io7m.aradine.ensemble.internal.model.AREnsInstrumentContext;
import com.io7m.aradine.ensemble.internal.v1.context.AREnsSPI1InstrumentContextAdapter;
import com.io7m.aradine.ensemble.internal.v1.context.AREnsSPI1InstrumentDescriptions;
import com.io7m.aradine.ensemble.internal.v1.context.AREnsSPI1PortAudioAdapter;
import com.io7m.aradine.ensemble.internal.v1.context.AREnsSPI1PortNoteAdapter;
import com.io7m.aradine.instrument.sine0.ARSine0SynthFactory;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentDescription;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentType;
import com.io7m.aradine.instrument.spi1.ARI1PortNumber;
import com.io7m.aradine.instrument.spi1.ARI1PortSourceAudioType;
import com.io7m.aradine.instrument.spi1.ARI1PortTargetNoteType;
import com.io7m.aradine.instrument.spi1.json_data.ARI1InstrumentParsers;
import com.io7m.aradine.tests.AROutputCharting;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class ARSine0SynthTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ARSine0SynthTest.class);

  private ARSine0SynthFactory factory;
  private ARAudioSystemUsableType audioSystem;
  private ARI1InstrumentDescription description;
  private ARI1InstrumentType synth;
  private AREnsSPI1PortNoteAdapter notePort;
  private AREnsSPI1PortAudioAdapter outputL;
  private AREnsSPI1PortAudioAdapter outputR;
  private ARInstrumentInstanceID instanceId;
  private AREnsSPI1InstrumentContextAdapter context;

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
    this.instanceId =
      ARInstrumentInstanceID.random();
    this.audioSystem =
      createAudioSystem();
    this.context =
      AREnsSPI1InstrumentContextAdapter.wrap(
        this.audioSystem,
        AREnsInstrumentContext.create(
          this.audioSystem,
          AREnsSPI1InstrumentDescriptions.ofV1(
            this.instanceId,
            this.description
          )
        )
      );

    this.synth =
      this.factory.createInstrument(this.context);

    this.notePort =
      (AREnsSPI1PortNoteAdapter) this.context.declaredPort(
        new ARI1PortNumber(2L),
        ARI1PortTargetNoteType.class
      );
    this.outputL =
      (AREnsSPI1PortAudioAdapter) this.context.declaredPort(
        new ARI1PortNumber(0),
        ARI1PortSourceAudioType.class
      );
    this.outputR =
      (AREnsSPI1PortAudioAdapter) this.context.declaredPort(
        new ARI1PortNumber(1),
        ARI1PortSourceAudioType.class
      );
  }

  private static ARAudioSystemUsableType createAudioSystem()
    throws ARException
  {
    final var provider =
      new ARAudioBackendZeroProvider();

    return ARAudioSystem.open(
      List.of(provider)
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

    final var size =
      this.audioSystem.attributes().bufferSize().get().intValue();

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
      new AREnsEventNoteOn(0, 60, 1.0));
    this.notePort.eventPut(
      new AREnsEventNoteOff(512, 60, 0.0));

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
