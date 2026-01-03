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

package com.io7m.aradine.ensemble.internal.v1.json;

import com.io7m.aradine.api.instrument.ARInstrumentInstanceID;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.exc.ValueInstantiationException;

/**
 * An instrument instance ID deserializer.
 */

public final class AREnsJInstrumentInstanceIDDeserializer
  extends ValueDeserializer<ARInstrumentInstanceID>
{
  /**
   * An instrument instance ID deserializer.
   */

  public AREnsJInstrumentInstanceIDDeserializer()
  {

  }

  @Override
  public ARInstrumentInstanceID deserialize(
    final JsonParser p,
    final DeserializationContext ctxt)
    throws JacksonException
  {
    try {
      return ARInstrumentInstanceID.ofString(p.getString());
    } catch (final IllegalArgumentException e) {
      throw ValueInstantiationException.from(p, e.getMessage(), e);
    }
  }
}
