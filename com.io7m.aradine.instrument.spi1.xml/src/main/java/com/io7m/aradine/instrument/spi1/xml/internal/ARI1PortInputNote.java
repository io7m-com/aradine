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


package com.io7m.aradine.instrument.spi1.xml.internal;

import com.io7m.aradine.instrument.spi1.ARI1PortDescriptionInputNoteType;
import com.io7m.aradine.instrument.spi1.ARI1PortId;

import java.util.Objects;
import java.util.Set;

/**
 * The description of a note input port.
 *
 * @param id        The port ID
 * @param semantics The port semantics
 * @param label     The port label
 */

public record ARI1PortInputNote(
  ARI1PortId id,
  Set<String> semantics,
  String label)
  implements ARI1PortDescriptionInputNoteType
{
  /**
   * The description of a note input port.
   *
   * @param id        The port ID
   * @param semantics The port semantics
   * @param label     The port label
   */

  public ARI1PortInputNote
  {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(semantics, "semantics");
    Objects.requireNonNull(label, "label");
  }
}
