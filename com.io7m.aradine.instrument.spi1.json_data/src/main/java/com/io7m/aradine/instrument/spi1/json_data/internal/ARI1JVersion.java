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

public record ARI1JVersion(
  int major,
  int minor,
  int patch,
  Optional<ARI1JVersionQualifier> qualifier)
{
  /**
   * A (semantic) version number.
   *
   * @param major     The major number
   * @param minor     The minor number
   * @param patch     The patch number
   * @param qualifier The (possibly empty) qualifier
   */

  public ARI1JVersion
  {
    Objects.requireNonNull(qualifier, "Qualifier");
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
}
