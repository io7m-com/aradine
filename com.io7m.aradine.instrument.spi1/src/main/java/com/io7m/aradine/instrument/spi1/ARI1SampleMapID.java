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

package com.io7m.aradine.instrument.spi1;

import java.util.Comparator;
import java.util.Objects;

/**
 * The identifier of a sample map.
 *
 * @param group   The group
 * @param name    The name
 * @param version The version
 */

public record ARI1SampleMapID(
  ARI1DottedName group,
  ARI1DottedName name,
  ARI1Version version)
  implements Comparable<ARI1SampleMapID>
{
  /**
   * The identifier of a sample map.
   *
   * @param group   The group
   * @param name    The name
   * @param version The version
   */

  public ARI1SampleMapID
  {
    Objects.requireNonNull(group, "Group");
    Objects.requireNonNull(name, "Name");
    Objects.requireNonNull(version, "Version");
  }

  @Override
  public String toString()
  {
    return "%s:%s:%s".formatted(this.group, this.name, this.version);
  }

  @Override
  public int compareTo(
    final ARI1SampleMapID other)
  {
    return Comparator.comparing(ARI1SampleMapID::group)
      .thenComparing(ARI1SampleMapID::name)
      .thenComparing(ARI1SampleMapID::version)
      .compare(this, other);
  }
}
