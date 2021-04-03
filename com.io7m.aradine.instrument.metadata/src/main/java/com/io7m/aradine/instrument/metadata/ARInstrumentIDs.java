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

package com.io7m.aradine.instrument.metadata;

import java.util.Objects;
import java.util.regex.Pattern;

public final class ARInstrumentIDs
{
  private static final Pattern VALID_INSTRUMENT_NAMES =
    Pattern.compile("([a-z0-9_]+)(\\.[a-z0-9_]+)+");

  private static final Pattern VALID_LICENSE_IDS =
    Pattern.compile("[A-Z0-9_]+");

  private static final Pattern VALID_PORT_IDS =
    Pattern.compile("[a-z0-9_]+");

  private ARInstrumentIDs()
  {

  }

  /**
   * Check that an instrument ID is valid.
   *
   * @param name The ID
   *
   * @return The ID
   *
   * @throws IllegalArgumentException On invalid IDs
   */

  public static String checkInstrumentId(
    final String name)
    throws IllegalArgumentException
  {
    if (!isInstrumentId(name)) {
      final var lineSeparator = System.lineSeparator();
      throw new IllegalArgumentException(
        new StringBuilder(128)
          .append("Invalid instrument name.")
          .append(lineSeparator)
          .append("  Expected: ")
          .append(VALID_INSTRUMENT_NAMES)
          .append(lineSeparator)
          .append("  Received: ")
          .append(name)
          .append(lineSeparator)
          .toString()
      );
    }
    return name;
  }

  /**
   * @param name The input text
   *
   * @return {@code true} if the name is an instrument ID
   */

  public static boolean isInstrumentId(
    final String name)
  {
    Objects.requireNonNull(name, "name");
    return VALID_INSTRUMENT_NAMES.matcher(name).matches();
  }

  /**
   * Check that an instrument license ID is valid.
   *
   * @param name The license ID
   *
   * @return The license ID
   *
   * @throws IllegalArgumentException On invalid license IDs
   */

  public static String checkInstrumentLicenseId(
    final String name)
    throws IllegalArgumentException
  {
    if (!isInstrumentLicenseId(name)) {
      final var lineSeparator = System.lineSeparator();
      throw new IllegalArgumentException(
        new StringBuilder(128)
          .append("Invalid instrument license ID.")
          .append(lineSeparator)
          .append("  Expected: ")
          .append(VALID_LICENSE_IDS)
          .append(lineSeparator)
          .append("  Received: ")
          .append(name)
          .append(lineSeparator)
          .toString()
      );
    }
    return name;
  }

  /**
   * @param name The input text
   *
   * @return {@code true} if the name is an instrument license ID
   */

  public static boolean isInstrumentLicenseId(
    final String name)
  {
    Objects.requireNonNull(name, "name");
    return VALID_LICENSE_IDS.matcher(name).matches();
  }

  /**
   * Check that an instrument port ID is valid.
   *
   * @param name The port ID
   *
   * @return The port ID
   *
   * @throws IllegalArgumentException On invalid port IDs
   */

  public static String checkInstrumentPortId(
    final String name)
    throws IllegalArgumentException
  {
    if (!isInstrumentPortId(name)) {
      final var lineSeparator = System.lineSeparator();
      throw new IllegalArgumentException(
        new StringBuilder(128)
          .append("Invalid instrument port ID.")
          .append(lineSeparator)
          .append("  Expected: ")
          .append(VALID_PORT_IDS)
          .append(lineSeparator)
          .append("  Received: ")
          .append(name)
          .append(lineSeparator)
          .toString()
      );
    }
    return name;
  }

  /**
   * @param name The input text
   *
   * @return {@code true} if the name is an instrument port ID
   */

  public static boolean isInstrumentPortId(
    final String name)
  {
    Objects.requireNonNull(name, "name");
    return VALID_PORT_IDS.matcher(name).matches();
  }
}
