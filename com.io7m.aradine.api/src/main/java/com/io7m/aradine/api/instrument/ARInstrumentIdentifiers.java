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

package com.io7m.aradine.api.instrument;

import com.io7m.lanark.core.RDottedName;
import com.io7m.verona.core.Version;

/**
 * Instrument identifiers.
 */

public final class ARInstrumentIdentifiers
{
  private static final ARInstrumentID SYSTEM_SOURCE =
    new ARInstrumentID(
      new RDottedName("com.io7m.aradine"),
      new RDottedName("com.io7m.aradine.system.source"),
      Version.of(1, 0, 0)
    );

  private static final ARInstrumentID SYSTEM_TARGET =
    new ARInstrumentID(
      new RDottedName("com.io7m.aradine"),
      new RDottedName("com.io7m.aradine.system.target"),
      Version.of(1, 0, 0)
    );

  private ARInstrumentIdentifiers()
  {

  }

  /**
   * @return The identifier of the system source
   */

  public static ARInstrumentID systemSource()
  {
    return SYSTEM_SOURCE;
  }

  /**
   * @return The identifier of the system target
   */

  public static ARInstrumentID systemTarget()
  {
    return SYSTEM_TARGET;
  }
}
