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

import com.io7m.aradine.api.ARException;
import com.io7m.lanark.core.RDottedName;
import com.io7m.verona.core.Version;
import com.io7m.verona.core.VersionException;
import com.io7m.verona.core.VersionParser;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The identifier of a sample map.
 *
 * @param group   The group
 * @param name    The name
 * @param version The version
 */

public record ARSampleMapID(
  RDottedName group,
  RDottedName name,
  Version version)
  implements Comparable<ARSampleMapID>
{
  /**
   * The identifier of a sample map.
   *
   * @param group   The group
   * @param name    The name
   * @param version The version
   */

  public ARSampleMapID
  {
    Objects.requireNonNull(group, "Group");
    Objects.requireNonNull(name, "Name");
    Objects.requireNonNull(version, "Version");
  }

  /**
   * Parse a sample map ID.
   *
   * @param text The text
   *
   * @return A sample map ID
   *
   * @throws ARException On errors
   */

  public static ARSampleMapID parse(
    final String text)
    throws ARException
  {
    final var segments = List.of(text.split(":"));
    if (segments.size() == 3) {
      try {
        final var group =
          new RDottedName(segments.get(0));
        final var name =
          new RDottedName(segments.get(1));
        final var version =
          VersionParser.parse(segments.get(2));

        return new ARSampleMapID(group, name, version);
      } catch (final VersionException e) {
        throw new ARException(
          e.getMessage(),
          e,
          "error-parse",
          Map.of("Text", text),
          Optional.empty()
        );
      }
    }

    throw new ARException(
      "Unparseable sample map ID.",
      "error-parse",
      Map.of("Text", text),
      Optional.empty()
    );
  }

  @Override
  public String toString()
  {
    return "%s:%s:%s".formatted(this.group, this.name, this.version);
  }

  @Override
  public int compareTo(
    final ARSampleMapID other)
  {
    return Comparator.comparing(ARSampleMapID::group)
      .thenComparing(ARSampleMapID::name)
      .thenComparing(ARSampleMapID::version)
      .compare(this, other);
  }
}
