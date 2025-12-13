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

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A description of an note-typed input port.
 *
 * @param id            The port ID
 * @param label         The port label
 * @param semantics     The port semantics
 * @param documentation The port documentation
 */

public record ARI1PortDescriptionInputNote(
  ARI1PortId id,
  String label,
  Set<String> semantics,
  Optional<ARI1Documentation> documentation)
  implements ARI1PortDescriptionInputType
{
  /**
   * A description of an note-typed input port.
   *
   * @param id            The port ID
   * @param label         The port label
   * @param semantics     The port semantics
   * @param documentation The port documentation
   */

  public ARI1PortDescriptionInputNote
  {
    Objects.requireNonNull(id, "ID");
    Objects.requireNonNull(label, "Label");
    Objects.requireNonNull(documentation, "Documentation");
    semantics = Set.copyOf(semantics);
  }
}
