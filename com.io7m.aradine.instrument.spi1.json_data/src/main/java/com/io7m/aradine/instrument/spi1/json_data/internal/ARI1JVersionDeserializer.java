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

import com.io7m.aradine.instrument.spi1.ARI1Version;
import com.io7m.aradine.instrument.spi1.ARI1VersionException;
import com.io7m.aradine.instrument.spi1.ARI1VersionParser;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.exc.ValueInstantiationException;

/**
 * A version number deserializer.
 */

public final class ARI1JVersionDeserializer
  extends ValueDeserializer<ARI1JVersion>
{
  /**
   * A version number deserializer.
   */

  public ARI1JVersionDeserializer()
  {

  }

  @Override
  public ARI1JVersion deserialize(
    final JsonParser p,
    final DeserializationContext ctxt)
    throws JacksonException
  {
    try {
      final ARI1Version v1 = ARI1VersionParser.parse(p.getString());
      return new ARI1JVersion(
        v1.major(),
        v1.minor(),
        v1.patch(),
        v1.qualifier().map(x -> new ARI1JVersionQualifier(x.text()))
      );
    } catch (final ARI1VersionException e) {
      throw ValueInstantiationException.from(p, e.getMessage(), e);
    }
  }
}
