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

package com.io7m.aradine.instrument.spi1;

/**
 * A port number.
 *
 * @param value The number
 */

public record ARI1PortNumber(
  long value)
  implements Comparable<ARI1PortNumber>
{
  /**
   * A port number.
   *
   * @param value The number
   */

  public ARI1PortNumber
  {
    if (!(value >= 0 && value <= 4294967295L)) {
      throw new IllegalArgumentException(
        "Port numbers must be in the range [0, 4294967295]");
    }
  }

  @Override
  public String toString()
  {
    return Long.toUnsignedString(this.value);
  }

  @Override
  public int compareTo(
    final ARI1PortNumber o)
  {
    return Long.compareUnsigned(this.value, o.value);
  }
}
