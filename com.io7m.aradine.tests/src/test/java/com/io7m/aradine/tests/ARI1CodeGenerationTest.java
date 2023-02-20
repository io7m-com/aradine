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


package com.io7m.aradine.tests;

import com.io7m.aradine.instrument.codegen.ARI1CodeGeneratorParameters;
import com.io7m.aradine.instrument.codegen.ARI1CodeGenerators;
import com.io7m.aradine.instrument.spi1.ARI1Version;
import com.io7m.aradine.instrument.spi1.xml.ARI1InstrumentParsers;
import com.io7m.aradine.instrument.spi1.xml.ARI1InstrumentSerializers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class ARI1CodeGenerationTest
{
  private ARI1InstrumentParsers parsers;
  private Path directory;
  private ARI1InstrumentSerializers serializers;
  private ARI1CodeGenerators codeGenerators;
  private Path outSrc;
  private Path outRes;

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
    this.codeGenerators =
      new ARI1CodeGenerators();

    this.outSrc =
      this.directory.resolve("src");
    this.outRes =
      this.directory.resolve("res");
  }

  @AfterEach
  public void tearDown()
    throws IOException
  {
    ARTestDirectories.deleteDirectory(this.directory);
  }

  /**
   * Code generation works for a basic instrument.
   *
   * @throws Exception On errors
   */

  @Test
  public void generateInstrument0()
    throws Exception
  {
    final var file =
      ARTestDirectories.resourceOf(
        ARI1InstrumentParserTest.class,
        this.directory,
        "instrument-0.xml"
      );

    final var newVersion =
      new ARI1Version(1, 2, 3, empty());
    final var newName =
      "com.io7m.aradine.instrument.sampler_xp0";

    final var generator =
      this.codeGenerators.createCodeGenerator(
        new ARI1CodeGeneratorParameters(
          newName,
          newVersion,
          "com.io7m.aradine.tests.generated",
          file,
          this.outSrc,
          this.outRes,
          this.parsers,
          this.serializers
        )
      );

    final var result = generator.execute();
    final var instrument = this.parsers.parseFile(result.resourceFile());
    assertEquals(newVersion, instrument.version());
    assertEquals(newName, instrument.identifier());
  }
}
