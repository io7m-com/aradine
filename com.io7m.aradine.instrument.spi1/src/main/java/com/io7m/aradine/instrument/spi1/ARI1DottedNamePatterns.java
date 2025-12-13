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

import java.util.regex.Pattern;

/**
 * The patterns that define standard dotted names and prefixes.
 */

public final class ARI1DottedNamePatterns
{
  private ARI1DottedNamePatterns()
  {

  }

  private static final Pattern DOTTED_NAME =
    Pattern.compile("([a-z][a-z0-9_-]{0,63})(\\.[a-z][a-z0-9_-]{0,62}){0,15}");

  private static final Pattern DOTTED_PREFIX =
    Pattern.compile("([a-z][a-z0-9_-]{0,63})(\\.[a-z][a-z0-9_-]{0,62}){0,15}\\.");

  /**
   * @return The pattern that defines a restricted dotted name
   */

  public static Pattern dottedName()
  {
    return DOTTED_NAME;
  }

  /**
   * @return The pattern that defines a restricted dotted prefix
   */

  public static Pattern dottedPrefix()
  {
    return DOTTED_PREFIX;
  }
}
