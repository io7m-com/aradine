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

import com.io7m.aradine.instrument.spi1.json_data.internal.ARI1JInstrumentDescription;
import com.io7m.sumjack.core.SjGeneratorConfiguration;
import com.io7m.sumjack.core.SjGenerators;
import com.io7m.sumjack.core.SjSchemaVersion;
import com.io7m.sumjack.core.standard.SjPrimitives;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.nio.file.Paths;

import static com.io7m.aradine.instrument.spi1.json_schemagen.ARIJ1DocumentationDefinition.DOCUMENTATION;
import static com.io7m.aradine.instrument.spi1.json_schemagen.ARIJ1DottedNameDefinition.DOTTED_NAME_DEFINITION;
import static com.io7m.aradine.instrument.spi1.json_schemagen.ARIJ1ListStringDefinition.LIST_STRING_DEFINITION;
import static com.io7m.aradine.instrument.spi1.json_schemagen.ARIJ1ParameterIdDefinition.PARAMETER_ID_DEFINITION;
import static com.io7m.aradine.instrument.spi1.json_schemagen.ARIJ1PortIdDefinition.PORT_ID_DEFINITION;
import static com.io7m.aradine.instrument.spi1.json_schemagen.ARIJ1VersionDefinition.VERSION_DEFINITION;

/**
 * The schema generator.
 */

public final class ARIJ1SchemaGeneratorMain
{
  private ARIJ1SchemaGeneratorMain()
  {

  }

  /**
   * The schema generator.
   *
   * @param args The command-line arguments
   *
   * @throws Exception On errors
   */

  public static void main(
    final String[] args)
    throws Exception
  {
    final var outputFile =
      Paths.get(args[0]);

    final var configuration =
      SjGeneratorConfiguration.builder()
        .addDefinitions(SjPrimitives.values())
        .addDefinitions(DOCUMENTATION)
        .addDefinitions(DOTTED_NAME_DEFINITION)
        .addDefinitions(LIST_STRING_DEFINITION)
        .addDefinitions(PARAMETER_ID_DEFINITION)
        .addDefinitions(PORT_ID_DEFINITION)
        .addDefinitions(VERSION_DEFINITION)
        .setId(URI.create("urn:com.io7m.aradine.instrument:1.0"))
        .setMapper(JsonMapper.shared())
        .setRootType(ARI1JInstrumentDescription.class)
        .setSchemaVersion(SjSchemaVersion.DRAFT_2020_12)
        .setTitle("Aradine Instrument 1.0")
        .build();

    final var generator =
      SjGenerators.create(configuration);

    generator.executeAndWrite(outputFile);
  }
}
