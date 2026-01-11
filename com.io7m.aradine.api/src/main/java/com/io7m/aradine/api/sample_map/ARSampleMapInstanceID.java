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

package com.io7m.aradine.api.sample_map;

import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

/**
 * A sample map instance ID.
 *
 * @param value The raw ID value
 */

public record ARSampleMapInstanceID(
  UUID value)
  implements Comparable<ARSampleMapInstanceID>
{
  /**
   * A sample map instance ID.
   *
   * @param value The raw ID value
   */
  public ARSampleMapInstanceID
  {
    Objects.requireNonNull(value, "Value");
  }

  /**
   * @return A random instance ID
   */

  public static ARSampleMapInstanceID random()
  {
    return new ARSampleMapInstanceID(UUID.randomUUID());
  }

  /**
   * Parse a sample map ID.
   *
   * @param text The text
   *
   * @return The parsed ID
   */

  public static ARSampleMapInstanceID ofString(
    final String text)
  {
    return new ARSampleMapInstanceID(UUID.fromString(text));
  }

  @Override
  public String toString()
  {
    return this.value.toString();
  }

  @Override
  public int compareTo(
    final ARSampleMapInstanceID other)
  {
    return Comparator.comparing(ARSampleMapInstanceID::value)
      .compare(this, other);
  }
}
