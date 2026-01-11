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

package com.io7m.aradine.api.sample_map;

import com.io7m.lanark.core.RDottedName;
import com.io7m.verona.core.Version;

/**
 * Sample map identifiers.
 */

public final class ARSampleMapIdentifiers
{
  private static final ARSampleMapID EMPTY =
    new ARSampleMapID(
      new RDottedName("com.io7m.aradine"),
      new RDottedName("com.io7m.aradine.empty"),
      Version.of(1, 0, 0)
    );

  private static final ARSampleMapInstanceID EMPTY_INSTANCE_ID =
    ARSampleMapInstanceID.ofString("00000000-0000-0000-0000-000000000000");

  private ARSampleMapIdentifiers()
  {

  }

  /**
   * @return The identifier of the empty map
   */

  public static ARSampleMapInstanceID emptyInstanceID()
  {
    return EMPTY_INSTANCE_ID;
  }

  /**
   * @return The identifier of the empty sample map
   */

  public static ARSampleMapID empty()
  {
    return EMPTY;
  }
}
