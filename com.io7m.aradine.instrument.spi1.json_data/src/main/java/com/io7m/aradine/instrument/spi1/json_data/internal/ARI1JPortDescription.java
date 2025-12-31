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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A description of a port used to supply data to, or extract data from, an instrument.
 *
 * @param number        The port number
 * @param kind          The port kind
 * @param direction     The port direction
 * @param label         The port label
 * @param semantics     The port semantics
 * @param documentation The documentation
 */

public record ARI1JPortDescription(
  @JsonProperty(value = "Kind", required = true)
  ARI1JPortKind kind,
  @JsonProperty(value = "Direction", required = true)
  ARI1JPortDirection direction,
  @JsonProperty(value = "Number", required = true)
  ARI1JPortNumber number,
  @JsonProperty(value = "Label", required = true)
  String label,
  @JsonProperty(value = "Semantics")
  Set<String> semantics,
  @JsonProperty(value = "Documentation")
  Optional<ARI1JDocumentation> documentation)
{
  /**
   * A description of a port used to supply data to, or extract data from, an instrument.
   *
   * @param number        The port number
   * @param kind          The port kind
   * @param direction     The port direction
   * @param label         The port label
   * @param semantics     The port semantics
   * @param documentation The documentation
   */

  public ARI1JPortDescription
  {
    Objects.requireNonNull(kind, "Kind");
    Objects.requireNonNull(direction, "Direction");
    Objects.requireNonNull(number, "Number");
    Objects.requireNonNull(label, "Label");
    semantics = Set.copyOf(semantics);
  }
}
