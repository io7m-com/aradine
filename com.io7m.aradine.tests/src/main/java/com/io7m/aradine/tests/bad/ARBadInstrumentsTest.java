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

package com.io7m.aradine.tests.bad;

import com.io7m.aradine.instrument.spi1.ARI1InstrumentContextType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentFactoryType;
import com.io7m.aradine.instrument.spi1.json_data.ARI1InstrumentParsers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.mockito.Mockito;

import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

public final class ARBadInstrumentsTest
{
  private static final List<ARI1InstrumentFactoryType> INSTRUMENTS =
    List.of(
      new com.io7m.aradine.instrument.bad_extradep.BadFactory(),
      new com.io7m.aradine.instrument.bad_noprovides.BadFactory(),
      new com.io7m.aradine.instrument.bad_provides.BadFactory(),
      new com.io7m.aradine.instrument.bad_uses.BadFactory()
    );

  private static final ARI1InstrumentParsers PARSERS =
    new ARI1InstrumentParsers();

  private ARI1InstrumentContextType context;

  @BeforeEach
  public void setup()
  {
    this.context =
      Mockito.mock(ARI1InstrumentContextType.class);
  }

  @TestFactory
  public Stream<DynamicTest> testBadInstrumentsInstrumentDescription()
  {
    return INSTRUMENTS.stream()
      .map(ARBadInstrumentsTest::testBadInstrumentDescription);
  }

  private static DynamicTest testBadInstrumentDescription(
    final ARI1InstrumentFactoryType factory)
  {
    return DynamicTest.dynamicTest(
      "testBadInstrumentDescription_%s".formatted(
        factory.getClass().getCanonicalName()
      ),
      () -> {
        PARSERS.parse(URI.create("urn:in"), factory.openInstrumentDescription());
      }
    );
  }

  @TestFactory
  public Stream<DynamicTest> testBadInstrumentsCreate()
  {
    return INSTRUMENTS.stream()
      .map(this::testBadCreateInstrument);
  }

  private DynamicTest testBadCreateInstrument(
    final ARI1InstrumentFactoryType factory)
  {
    return DynamicTest.dynamicTest(
      "testBadCreateInstrument_%s".formatted(
        factory.getClass().getCanonicalName()
      ),
      () -> {
        Assertions.assertThrows(IllegalStateException.class, () -> {
          factory.createInstrument(this.context);
        });
      }
    );
  }
}
