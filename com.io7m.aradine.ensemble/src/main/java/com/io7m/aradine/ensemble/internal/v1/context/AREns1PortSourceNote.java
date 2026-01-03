/*
 * Copyright © 2026 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

package com.io7m.aradine.ensemble.internal.v1.context;

import com.io7m.aradine.instrument.spi1.ARI1PortNumber;
import com.io7m.aradine.instrument.spi1.ARI1PortSourceNoteType;

import java.util.Objects;

/**
 * A source note port.
 */

public final class AREns1PortSourceNote
  implements ARI1PortSourceNoteType
{
  private final AREns1InstrumentContext context;
  private final ARI1PortNumber portNumber;

  AREns1PortSourceNote(
    final AREns1InstrumentContext inContext,
    final ARI1PortNumber inPortNumber)
  {
    this.context =
      Objects.requireNonNull(inContext, "Context");
    this.portNumber =
      Objects.requireNonNull(inPortNumber, "PortNumber");
  }

  @Override
  public ARI1PortNumber id()
  {
    return this.portNumber;
  }
}
