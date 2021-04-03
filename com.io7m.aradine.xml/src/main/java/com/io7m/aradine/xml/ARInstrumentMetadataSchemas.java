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

package com.io7m.aradine.xml;

import com.io7m.blackthorne.api.BTQualifiedName;
import com.io7m.jxe.core.JXESchemaDefinition;
import com.io7m.jxe.core.JXESchemaResolutionMappings;

import java.net.URI;

/**
 * Instrument data schemas.
 */

public final class ARInstrumentMetadataSchemas
{
  private static final URI INSTRUMENT_1_NS =
    URI.create("urn:com.io7m.aradine:instrument:1");

  private static final JXESchemaDefinition INSTRUMENT_SCHEMA_1 =
    JXESchemaDefinition.of(
      INSTRUMENT_1_NS,
      "instrument-1.xsd",
      ARInstrumentMetadataSchemas.class.getResource(
        "/com/io7m/aradine/xml/instrument-1.xsd")
    );

  private static final JXESchemaResolutionMappings INSTRUMENT_SCHEMAS =
    JXESchemaResolutionMappings.builder()
      .putMappings(INSTRUMENT_1_NS, INSTRUMENT_SCHEMA_1)
      .build();

  private ARInstrumentMetadataSchemas()
  {

  }

  public static JXESchemaResolutionMappings instrumentSchemas()
  {
    return INSTRUMENT_SCHEMAS;
  }

  public static URI instrument1NS()
  {
    return INSTRUMENT_1_NS;
  }

  public static BTQualifiedName instrument1Element(
    final String name)
  {
    return BTQualifiedName.of(
      instrument1NS().toString(),
      name
    );
  }
}
