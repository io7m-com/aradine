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

package com.io7m.aradine.audiosystem.spi;

import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.audiosystem.ARAudioSystemAttributes;
import com.io7m.lanark.core.RDottedName;

import java.util.Map;

/**
 * A provider of audio system backends.
 */

public interface ARAudioSystemBackendProviderType
{
  /**
   * @return The fully-qualified name of the provider
   */

  RDottedName name();

  /**
   * Determine if a provider is supported with the given configuration.
   *
   * @param configuration The configuration
   *
   * @return {@code true} iff the provider is supported
   *
   * @throws ARException On errors
   */

  boolean isSupported(
    Map<String, String> configuration)
    throws ARException;

  /**
   * Open a new backend.
   *
   * @param attributes    The audio system attributes
   * @param configuration The configuration
   *
   * @return The new backend
   *
   * @throws ARException On errors
   */

  ARAudioSystemBackendType openBackend(
    ARAudioSystemAttributes attributes,
    Map<String, String> configuration)
    throws ARException;
}
