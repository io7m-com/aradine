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

package com.io7m.aradine.audiosystem.zero.internal;

import com.io7m.aradine.audiosystem.spi.ARAudioSystemBackendType;
import com.io7m.lanark.core.RDottedName;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The no-op audio backend.
 */

public final class ARAudioBackendZero
  implements ARAudioSystemBackendType
{
  private static final RDottedName NAME =
    new RDottedName("com.io7m.aradine.audiosystem.zero");

  private final AtomicBoolean closed;

  /**
   * The no-op audio backend.
   */

  public ARAudioBackendZero()
  {
    this.closed = new AtomicBoolean(false);
  }

  @Override
  public RDottedName name()
  {
    return NAME;
  }

  @Override
  public void close()
  {
    if (this.closed.compareAndSet(false, true)) {
      // Nothing.
    }
  }

  @Override
  public boolean isClosed()
  {
    return this.closed.get();
  }
}
