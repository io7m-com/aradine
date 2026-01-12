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

package com.io7m.aradine.tests.audiosystem;

import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.audiosystem.ARAudioSystemEventBackendClosed;
import com.io7m.aradine.api.audiosystem.ARAudioSystemEventBackendOpened;
import com.io7m.aradine.api.audiosystem.ARAudioSystemEventType;
import com.io7m.aradine.audiosystem.jnajack.ARAudioBackendJNAJackProvider;
import com.io7m.aradine.audiosystem.main.ARAudioSystem;
import com.io7m.aradine.audiosystem.spi.ARAudioSystemBackendProviderType;
import com.io7m.aradine.audiosystem.zero.ARAudioBackendZeroProvider;
import com.io7m.aradine.tests.ARFunctionSubscriber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class ARAudioSystemTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ARAudioSystemTest.class);

  private ArrayList<ARAudioSystemEventType> events;

  private static boolean runningOnCI()
  {
    return System.getenv("CI") != null;
  }

  private void logEvent(
    final ARAudioSystemEventType event)
  {
    LOG.debug("Event: {}", event);
    this.events.add(event);
  }

  @BeforeEach
  public void setup()
  {
    this.events = new ArrayList<>();
  }

  @Test
  public void testOpenCloseJack()
  {
    Assumptions.assumeFalse(runningOnCI());
    Assertions.assertTimeout(
      Duration.ofSeconds(10L),
      () -> {
        final var provider =
          new ARAudioBackendJNAJackProvider();
        final List<ARAudioSystemBackendProviderType> providers =
          List.of(provider);
        final var configuration =
          Map.ofEntries(
            Map.entry("ApplicationName", "Aradine"),
            Map.entry("PortSourceCount", "16"),
            Map.entry("PortTargetCount", "16")
          );

        try (var system = ARAudioSystem.open(providers)) {
          system.events().subscribe(new ARFunctionSubscriber<>(this::logEvent));
          system.openBackend(provider.name(), configuration);
          system.openBackend(provider.name(), configuration);
        }

        assertInstanceOf(
          ARAudioSystemEventBackendOpened.class,
          this.events.get(0)
        );
        assertInstanceOf(
          ARAudioSystemEventBackendClosed.class,
          this.events.get(1)
        );
        assertInstanceOf(
          ARAudioSystemEventBackendOpened.class,
          this.events.get(2)
        );
      });
  }

  @Test
  public void testOpenCloseZero()
    throws Exception
  {
    final var provider =
      new ARAudioBackendZeroProvider();
    final List<ARAudioSystemBackendProviderType> providers =
      List.of(provider);
    final var configuration =
      Map.ofEntries(
        Map.entry("ApplicationName", "Aradine"),
        Map.entry("PortSourceCount", "16"),
        Map.entry("PortTargetCount", "16")
      );

    try (var system = ARAudioSystem.open(providers)) {
      system.events().subscribe(new ARFunctionSubscriber<>(this::logEvent));
      system.openBackend(provider.name(), configuration);
      system.openBackend(provider.name(), configuration);
    }

    assertInstanceOf(
      ARAudioSystemEventBackendOpened.class,
      this.events.get(0)
    );
    assertInstanceOf(
      ARAudioSystemEventBackendClosed.class,
      this.events.get(1)
    );
    assertInstanceOf(
      ARAudioSystemEventBackendOpened.class,
      this.events.get(2)
    );
  }

  @Test
  public void testNonexistentName()
    throws ARException
  {
    final List<ARAudioSystemBackendProviderType> providers =
      List.of();

    try (var system = ARAudioSystem.open(providers)) {
      final var ex =
        assertThrows(
          ARException.class,
          () -> system.openBackend("x", Map.of()));
      assertEquals("error-audiosystem-backend-nonexistent", ex.errorCode());
    }
  }

  @Test
  public void testDuplicateName()
  {
    final var provider =
      new ARAudioBackendZeroProvider();
    final List<ARAudioSystemBackendProviderType> providers =
      List.of(provider, provider);

    final var ex =
      assertThrows(ARException.class, () -> ARAudioSystem.open(providers));
    assertEquals("error-audiosystem-backend-name-conflict", ex.errorCode());
  }
}
