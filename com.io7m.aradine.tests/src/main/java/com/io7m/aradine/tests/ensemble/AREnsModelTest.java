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

package com.io7m.aradine.tests.ensemble;

import com.io7m.aradine.api.instrument.ARInstrumentInstanceID;
import com.io7m.aradine.api.progress.ARProgress;
import com.io7m.aradine.ensemble.internal.events.AREnsEventInstrumentClosed;
import com.io7m.aradine.ensemble.internal.events.AREnsEventInstrumentLoaded;
import com.io7m.aradine.ensemble.internal.events.AREnsEventType;
import com.io7m.aradine.ensemble.internal.model.AREnsModel;
import com.io7m.aradine.ensemble.internal.model.AREnsModelConfiguration;
import com.io7m.aradine.ensemble.internal.v1.commands.AREnsModelCommandInstrumentLoad;
import com.io7m.aradine.ensemble.internal.v1.commands.AREnsModelCommandInstrumentLoadParameters;
import com.io7m.aradine.instrument.loader.ARInstrumentLoaders;
import com.io7m.aradine.instrument.loader.ARInstrumentReaders;
import com.io7m.aradine.inventory.ARInventories;
import com.io7m.aradine.inventory.api.ARInventoryConfiguration;
import com.io7m.aradine.inventory.api.ARInventoryType;
import com.io7m.aradine.tests.ARAudioSystemAttributes;
import com.io7m.aradine.tests.ARFunctionSubscriber;
import com.io7m.aradine.tests.inventory.ARInventoryTest;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public final class AREnsModelTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(AREnsModelTest.class);

  private Path directory;
  private Path dataDirectory;
  private Path databaseFile;
  private ARInventoryConfiguration inventoryConfiguration;
  private ARInventoryType inventory;
  private ARInstrumentLoaders loaders;
  private ARAudioSystemAttributes audioSystem;
  private ConcurrentLinkedQueue<AREnsEventType> events;

  private static void logProgress(
    final ARProgress progress)
  {
    LOG.debug("Progress: {}", progress);
  }

  @BeforeEach
  public void setup()
    throws Exception
  {
    this.directory =
      Files.createTempDirectory("aradine");
    this.dataDirectory =
      Files.createTempDirectory("aradine");
    this.databaseFile =
      this.directory.resolve("database.db");

    this.dataDirectory =
      this.directory.resolve("data");

    this.inventoryConfiguration =
      ARInventoryConfiguration.builder()
        .setDataDirectory(this.dataDirectory)
        .setDatabaseFile(this.databaseFile)
        .setInstrumentReaders(new ARInstrumentReaders())
        .build();

    Files.createDirectories(this.directory);
    Files.createDirectories(this.dataDirectory);

    this.inventory =
      ARInventories.open(this.inventoryConfiguration);
    this.loaders =
      new ARInstrumentLoaders();
    this.audioSystem =
      new ARAudioSystemAttributes();
    this.events =
      new ConcurrentLinkedQueue<AREnsEventType>();
  }

  @AfterEach
  public void tearDown()
    throws Exception
  {
    this.inventory.close();

    try {
      FileUtils.deleteDirectory(this.dataDirectory.toFile());
    } catch (final Throwable e) {
      // Don't care
    }

    try {
      FileUtils.deleteDirectory(this.directory.toFile());
    } catch (final Throwable e) {
      // Don't care
    }
  }

  @Test
  public void testOpenCloseEmpty()
    throws Exception
  {
    final var configuration =
      new AREnsModelConfiguration(
        this.directory.resolve("file.aens"),
        this.inventory,
        this.loaders,
        this.audioSystem
      );

    try (var model = AREnsModel.open(configuration)) {
      model.loading().get();
    }
  }

  @Test
  public void testInstrumentRegister()
    throws Exception
  {
    final var samplerFile =
      this.resourceOf("sampler_m0.jar");

    final var instrumentID =
      this.inventory.instrumentInstall(
        samplerFile, AREnsModelTest::logProgress).get();

    final var configuration =
      new AREnsModelConfiguration(
        this.directory.resolve("file.aens"),
        this.inventory,
        this.loaders,
        this.audioSystem
      );

    try (var model = AREnsModel.open(configuration)) {
      model.events().subscribe(new ARFunctionSubscriber<>(this::logEvent));
      model.loading().get();
      model.executeCommand(
        AREnsModelCommandInstrumentLoad.INSTANCE,
        new AREnsModelCommandInstrumentLoadParameters(
          ARInstrumentInstanceID.random(),
          instrumentID
        )
      ).get();
      model.undo().get();
      model.redo().get();
      model.undo().get();
    }

    assertInstanceOf(AREnsEventInstrumentLoaded.class, this.events.poll());
    assertInstanceOf(AREnsEventInstrumentClosed.class, this.events.poll());
    assertInstanceOf(AREnsEventInstrumentLoaded.class, this.events.poll());
    assertInstanceOf(AREnsEventInstrumentClosed.class, this.events.poll());
  }

  private void logEvent(
    final AREnsEventType event)
  {
    LOG.debug("Event: {}", event);
    this.events.add(event);
  }

  private Path resourceOf(
    final String name)
    throws IOException
  {
    final var path =
      "/com/io7m/aradine/tests/%s".formatted(name);
    final var url =
      ARInventoryTest.class.getResource(path);

    Objects.requireNonNull(url, "URL");
    try (var stream = url.openStream()) {
      final var output = this.directory.resolve(name);
      Files.copy(stream, output, StandardCopyOption.REPLACE_EXISTING);
      return output;
    }
  }
}
