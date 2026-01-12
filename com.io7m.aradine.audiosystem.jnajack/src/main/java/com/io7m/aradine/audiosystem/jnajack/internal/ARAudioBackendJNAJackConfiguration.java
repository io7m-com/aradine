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

package com.io7m.aradine.audiosystem.jnajack.internal;

import com.io7m.aradine.api.ARException;

import java.util.Map;
import java.util.Objects;

/**
 * The backend configuration.
 *
 * @param applicationName The JACK application name
 * @param portSourceCount The number of source ports to register
 * @param portTargetCount The number of target ports to register
 */

public record ARAudioBackendJNAJackConfiguration(
  String applicationName,
  int portSourceCount,
  int portTargetCount)
{
  /**
   * The backend configuration.
   *
   * @param applicationName The JACK application name
   * @param portSourceCount The number of source ports to register
   * @param portTargetCount The number of target ports to register
   */

  public ARAudioBackendJNAJackConfiguration
  {
    Objects.requireNonNull(applicationName, "ApplicationName");
    Objects.checkIndex(portSourceCount, Integer.MAX_VALUE);
    Objects.checkIndex(portTargetCount, Integer.MAX_VALUE);
  }

  /**
   * Parse configuration data from the given map.
   *
   * @param data The map
   *
   * @return The configuration
   *
   * @throws ARException On errors
   */

  public static ARAudioBackendJNAJackConfiguration parse(
    final Map<String, String> data)
    throws ARException
  {
    try {
      final var applicationName =
        Objects.requireNonNull(
          data.get("ApplicationName"),
          "ApplicationName"
        );

      final var portSourceCount =
        Integer.parseUnsignedInt(data.get("PortSourceCount"));

      final var portTargetCount =
        Integer.parseUnsignedInt(data.get("PortTargetCount"));

      return new ARAudioBackendJNAJackConfiguration(
        applicationName,
        portSourceCount,
        portTargetCount
      );
    } catch (final Throwable e) {
      throw ARAudioBackendJNAJackExceptions.wrap(e);
    }
  }
}
