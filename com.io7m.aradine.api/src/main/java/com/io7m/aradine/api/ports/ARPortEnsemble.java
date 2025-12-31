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

package com.io7m.aradine.api.ports;

import java.util.Objects;
import java.util.Set;

/**
 * An instrument port.
 *
 * @param id        The port ID
 * @param number    The port number
 * @param kind      The port kind
 * @param direction The port direction
 * @param label     The port label
 * @param semantics The port semantics
 */

public record ARPortEnsemble(
  ARPortID id,
  ARPortKind kind,
  ARPortDirection direction,
  ARPortNumber number,
  String label,
  Set<String> semantics)
  implements ARPortType
{
  /**
   * An ensemble port.
   *
   * @param id        The port ID
   * @param number    The port number
   * @param kind      The port kind
   * @param direction The port direction
   * @param label     The port label
   * @param semantics The port semantics
   */

  public ARPortEnsemble
  {
    Objects.requireNonNull(id, "ID");
    Objects.requireNonNull(kind, "Kind");
    Objects.requireNonNull(direction, "Direction");
    Objects.requireNonNull(number, "Number");
    Objects.requireNonNull(label, "Label");
    semantics = Set.copyOf(semantics);
  }

  @Override
  public ARPortCategory category()
  {
    return ARPortCategory.AR_ENSEMBLE;
  }
}
