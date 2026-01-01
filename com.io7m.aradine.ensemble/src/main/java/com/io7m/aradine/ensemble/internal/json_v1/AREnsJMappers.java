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

package com.io7m.aradine.ensemble.internal.json_v1;

import com.io7m.aradine.ensemble.internal.model.AREnsModelCommandRecord;
import com.io7m.aradine.ensemble.internal.model.AREnsModelOpType;
import com.io7m.dixmont.core.DmJsonRestrictedDeserializers;
import com.io7m.lanark.core.RDottedName;
import com.io7m.verona.core.Version;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.time.OffsetDateTime;

/**
 * The JSON mappers used for serialization.
 */

public final class AREnsJMappers
{
  private static final JsonMapper MAPPER =
    createMapper();

  private AREnsJMappers()
  {

  }

  private static JsonMapper createMapper()
  {
    final var dixBuilder =
      DmJsonRestrictedDeserializers.builder();

    dixBuilder.allowClass(AREnsModelCommandRecord.class);
    dixBuilder.allowClass(OffsetDateTime.class);
    dixBuilder.allowClass(String.class);
    dixBuilder.allowClass(long.class);
    dixBuilder.allowListsOfClass(AREnsModelOpType.class);

    final var serializers =
      dixBuilder.build();

    final var simpleModule = new SimpleModule();
    simpleModule.setDeserializers(serializers);

    simpleModule.addSerializer(
      RDottedName.class,
      new AREnsJDottedNameSerializer()
    );
    simpleModule.addDeserializer(
      RDottedName.class,
      new AREnsJDottedNameDeserializer()
    );

    simpleModule.addSerializer(
      Version.class,
      new AREnsJVersionSerializer()
    );
    simpleModule.addDeserializer(
      Version.class,
      new AREnsJVersionDeserializer()
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
