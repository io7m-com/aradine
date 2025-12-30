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

package com.io7m.aradine.api.instrument;

import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

/**
 * An instrument instance ID.
 *
 * @param value The raw ID value
 */

public record ARInstrumentInstanceID(
  UUID value)
  implements Comparable<ARInstrumentInstanceID>
{
  /**
   * An instrument instance ID.
   *
   * @param value The raw ID value
   */
  public ARInstrumentInstanceID
  {
    Objects.requireNonNull(value, "Value");
  }

  /**
   * @return A random instance ID
   */

  public static ARInstrumentInstanceID random()
  {
    return new ARInstrumentInstanceID(UUID.randomUUID());
  }

  /**
   * Parse an instrument ID.
   *
   * @param text The text
   *
   * @return The parsed ID
   */

  public static ARInstrumentInstanceID ofString(
    final String text)
  {
    return new ARInstrumentInstanceID(UUID.fromString(text));
  }

  @Override
  public String toString()
  {
    return this.value.toString();
  }

  @Override
  public int compareTo(
    final ARInstrumentInstanceID other)
  {
    return Comparator.comparing(ARInstrumentInstanceID::value)
      .compare(this, other);
  }
}
