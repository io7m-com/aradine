/*
 * Copyright © 2021 Mark Raynsford <code@io7m.com> http://io7m.com
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

package com.io7m.aradine.tests;

import com.io7m.aradine.instrument.metadata.ARInstrumentAuthor;
import com.io7m.aradine.instrument.metadata.ARInstrumentDocumentation;
import com.io7m.aradine.instrument.metadata.ARInstrumentDocumentationParagraph;
import com.io7m.aradine.instrument.metadata.ARInstrumentID;
import com.io7m.aradine.instrument.metadata.ARInstrumentInputPortSignalDefinition;
import com.io7m.aradine.instrument.metadata.ARInstrumentLicenseID;
import com.io7m.aradine.instrument.metadata.ARInstrumentMetadata;
import com.io7m.aradine.instrument.metadata.ARInstrumentOutputPortSignalDefinition;
import com.io7m.aradine.instrument.metadata.ARInstrumentPortID;
import com.io7m.aradine.instrument.metadata.ARInstrumentVersion;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

public final class AREqualsTest
{
  @TestFactory
  public Stream<DynamicTest> testGeneratedClasses()
  {
    return Stream.of(
      ARInstrumentAuthor.class,
      ARInstrumentDocumentation.class,
      ARInstrumentDocumentationParagraph.class,
      ARInstrumentID.class,
      ARInstrumentInputPortSignalDefinition.class,
      ARInstrumentLicenseID.class,
      ARInstrumentMetadata.class,
      ARInstrumentOutputPortSignalDefinition.class,
      ARInstrumentPortID.class,
      ARInstrumentVersion.class
    ).map(AREqualsTest::testFor);
  }

  private static DynamicTest testFor(
    final Class<?> c)
  {
    return DynamicTest.dynamicTest(
      String.format("testEquals_%s", c.getCanonicalName()),
      () -> {
        EqualsVerifier.forClass(c)
          .suppress(Warning.NULL_FIELDS)
          .verify();
      });
  }
}
