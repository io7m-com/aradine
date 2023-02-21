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


package com.io7m.aradine.tests.spi1.xml;

import com.io7m.anethum.common.ParseException;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentDescriptionType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionIntegerType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionRealType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionSampleMapType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterId;
import com.io7m.aradine.instrument.spi1.ARI1PortDescriptionOutputSampledType;
import com.io7m.aradine.instrument.spi1.ARI1PortId;
import com.io7m.aradine.instrument.spi1.ARI1Version;
import com.io7m.aradine.instrument.spi1.xml.ARI1InstrumentParsers;
import com.io7m.aradine.instrument.spi1.xml.ARI1InstrumentSerializers;
import com.io7m.aradine.tests.ARTestDirectories;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class ARI1InstrumentParserTest
{
  private ARI1InstrumentParsers parsers;
  private Path directory;
  private ARI1InstrumentSerializers serializers;

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.directory =
      ARTestDirectories.createTempDirectory();
    this.parsers =
      new ARI1InstrumentParsers();
    this.serializers =
      new ARI1InstrumentSerializers();
  }

  @AfterEach
  public void tearDown()
    throws IOException
  {
    ARTestDirectories.deleteDirectory(this.directory);
  }

  /**
   * A basic instrument can be parsed.
   *
   * @throws Exception On errors
   */

  @Test
  public void testParseInstrument0()
    throws Exception
  {
    final var file =
      ARTestDirectories.resourceOf(
        ARI1InstrumentParserTest.class,
        this.directory,
        "instrument-0.xml"
      );

    final var instrument = this.parsers.parseFile(file);
    assertEquals(
      "com.io7m.aradine.instrument.sampler_xp0",
      instrument.identifier()
    );
    assertEquals(
      new ARI1Version(1, 2, 3, Optional.empty()),
      instrument.version()
    );

    {
      final var p =
        (ARI1ParameterDescriptionIntegerType)
          instrument.parameters().get(new ARI1ParameterId(0));

      assertEquals("com.io7m.aradine.semitones", p.unitOfMeasurement());
      assertEquals(1L, p.valueMinimum());
      assertEquals(120L, p.valueMaximum());
      assertEquals(24L, p.valueDefault());
      assertEquals("Pitch Bend Range", p.label());
    }

    {
      final var p =
        (ARI1ParameterDescriptionRealType)
          instrument.parameters().get(new ARI1ParameterId(1));

      assertEquals("com.io7m.aradine.position_normal", p.unitOfMeasurement());
      assertEquals(0.0, p.valueMinimum());
      assertEquals(1.0, p.valueMaximum());
      assertEquals(0.8, p.valueDefault());
      assertEquals("Loop Point", p.label());
    }

    {
      final var p =
        (ARI1ParameterDescriptionSampleMapType)
          instrument.parameters().get(new ARI1ParameterId(2));

      assertEquals("Samples", p.label());
    }

    {
      final var p =
        (ARI1PortDescriptionOutputSampledType)
          instrument.ports().get(new ARI1PortId(0));

      assertEquals("Output L", p.label());
      assertEquals(Set.of("com.io7m.aradine.port.main_left"), p.semantics());
    }

    {
      final var p =
        (ARI1PortDescriptionOutputSampledType)
          instrument.ports().get(new ARI1PortId(1));

      assertEquals("Output R", p.label());
      assertEquals(Set.of("com.io7m.aradine.port.main_right"), p.semantics());
    }

    this.roundTrip(instrument);
  }

  /**
   * Invalid inputs must cause errors.
   */

  @TestFactory
  public Stream<DynamicTest> testErrors()
  {
    return Stream.of(
      "instrument-error-0.xml",
      "instrument-error-1.xml",
      "instrument-error-2.xml",
      "instrument-error-3.xml",
      "instrument-error-4.xml",
      "instrument-error-5.xml"
    ).map(name -> {
      return DynamicTest.dynamicTest("testErrors_" + name, () -> {
        final var file =
          ARTestDirectories.resourceOf(
            ARI1InstrumentParserTest.class,
            this.directory,
            name
          );

        final var ex =
          assertThrows(ParseException.class, () -> {
            this.parsers.parseFile(file);
          });

        assertNotEquals(0, ex.statusValues().size());
      });
    });
  }

  private void roundTrip(
    final ARI1InstrumentDescriptionType instrument)
    throws Exception
  {
    final var path = this.directory.resolve("out.xml");
    this.serializers.serializeFile(path, instrument);
    final var after = this.parsers.parseFile(path);
    assertEquals(instrument, after);
  }
}
