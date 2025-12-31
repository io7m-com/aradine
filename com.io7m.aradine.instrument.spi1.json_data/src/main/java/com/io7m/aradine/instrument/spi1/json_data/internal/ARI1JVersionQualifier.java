/*
 * Copyright © 2022 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A version number qualifier.
 *
 * @param text The qualifier text
 */

public record ARI1JVersionQualifier(
  String text)
{
  private static final Pattern VALID_QUALIFIER =
    Pattern.compile("[A-Za-z0-9\\-]+(\\.[A-Za-z0-9\\-]+)*");

  @Override
  public String toString()
  {
    return this.text;
  }

  /**
   * A version number qualifier.
   *
   * @param text The qualifier text
   */

  public ARI1JVersionQualifier
  {
    Objects.requireNonNull(text, "text");

    final var matcher = VALID_QUALIFIER.matcher(text);
    if (!matcher.matches()) {
      throw new IllegalArgumentException(
        "Qualifier '%s' must match the pattern '%s'"
          .formatted(text, VALID_QUALIFIER)
      );
    }
  }
}
