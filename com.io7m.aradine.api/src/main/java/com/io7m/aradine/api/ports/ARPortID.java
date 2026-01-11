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

package com.io7m.aradine.api.ports;

import com.io7m.aradine.api.instrument.ARInstrumentInstanceID;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.UUID;

/**
 * A port ID.
 *
 * @param value The raw ID value
 */

public record ARPortID(
  UUID value)
  implements Comparable<ARPortID>
{
  /**
   * @param text The text
   *
   * @return A port ID from the given text
   */

  public static ARPortID ofString(
    final String text)
  {
    return new ARPortID(UUID.fromString(text));
  }

  /**
   * Deterministically generate a unique port ID.
   *
   * @param instance The instrument instance ID
   * @param number   The port number
   *
   * @return A unique port ID
   */

  public static ARPortID ofInstancePort(
    final ARInstrumentInstanceID instance,
    final ARPortNumber number)
  {
    final var text =
      String.format("%s:port:%s", instance, number);
    final var uuid =
      UUID.nameUUIDFromBytes(text.getBytes(StandardCharsets.UTF_8));

    return new ARPortID(uuid);
  }

  @Override
  public String toString()
  {
    return this.value.toString();
  }

  @Override
  public int compareTo(
    final ARPortID other)
  {
    return Comparator.comparing(ARPortID::value)
      .compare(this, other);
  }
}
