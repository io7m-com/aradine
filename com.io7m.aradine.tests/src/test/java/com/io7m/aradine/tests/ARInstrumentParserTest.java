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

import com.io7m.anethum.common.ParseException;
import com.io7m.anethum.common.ParseStatus;
import com.io7m.aradine.instrument.metadata.ARInstrumentVersion;
import com.io7m.aradine.xml.ARInstrumentMetadataParsers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public final class ARInstrumentParserTest
{
  private Path directory;
  private ARInstrumentMetadataParsers parsers;
  private ArrayList<ParseStatus> events;

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.directory = ARTestDirectories.createTempDirectory();
    this.parsers = new ARInstrumentMetadataParsers();
    this.events = new ArrayList<>();
  }

  private void logError(
    final ParseStatus parseStatus)
  {
    this.events.add(parseStatus);
  }

  @Test
  public void testEmpty()
  {
    final var ex =
      assertThrows(ParseException.class, () -> {
        this.parsers.parseFile(
          ARTestDirectories.resourceOf(
            ARInstrumentParserTest.class,
            this.directory,
            "empty.xml"
          ),
          this::logError
        );
      });


    final var errors = new ArrayList<>(ex.statusValues());
    assertEquals(2, errors.size());

    {
      final var error = errors.remove(0);
      assertTrue(error.message().contains("Premature end of file."));
    }

    {
      final var error = errors.remove(0);
      assertTrue(error.message().contains("Premature end of file."));
    }
  }

  @Test
  public void testExample()
    throws Exception
  {
    final var meta =
      this.parsers.parseFile(
        ARTestDirectories.resourceOf(
          ARInstrumentParserTest.class,
          this.directory,
          "example.xml"
        ),
        this::logError
      );

    assertEquals("com.io7m.aradine.example", meta.id().value());
    assertEquals("Example", meta.readableName());
    assertEquals("https://www.example.com/plugin.xhtml", meta.site().get().toString());
    assertEquals("https://www.example.com/icon.png", meta.icon().get().toString());
    assertEquals("ISC", meta.licenseId().value());
    assertEquals(
      ARInstrumentVersion.builder()
        .setMajor(BigInteger.valueOf(1L))
        .setMinor(BigInteger.valueOf(2L))
        .setPatch(BigInteger.valueOf(3L))
        .setQualifier("SNAPSHOT")
        .build(),
      meta.version()
    );

    final var authors = new ArrayList<>(meta.authors());
    assertEquals("Someone 0", authors.remove(0).name());
    assertEquals("Someone 1", authors.remove(0).name());
    assertEquals("Someone 2", authors.remove(0).name());
  }
}
