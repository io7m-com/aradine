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

import com.io7m.aradine.instrument.spi1.ARI1PortId;
import com.io7m.sumjack.core.SjDefinitionProviderType;
import com.io7m.sumjack.core.SjDefinitionType;
import com.io7m.sumjack.core.SjGeneratorConfiguration;

/**
 * Port IDs.
 */

public enum ARIJ1PortIdDefinition
  implements SjDefinitionProviderType
{
  /**
   * Port IDs.
   */

  PORT_ID_DEFINITION;

  @Override
  public String typeName()
  {
    return ARI1PortId.class.getSimpleName();
  }

  @Override
  public SjDefinitionType create(
    final SjGeneratorConfiguration configuration)
  {
    return () -> {
      final var mapper = configuration.mapper();

      final var object = mapper.createObjectNode();
      object.put("type", "integer");
      object.put("description", "A port ID.");
      object.put("minimum", 0);
      object.put("maximum", 4294967295L);
      return object;
    };
  }
}
