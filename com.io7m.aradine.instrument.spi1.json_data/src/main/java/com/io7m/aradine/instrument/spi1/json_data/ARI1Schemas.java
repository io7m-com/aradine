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

package com.io7m.aradine.instrument.spi1.json_data;

import java.net.URI;
import java.net.URL;

/**
 * Access to schemas.
 */

public final class ARI1Schemas
{
  private static final URI SCHEMA_1 =
    URI.create("urn:com.io7m.aradine.instrument:1.0");

  private ARI1Schemas()
  {

  }

  /**
   * @return The schema URI
   */

  public static URI schema1()
  {
    return SCHEMA_1;
  }

  /**
   * @return The URL of the schema resource
   */

  public static URL schema1Resource()
  {
    return ARI1Schemas.class.getResource(
      "/com/io7m/aradine/instrument/spi1/json_data/schema.json"
    );
  }
}
