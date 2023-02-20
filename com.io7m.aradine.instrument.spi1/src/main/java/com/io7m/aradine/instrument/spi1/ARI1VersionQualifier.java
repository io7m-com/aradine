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

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static java.lang.Integer.compareUnsigned;
import static java.lang.Integer.parseUnsignedInt;

/**
 * A version number qualifier.
 *
 * @param text The qualifier text
 */

public record ARI1VersionQualifier(
  String text)
  implements Comparable<ARI1VersionQualifier>
{
  private static final Pattern VALID_QUALIFIER =
    Pattern.compile("[A-Za-z0-9\\-]+(\\.[A-Za-z0-9\\-]+)*");

  private static final Pattern IS_NUMERIC =
    Pattern.compile("[0-9]+");

  /**
   * A version number qualifier.
   *
   * @param text The qualifier text
   */

  public ARI1VersionQualifier
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

  /**
   * @return {@code true} if this version qualifier denotes a snapshot version
   */

  public boolean isSnapshot()
  {
    return Objects.equals(this.text, "SNAPSHOT");
  }

  @Override
  public int compareTo(
    final ARI1VersionQualifier other)
  {
    if (this.text.equals(other.text)) {
      return 0;
    }

    final var identifiersA =
      List.of(this.text.split("\\."));
    final var identifiersB =
      List.of(other.text.split("\\."));

    /*
     * 1. Identifiers consisting of only digits are compared numerically.
     *
     * 2. Identifiers with letters or hyphens are compared lexically in ASCII
     *    sort order.
     *
     * 3. Numeric identifiers always have lower precedence than non-numeric
     *    identifiers.
     *
     * 4. A larger set of pre-release fields has a higher precedence than a
     *    smaller set, if all the preceding identifiers are equal.
     */

    final var sizeA = identifiersA.size();
    final var sizeB = identifiersB.size();
    final var smallest = Math.min(sizeA, sizeB);

    for (int index = 0; index < smallest; ++index) {
      final var r =
        compareIdentifier(identifiersA.get(index), identifiersB.get(index));
      if (r != 0) {
        return r;
      }
    }

    return (sizeA < sizeB) ? -1 : 1;
  }

  private static int compareIdentifier(
    final String identifierA,
    final String identifierB)
  {
    final var numericA =
      IS_NUMERIC.matcher(identifierA).matches();
    final var numericB =
      IS_NUMERIC.matcher(identifierB).matches();

    /*
     * Identifiers consisting of only digits are compared numerically.
     */

    if (numericA && numericB) {
      return compareUnsigned(
        parseUnsignedInt(identifierA),
        parseUnsignedInt(identifierB)
      );
    }

    /*
     * Identifiers with letters or hyphens are compared lexically in ASCII
     * sort order.
     */

    if (!numericA && !numericB) {
      return identifierA.compareTo(identifierB);
    }

    /*
     * Numeric identifiers always have lower precedence than non-numeric
     * identifiers.
     */

    return numericA ? -1 : 1;
  }

  @Override
  public String toString()
  {
    return this.text;
  }
}
