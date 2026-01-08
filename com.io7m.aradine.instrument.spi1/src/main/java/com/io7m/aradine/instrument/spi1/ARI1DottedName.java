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

package com.io7m.aradine.instrument.spi1;

import java.util.Comparator;
import java.util.Objects;

/**
 * A standard restricted dotted name.
 *
 * @param value The name
 */

public record ARI1DottedName(String value)
  implements Comparable<ARI1DottedName>
{
  /**
   * A standard restricted dotted name.
   *
   * @param value The name
   */

  public ARI1DottedName
  {
    Objects.requireNonNull(value, "value");

    final var pattern = ARI1DottedNamePatterns.dottedName();
    if (!pattern.matcher(value).matches()) {
      throw new IllegalArgumentException(
        "Name '%s' must match %s".formatted(value, pattern)
      );
    }
  }

  @Override
  public String toString()
  {
    return this.value;
  }

  @Override
  public int compareTo(
    final ARI1DottedName o)
  {
    return Comparator.comparing(ARI1DottedName::value).compare(this, o);
  }
}
