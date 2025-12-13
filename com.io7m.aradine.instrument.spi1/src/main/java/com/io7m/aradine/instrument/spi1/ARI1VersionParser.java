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

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseUnsignedInt;

/**
 * A parser of version numbers.
 */

public final class ARI1VersionParser
{
  private static final Pattern VERSION_TEXT =
    Pattern.compile("([0-9]+)\\.([0-9]+)\\.([0-9]+)(-(.+))?");

  private static final Pattern VERSION_OSGI_TEXT =
    Pattern.compile("([0-9]+)\\.([0-9]+)\\.([0-9]+)(\\.(.+))?");

  private static final Pattern VERSION_ONLY_MAJOR_TEXT =
    Pattern.compile("([0-9]+)(-(.+))?");

  private static final Pattern VERSION_ONLY_MAJOR_MINOR_TEXT =
    Pattern.compile("([0-9]+)\\.([0-9]+)(-(.+))?");

  private ARI1VersionParser()
  {

  }

  /**
   * Parse a version number.
   *
   * @param text The version text
   *
   * @return The parsed version
   *
   * @throws ARI1VersionException On errors
   */

  public static ARI1Version parse(
    final String text)
    throws ARI1VersionException
  {
    final var matcher = VERSION_TEXT.matcher(text);
    if (matcher.matches()) {
      try {
        final var qualifierText = matcher.group(5);
        final Optional<ARI1VersionQualifier> qualifier;
        if (qualifierText != null) {
          qualifier = Optional.of(new ARI1VersionQualifier(qualifierText));
        } else {
          qualifier = Optional.empty();
        }

        return new ARI1Version(
          parseUnsignedInt(matcher.group(1)),
          parseUnsignedInt(matcher.group(2)),
          parseUnsignedInt(matcher.group(3)),
          qualifier
        );
      } catch (final Exception e) {
        throw new ARI1VersionException(
          "Version text '%s' cannot be parsed: %s"
            .formatted(text, e.getMessage()),
          e
        );
      }
    }

    throw new ARI1VersionException(
      "Version text '%s' must match the pattern '%s'"
        .formatted(text, VERSION_TEXT)
    );
  }

  /**
   * Parse an OSGi style version number.
   *
   * @param text The version text
   *
   * @return The parsed version
   *
   * @throws ARI1VersionException On errors
   */

  public static ARI1Version parseOSGi(
    final String text)
    throws ARI1VersionException
  {
    final var matcher = VERSION_OSGI_TEXT.matcher(text);
    if (matcher.matches()) {
      try {
        final var qualifierText = matcher.group(5);
        final Optional<ARI1VersionQualifier> qualifier;
        if (qualifierText != null) {
          qualifier = Optional.of(new ARI1VersionQualifier(qualifierText));
        } else {
          qualifier = Optional.empty();
        }

        return new ARI1Version(
          parseUnsignedInt(matcher.group(1)),
          parseUnsignedInt(matcher.group(2)),
          parseUnsignedInt(matcher.group(3)),
          qualifier
        );
      } catch (final Exception e) {
        throw new ARI1VersionException(
          "Version text '%s' cannot be parsed: %s"
            .formatted(text, e.getMessage()),
          e
        );
      }
    }

    throw new ARI1VersionException(
      "Version text '%s' must match the pattern '%s'"
        .formatted(text, VERSION_OSGI_TEXT)
    );
  }

  /**
   * Parse a version number, allowing for missing components.
   *
   * @param text The version text
   *
   * @return The parsed version
   *
   * @throws ARI1VersionException On errors
   */

  public static ARI1Version parseLax(
    final String text)
    throws ARI1VersionException
  {
    {
      final var m = VERSION_TEXT.matcher(text);
      if (m.matches()) {
        return parse(text);
      }
    }

    {
      final var m = VERSION_OSGI_TEXT.matcher(text);
      if (m.matches()) {
        return parseOSGi(text);
      }
    }

    {
      final var m = VERSION_ONLY_MAJOR_MINOR_TEXT.matcher(text);
      if (m.matches()) {
        return parseMajorMinor(text, m);
      }
    }

    {
      final var m = VERSION_ONLY_MAJOR_TEXT.matcher(text);
      if (m.matches()) {
        return parseMajor(text, m);
      }
    }

    throw new ARI1VersionException(
      "Version text '%s' must match the pattern '%s'"
        .formatted(text, VERSION_TEXT)
    );
  }

  private static ARI1Version parseMajor(
    final String text,
    final Matcher matcher)
    throws ARI1VersionException
  {
    try {
      final var qualifierText = matcher.group(2);
      final Optional<ARI1VersionQualifier> qualifier;
      if (qualifierText != null) {
        qualifier = Optional.of(new ARI1VersionQualifier(qualifierText));
      } else {
        qualifier = Optional.empty();
      }

      return new ARI1Version(
        parseUnsignedInt(matcher.group(1)),
        0,
        0,
        qualifier
      );
    } catch (final Exception e) {
      throw new ARI1VersionException(
        "Version text '%s' cannot be parsed: %s"
          .formatted(text, e.getMessage()),
        e
      );
    }
  }

  private static ARI1Version parseMajorMinor(
    final String text,
    final Matcher matcher)
    throws ARI1VersionException
  {
    try {
      final var qualifierText = matcher.group(3);
      final Optional<ARI1VersionQualifier> qualifier;
      if (qualifierText != null) {
        qualifier = Optional.of(new ARI1VersionQualifier(qualifierText));
      } else {
        qualifier = Optional.empty();
      }

      return new ARI1Version(
        parseUnsignedInt(matcher.group(1)),
        parseUnsignedInt(matcher.group(2)),
        0,
        qualifier
      );
    } catch (final Exception e) {
      throw new ARI1VersionException(
        "Version text '%s' cannot be parsed: %s"
          .formatted(text, e.getMessage()),
        e
      );
    }
  }
}
