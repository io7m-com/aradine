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

package com.io7m.aradine.instrument.spi1.json_data.internal;

import com.io7m.dixmont.core.DmJsonRestrictedDeserializers;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * The JSON mappers used for serialization.
 */

public final class ARI1JMappers
{
  private static final JsonMapper MAPPER =
    createMapper();

  private ARI1JMappers()
  {

  }

  private static JsonMapper createMapper()
  {
    final var dixBuilder =
      DmJsonRestrictedDeserializers.builder();

    dixBuilder.allowClass(ARI1JDottedName.class);
    dixBuilder.allowClass(ARI1JInstrumentDescription.class);
    dixBuilder.allowClass(ARI1JParameterDescriptionInteger.class);
    dixBuilder.allowClass(ARI1JParameterDescriptionReal.class);
    dixBuilder.allowClass(ARI1JParameterDescriptionSampleMap.class);
    dixBuilder.allowClass(ARI1JParameterNumber.class);
    dixBuilder.allowClass(ARI1JPortDirection.class);
    dixBuilder.allowClass(ARI1JPortKind.class);
    dixBuilder.allowClass(ARI1JPortNumber.class);
    dixBuilder.allowClass(ARI1JVersion.class);
    dixBuilder.allowClass(String.class);
    dixBuilder.allowClass(double.class);
    dixBuilder.allowClass(int.class);
    dixBuilder.allowClass(long.class);
    dixBuilder.allowListsOfClass(ARI1JParameterDescriptionType.class);
    dixBuilder.allowListsOfClass(ARI1JPortDescription.class);
    dixBuilder.allowListsOfClass(String.class);
    dixBuilder.allowMapsOfClass(String.class, String.class);
    dixBuilder.allowOptionalOfClass(ARI1JDocumentation.class);
    dixBuilder.allowOptionalOfClass(ARI1JVersionQualifier.class);
    dixBuilder.allowSetsOfClass(String.class);

    final var serializers =
      dixBuilder.build();

    final var simpleModule = new SimpleModule();
    simpleModule.setDeserializers(serializers);

    simpleModule.addSerializer(
      ARI1JVersion.class,
      new ARI1JVersionSerializer()
    );
    simpleModule.addDeserializer(
      ARI1JVersion.class,
      new ARI1JVersionDeserializer()
    );

    simpleModule.addSerializer(
      ARI1JDottedName.class,
      new ARI1JDottedNameSerializer()
    );
    simpleModule.addDeserializer(
      ARI1JDottedName.class,
      new ARI1JDottedNameDeserializer()
    );

    simpleModule.addSerializer(
      ARI1JParameterNumber.class,
      new ARI1JParameterNumberSerializer()
    );
    simpleModule.addDeserializer(
      ARI1JParameterNumber.class,
      new ARI1JParameterNumberDeserializer()
    );

    simpleModule.addSerializer(
      ARI1JPortNumber.class,
      new ARI1JPortNumberSerializer()
    );
    simpleModule.addDeserializer(
      ARI1JPortNumber.class,
      new ARI1JPortNumberDeserializer()
    );

    final var builder = JsonMapper.builder();
    builder.addModule(simpleModule);
    return builder.build();
  }

  /**
   * @return The main JSON mapper
   */

  public static JsonMapper mapper()
  {
    return MAPPER;
  }
}
