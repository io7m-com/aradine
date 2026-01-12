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

package com.io7m.aradine.audiosystem.main;

import com.io7m.aradine.api.ARCloseables;
import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.audiosystem.ARAudioSystemAttributes;
import com.io7m.aradine.api.audiosystem.ARAudioSystemEventBackendClosed;
import com.io7m.aradine.api.audiosystem.ARAudioSystemEventBackendOpened;
import com.io7m.aradine.api.audiosystem.ARAudioSystemEventType;
import com.io7m.aradine.api.audiosystem.ARAudioSystemType;
import com.io7m.aradine.audiosystem.spi.ARAudioSystemBackendProviderType;
import com.io7m.aradine.audiosystem.spi.ARAudioSystemBackendType;
import com.io7m.jmulticlose.core.CloseableTrackerType;
import com.io7m.lanark.core.RDottedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The main audio system implementation.
 */

public final class ARAudioSystem implements ARAudioSystemType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ARAudioSystem.class);

  private final SubmissionPublisher<ARAudioSystemEventType> events;
  private final ARAudioSystemAttributes attributes;
  private final CloseableTrackerType<ARException> resources;
  private final List<ARAudioSystemBackendProviderType> backendProviders;
  private ARAudioSystemBackendType backend;

  private ARAudioSystem(
    final CloseableTrackerType<ARException> inResources,
    final List<ARAudioSystemBackendProviderType> inBackendProviders)
  {
    this.resources =
      Objects.requireNonNull(inResources, "Resources");
    this.events =
      new SubmissionPublisher<>(Runnable::run, 1);
    this.attributes =
      new ARAudioSystemAttributes();
    this.backendProviders =
      List.copyOf(inBackendProviders);
  }

  /**
   * Open an audio system.
   *
   * @param backends The backends
   *
   * @return An audio system
   *
   * @throws ARException On errors
   */

  public static ARAudioSystemType open(
    final List<ARAudioSystemBackendProviderType> backends)
    throws ARException
  {
    Objects.requireNonNull(backends, "Backends");
    logBackends(backends);
    checkBackendNames(backends);

    final var resources =
      ARCloseables.createTracked();

    try {
      return new ARAudioSystem(resources, backends);
    } catch (final Throwable e) {
      resources.close();
      throw wrap(e);
    }
  }

  private static void logBackends(
    final List<ARAudioSystemBackendProviderType> backends)
  {
    if (LOG.isDebugEnabled()) {
      for (int index = 0; index < backends.size(); ++index) {
        LOG.debug(
          "Audio backend [{}]: {}",
          Integer.valueOf(index),
          backends.get(index).name()
        );
      }
    }
  }

  private static void checkBackendNames(
    final List<ARAudioSystemBackendProviderType> backends)
    throws ARException
  {
    try {
      backends.stream()
        .collect(
          Collectors.toMap(
            ARAudioSystemBackendProviderType::name,
            Function.identity()
          )
        );
    } catch (final IllegalStateException e) {
      throw errorDuplicateBackendProviderName(e);
    }
  }

  private static ARException errorDuplicateBackendProviderName(
    final IllegalStateException e)
  {
    return new ARException(
      "Multiple audio backend providers with the same name.",
      e,
      "error-audiosystem-backend-name-conflict",
      Map.of(),
      Optional.empty()
    );
  }

  /**
   * Open an audio system, loading backends from {@link ServiceLoader}.
   *
   * @return An audio system
   *
   * @throws ARException On errors
   */

  public static ARAudioSystemType open()
    throws ARException
  {
    return open(
      ServiceLoader.load(ARAudioSystemBackendProviderType.class)
        .stream()
        .map(ServiceLoader.Provider::get)
        .toList()
    );
  }

  private static ARException wrap(
    final Throwable e)
  {
    return switch (e) {
      case final ARException x -> x;
      case final Throwable x -> {
        yield new ARException(
          x.getMessage(),
          x,
          "error-exception",
          Map.of(),
          Optional.empty()
        );
      }
    };
  }

  private static ARException errorNoSuchBackend(
    final RDottedName name)
  {
    return new ARException(
      "No such audio backend.",
      "error-audiosystem-backend-nonexistent",
      Map.ofEntries(
        Map.entry("Backend", name.value())
      ),
      Optional.of(
        "Specify the name of a backend that actually exists."
      )
    );
  }

  @Override
  public Flow.Publisher<ARAudioSystemEventType> events()
  {
    return this.events;
  }

  @Override
  public ARAudioSystemAttributes attributes()
  {
    return this.attributes;
  }

  @Override
  public void openBackend(
    final RDottedName name,
    final Map<String, String> configuration)
    throws ARException
  {
    Objects.requireNonNull(name, "Name");
    Objects.requireNonNull(configuration, "Configuration");

    for (final var provider : this.backendProviders) {
      if (Objects.equals(provider.name(), name)) {
        this.openBackendNow(provider, configuration);
        return;
      }
    }

    throw errorNoSuchBackend(name);
  }

  private void openBackendNow(
    final ARAudioSystemBackendProviderType provider,
    final Map<String, String> configuration)
    throws ARException
  {
    final var existing = this.backend;
    if (existing != null) {
      final var name = existing.name();
      existing.close();
      this.events.submit(new ARAudioSystemEventBackendClosed(name));
    }

    this.backend =
      this.resources.add(provider.openBackend(this.attributes, configuration));
    this.events.submit(
      new ARAudioSystemEventBackendOpened(provider.name()));
  }

  @Override
  public void close()
    throws ARException
  {
    this.resources.close();
  }
}
