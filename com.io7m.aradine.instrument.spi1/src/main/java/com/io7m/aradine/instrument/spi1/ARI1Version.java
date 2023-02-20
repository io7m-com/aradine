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
import java.util.Optional;

import static java.lang.Integer.toUnsignedString;

/**
 * A (semantic) version number.
 *
 * @param major     The major number
 * @param minor     The minor number
 * @param patch     The patch number
 * @param qualifier The (possibly empty) qualifier
 */

public record ARI1Version(
  int major,
  int minor,
  int patch,
  Optional<ARI1VersionQualifier> qualifier)
  implements Comparable<ARI1Version>
{
  private static final Comparator<Integer> COMPARE_UNSIGNED =
    (x, y) -> Integer.compareUnsigned(x.intValue(), y.intValue());

  private static final Comparator<Optional<ARI1VersionQualifier>> COMPARE_QUALIFIER =
    (x, y) -> {
      if (Objects.equals(x, y)) {
        return 0;
      }
      if (x.isEmpty()) {
        return 1;
      }
      if (y.isEmpty()) {
        return -1;
      }

      final var xx = x.get();
      final var yy = y.get();
      return xx.compareTo(yy);
    };

  /**
   * A (semantic) version number.
   *
   * @param major     The major number
   * @param minor     The minor number
   * @param patch     The patch number
   * @param qualifier The (possibly empty) qualifier
   */

  public ARI1Version
  {
    Objects.requireNonNull(qualifier, "qualifier");
  }

  /**
   * @return {@code true} if this version is a snapshot version
   */

  public boolean isSnapshot()
  {
    return this.qualifier.stream().anyMatch(ARI1VersionQualifier::isSnapshot);
  }

  /**
   * Create a version number without a qualifier.
   *
   * @param major The major version
   * @param minor The minor version
   * @param patch The patch version
   *
   * @return A version number
   */

  public static ARI1Version of(
    final int major,
    final int minor,
    final int patch)
  {
    return new ARI1Version(major, minor, patch, Optional.empty());
  }

  /**
   * Create a version number with a qualifier.
   *
   * @param major     The major version
   * @param minor     The minor version
   * @param patch     The patch version
   * @param qualifier The qualifier
   *
   * @return A version number
   */

  public static ARI1Version of(
    final int major,
    final int minor,
    final int patch,
    final ARI1VersionQualifier qualifier)
  {
    return new ARI1Version(major, minor, patch, Optional.of(qualifier));
  }

  /**
   * Create a version number with a qualifier.
   *
   * @param major     The major version
   * @param minor     The minor version
   * @param patch     The patch version
   * @param qualifier The qualifier
   *
   * @return A version number
   *
   * @throws IllegalArgumentException On unparseable qualifiers
   */

  public static ARI1Version of(
    final int major,
    final int minor,
    final int patch,
    final String qualifier)
    throws IllegalArgumentException
  {
    return of(
      major,
      minor,
      patch,
      new ARI1VersionQualifier(qualifier)
    );
  }

  @Override
  public String toString()
  {
    final var text = new StringBuilder(32);
    text.append(toUnsignedString(this.major));
    text.append('.');
    text.append(toUnsignedString(this.minor));
    text.append('.');
    text.append(toUnsignedString(this.patch));

    if (this.qualifier.isPresent()) {
      text.append('-');
      text.append(this.qualifier.get());
    }
    return text.toString();
  }

  @Override
  public int compareTo(
    final ARI1Version other)
  {
    return Comparator.comparing(ARI1Version::major, COMPARE_UNSIGNED)
      .thenComparing(ARI1Version::minor, COMPARE_UNSIGNED)
      .thenComparing(ARI1Version::patch, COMPARE_UNSIGNED)
      .thenComparing(ARI1Version::qualifier, COMPARE_QUALIFIER)
      .compare(this, other);
  }
}
