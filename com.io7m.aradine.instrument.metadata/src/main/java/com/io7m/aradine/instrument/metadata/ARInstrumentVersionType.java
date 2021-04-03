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

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.Formattable;
import java.util.Formatter;
import java.util.regex.Pattern;

/**
 * An instrument name.
 */

@ImmutablesStyleType
@Value.Immutable
public interface ARInstrumentVersionType
  extends Comparable<ARInstrumentVersionType>, Formattable
{
  /**
   * The pattern describing valid qualifiers.
   */

  Pattern VALID_QUALIFIERS =
    Pattern.compile("[a-zA-Z_0-9]*");

  /**
   * @return The major version
   */

  BigInteger major();

  /**
   * @return The minor version
   */

  BigInteger minor();

  /**
   * @return The patch version
   */

  BigInteger patch();

  /**
   * @return The qualifier
   */

  @Value.Default
  default String qualifier()
  {
    return "";
  }

  @Override
  default int compareTo(
    final ARInstrumentVersionType other)
  {
    return Comparator.comparing(ARInstrumentVersionType::major)
      .thenComparing(ARInstrumentVersionType::minor)
      .thenComparing(ARInstrumentVersionType::patch)
      .thenComparing(ARInstrumentVersionType::qualifier)
      .compare(this, other);
  }

  @Override
  default void formatTo(
    final Formatter formatter,
    final int flags,
    final int width,
    final int precision)
  {
    if (this.qualifier().isEmpty()) {
      formatter.format(
        "%s.%s.%s",
        this.major(),
        this.minor(),
        this.patch()
      );
    } else {
      formatter.format(
        "%s.%s.%s-%s",
        this.major(),
        this.minor(),
        this.patch(),
        this.qualifier()
      );
    }
  }

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    final var vmj = this.major();
    if (vmj.compareTo(BigInteger.ZERO) < 0) {
      throw new IllegalArgumentException(
        String.format("Invalid major version: %s", vmj));
    }
    final var vmn = this.minor();
    if (vmn.compareTo(BigInteger.ZERO) < 0) {
      throw new IllegalArgumentException(
        String.format("Invalid minor version: %s", vmn));
    }
    final var vp = this.patch();
    if (vp.compareTo(BigInteger.ZERO) < 0) {
      throw new IllegalArgumentException(
        String.format("Invalid patch version: %s", vp));
    }
    final var vq = this.qualifier();
    final var matcher = VALID_QUALIFIERS.matcher(vq);
    if (!matcher.matches()) {
      throw new IllegalArgumentException(
        String.format("Invalid qualifier: %s", vq));
    }
  }
}
