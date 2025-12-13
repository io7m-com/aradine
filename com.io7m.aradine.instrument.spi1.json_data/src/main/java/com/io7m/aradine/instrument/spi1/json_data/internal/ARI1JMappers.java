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

import com.io7m.aradine.instrument.spi1.ARI1Documentation;
import com.io7m.aradine.instrument.spi1.ARI1DottedName;
import com.io7m.aradine.instrument.spi1.ARI1ParameterId;
import com.io7m.aradine.instrument.spi1.ARI1PortId;
import com.io7m.aradine.instrument.spi1.ARI1Version;
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

    dixBuilder.allowClass(ARI1Documentation.class);
    dixBuilder.allowClass(ARI1DottedName.class);
    dixBuilder.allowClass(ARI1JDocumentation.class);
    dixBuilder.allowClass(ARI1JInstrumentDescription.class);
    dixBuilder.allowClass(ARI1JParameterDescriptionInteger.class);
    dixBuilder.allowClass(ARI1JParameterDescriptionReal.class);
    dixBuilder.allowClass(ARI1JParameterDescriptionSampleMap.class);
    dixBuilder.allowClass(ARI1JPortDescriptionInputAudio.class);
    dixBuilder.allowClass(ARI1JPortDescriptionInputNote.class);
    dixBuilder.allowClass(ARI1JPortDescriptionOutputAudio.class);
    dixBuilder.allowClass(ARI1ParameterId.class);
    dixBuilder.allowClass(ARI1PortId.class);
    dixBuilder.allowClass(ARI1Version.class);
    dixBuilder.allowClass(String.class);
    dixBuilder.allowClass(double.class);
    dixBuilder.allowClass(long.class);
    dixBuilder.allowListsOfClass(ARI1JParameterDescriptionType.class);
    dixBuilder.allowListsOfClass(ARI1JPortDescriptionType.class);
    dixBuilder.allowListsOfClass(String.class);
    dixBuilder.allowMapsOfClass(String.class, String.class);
    dixBuilder.allowOptionalOfClass(ARI1Documentation.class);
    dixBuilder.allowSetsOfClass(String.class);

    final var serializers =
      dixBuilder.build();

    final var simpleModule = new SimpleModule();
    simpleModule.setDeserializers(serializers);

    simpleModule.addSerializer(
      ARI1Version.class,
      new ARI1JVersionSerializer()
    );
    simpleModule.addDeserializer(
      ARI1Version.class,
      new ARI1JVersionDeserializer()
    );

    simpleModule.addSerializer(
      ARI1DottedName.class,
      new ARI1JDottedNameSerializer()
    );
    simpleModule.addDeserializer(
      ARI1DottedName.class,
      new ARI1JDottedNameDeserializer()
    );

    simpleModule.addSerializer(
      ARI1ParameterId.class,
      new ARI1JParameterIdSerializer()
    );
    simpleModule.addDeserializer(
      ARI1ParameterId.class,
      new ARI1JParameterIdDeserializer()
    );

    simpleModule.addSerializer(
      ARI1PortId.class,
      new ARI1JPortIdSerializer()
    );
    simpleModule.addDeserializer(
      ARI1PortId.class,
      new ARI1JPortIdDeserializer()
    );

    simpleModule.addSerializer(
      ARI1Documentation.class,
      new ARI1JDocumentationSerializer()
    );
    simpleModule.addDeserializer(
      ARI1Documentation.class,
      new ARI1JDocumentationDeserializer()
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
