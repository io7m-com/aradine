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

package com.io7m.aradine.tests.audiosystem.jnajack;

import com.io7m.aradine.api.audiosystem.ARAudioSystemAttributes;
import com.io7m.aradine.audiosystem.jnajack.ARAudioBackendJNAJackProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

public final class ARAudioBackendJNAJackTest
{
  private static boolean runningOnCI()
  {
    return System.getenv("CI") != null;
  }

  @Test
  public void testOpenClose()
  {
    Assumptions.assumeFalse(runningOnCI());
    Assertions.assertTimeout(
      Duration.ofSeconds(10L),
      () -> {
        final var provider =
          new ARAudioBackendJNAJackProvider();

        try (var _ = provider.openBackend(
          new ARAudioSystemAttributes(),
          Map.ofEntries(
            Map.entry("ApplicationName", "Aradine"),
            Map.entry("PortSourceCount", "16"),
            Map.entry("PortTargetCount", "16")
          )
        )) {

        }
      });
  }
}
