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

package com.io7m.aradine.api.audiosystem;

import com.io7m.aradine.api.ARException;
import com.io7m.lanark.core.RDottedName;

import java.util.Map;
import java.util.Objects;

/**
 * An audio system.
 */

public interface ARAudioSystemType
  extends AutoCloseable, ARAudioSystemUsableType
{
  /**
   * Open a backend, replacing any existing backend.
   *
   * @param name          The backend name
   * @param configuration The backend configuration
   *
   * @throws ARException On errors
   */

  void openBackend(
    RDottedName name,
    Map<String, String> configuration)
    throws ARException;

  /**
   * Open a backend, replacing any existing backend.
   *
   * @param name          The backend name
   * @param configuration The backend configuration
   *
   * @throws ARException On errors
   */

  default void openBackend(
    final String name,
    final Map<String, String> configuration)
    throws ARException
  {
    Objects.requireNonNull(name, "Name");
    Objects.requireNonNull(configuration, "Configuration");

    this.openBackend(new RDottedName(name), configuration);
  }

  @Override
  void close()
    throws ARException;
}
