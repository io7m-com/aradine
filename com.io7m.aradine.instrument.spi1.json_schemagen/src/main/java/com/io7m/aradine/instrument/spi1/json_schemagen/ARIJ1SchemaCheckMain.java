/*
 * Copyright © 2025 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

package com.io7m.aradine.instrument.spi1.json_schemagen;

import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Schema validator.
 */

public final class ARIJ1SchemaCheckMain
{
  private ARIJ1SchemaCheckMain()
  {

  }

  /**
   * Schema validator.
   *
   * @param args The command-line arguments
   *
   * @throws Exception On errors
   */

  public static void main(
    final String[] args)
    throws Exception
  {
    final var schemaFile =
      Paths.get(args[0]);
    final var sourceFile =
      Paths.get(args[1]);

    final var jsonSchemaFactory =
      JsonSchemaFactory.getInstance(
        SpecVersion.VersionFlag.V202012, builder -> {

        }
      );

    final var builder =
      SchemaValidatorsConfig.builder();
    final var config =
      builder.build();
    final var schema =
      jsonSchemaFactory.getSchema(
        SchemaLocation.of(
          schemaFile.toAbsolutePath()
            .toUri()
            .toString()
        ), config
      );

    final var text =
      Files.readString(sourceFile, StandardCharsets.UTF_8);

    final var assertions =
      schema.validate(
        text, InputFormat.JSON, executionContext -> {
          final var execConfig = executionContext.getExecutionConfig();
          execConfig.setFormatAssertionsEnabled(true);
          execConfig.setDebugEnabled(true);
        });

    for (final var assertion : assertions) {
      System.err.printf(
        "error: (%s): %s: %s%n",
        assertion.getSchemaLocation(),
        assertion.getCode(),
        assertion.getError()
      );
    }
  }
}
