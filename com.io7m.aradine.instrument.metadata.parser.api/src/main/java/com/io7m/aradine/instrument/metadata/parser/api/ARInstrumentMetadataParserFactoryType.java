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

package com.io7m.aradine.instrument.metadata.parser.api;

import com.io7m.anethum.api.ParserFactoryType;
import com.io7m.anethum.common.ParseException;
import com.io7m.aradine.instrument.metadata.ARInstrumentMetadata;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * The type of instrument parser factories.
 */

public interface ARInstrumentMetadataParserFactoryType
  extends ParserFactoryType<Void, ARInstrumentMetadata, ARInstrumentMetadataParserType>
{
  /**
   * Parse metadata from a class resource.
   *
   * @param clazz        The owning class
   * @param resourcePath The resource path
   *
   * @return Parsed metadata
   *
   * @throws IOException On errors
   */

  default ARInstrumentMetadata parseClassResource(
    final Class<?> clazz,
    final String resourcePath)
    throws IOException
  {
    final var resourceURL = clazz.getResource(resourcePath);
    try (var stream = resourceURL.openStream()) {
      return this.parse(resourceURL.toURI(), stream);
    } catch (final ParseException | URISyntaxException e) {
      throw new IOException(e);
    }
  }
}
