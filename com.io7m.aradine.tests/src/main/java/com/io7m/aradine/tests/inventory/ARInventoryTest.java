/*
 * Copyright © 2025 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

package com.io7m.aradine.tests.inventory;

import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.ARHash;
import com.io7m.aradine.api.instrument.ARInstrumentID;
import com.io7m.aradine.database.api.ARDBConfiguration;
import com.io7m.aradine.database.api.ARDBType;
import com.io7m.aradine.database.sqlite3.ARDBFactory;
import com.io7m.aradine.instrument.loader.ARInstrumentReaders;
import com.io7m.aradine.inventory.ARInventories;
import com.io7m.aradine.inventory.api.ARInventoryConfiguration;
import com.io7m.aradine.inventory.api.ARInventoryType;
import com.io7m.aradine.inventory.api.queries.ARQueryBlobGetType;
import com.io7m.aradine.inventory.api.queries.ARQueryInstrumentGetType;
import com.io7m.lanark.core.RDottedName;
import com.io7m.mime2045.core.MimeType;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.io7m.aradine.api.ARHashAlgorithm.SHA_256;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ARInventoryTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ARInventoryTest.class);

  private Path databaseFile;
  private Path dataDirectory;
  private ARInventoryConfiguration inventoryConfiguration;
  private Path directory;
  private ARDBType database;
  private ExecutorService databaseExecutor;

  private static ARException runFailure(
    final ARInventoryType inventory,
    final Path file)
  {
    LOG.debug("Installing instrument...");

    final var cex =
      assertThrows(
        ExecutionException.class, () -> {
          inventory.instrumentInstall(
            file,
            progress -> LOG.debug("{}", progress)
          ).get();
        });

    final var ex =
      assertInstanceOf(ARException.class, cex.getCause());
    LOG.debug("", ex);
    return ex;
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
    this.database =
      new ARDBFactory()
        .open(
          ARDBConfiguration.builder()
            .addAllQueries(ARInventories.queries())
            .setApplicationId(0x10203040)
            .setApplicationIdText(new RDottedName("com.io7m.aradine.example"))
            .setDatabaseFile(this.databaseFile)
            .build()
        );
    this.databaseExecutor =
      Executors.newSingleThreadExecutor();

    this.inventoryConfiguration =
      ARInventoryConfiguration.builder()
        .setDatabase(this.database)
        .setDataDirectory(this.dataDirectory)
        .setDatabaseExecutor(this.databaseExecutor)
        .setReaders(new ARInstrumentReaders())
        .build();

    Files.createDirectories(this.directory);
    Files.createDirectories(this.dataDirectory);
  }

  @AfterEach
  public void tearDown()
  {
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

    this.databaseExecutor.close();
  }

  @Test
  public void testBlobInstall()
    throws Exception
  {
    final var file = this.dataDirectory.resolve("file.txt");
    Files.writeString(file, "HELLO");

    try (var inventory = ARInventories.open(this.inventoryConfiguration)) {
      LOG.debug("Installing blob...");
      inventory.blobInstall(
        file,
        MimeType.of("text", "plain"),
        progress -> LOG.debug("{}", progress)
      ).get();
    }

    LOG.debug("Checking database...");
    try (var inventory = ARInventories.open(this.inventoryConfiguration)) {
      final var database = inventory.database();
      final var transaction = database.openTransaction();
      final var blob =
        transaction.execute(
          ARQueryBlobGetType.class,
          new ARHash(
            SHA_256,
            "3733cd977ff8eb18b987357e22ced99f46097f31ecb239e878ae63760e83e4d5")
        );
      assertTrue(blob.isPresent());
    }
  }

  @Test
  public void testInstrumentInstall()
    throws Exception
  {
    final var file =
      this.resourceOf("sampler_m0.jar");

    final ARInstrumentID instrumentID;
    try (var inventory = ARInventories.open(this.inventoryConfiguration)) {
      LOG.debug("Installing instrument...");
      instrumentID =
        inventory.instrumentInstall(
          file,
          progress -> LOG.debug("{}", progress)
        ).get();
    }

    LOG.debug("Checking database...");
    try (var inventory = ARInventories.open(this.inventoryConfiguration)) {
      final var database = inventory.database();
      final var transaction = database.openTransaction();
      final var instrument =
        transaction.execute(ARQueryInstrumentGetType.class, instrumentID)
          .orElseThrow();
      assertEquals(instrumentID, instrument.identifier());
    }
  }

  @Test
  public void testInstrumentInstallNoManifest()
    throws Exception
  {
    final var file =
      this.resourceOf("sampler_m0-no_manifest.jar");

    try (var inventory = ARInventories.open(this.inventoryConfiguration)) {
      final var ex = runFailure(inventory, file);
      assertEquals("error-instrument-manifest-missing", ex.errorCode());
    }
  }

  @Test
  public void testInstrumentInstallNoManifestAradine()
    throws Exception
  {
    final var file =
      this.resourceOf("sampler_m0-no_manifest_aradine.jar");

    try (var inventory = ARInventories.open(this.inventoryConfiguration)) {
      final var ex = runFailure(inventory, file);
      assertEquals("error-manifest-missing-aradine-instrument", ex.errorCode());
    }
  }

  @Test
  public void testInstrumentInstallInstrumentMissing()
    throws Exception
  {
    final var file =
      this.resourceOf("sampler_m0-missing_instrument.jar");

    try (var inventory = ARInventories.open(this.inventoryConfiguration)) {
      final var ex = runFailure(inventory, file);
      assertEquals("error-manifest-nonexistent-instrument", ex.errorCode());
    }
  }

  @Test
  public void testInstrumentInstallInstrumentInvalid()
    throws Exception
  {
    final var file =
      this.resourceOf("sampler_m0-invalid_instrument.jar");

    try (var inventory = ARInventories.open(this.inventoryConfiguration)) {
      final var ex = runFailure(inventory, file);
      assertEquals("error-invalid-instrument", ex.errorCode());
    }
  }

  @Test
  public void testInstrumentInstallInstrumentUnsupported()
    throws Exception
  {
    final var file =
      this.resourceOf("sampler_m0-unsupported_instrument.jar");

    try (var inventory = ARInventories.open(this.inventoryConfiguration)) {
      final var ex = runFailure(inventory, file);
      assertEquals("error-unsupported-schema-version", ex.errorCode());
    }
  }

  @Test
  public void testInstrumentInstallCorruptJSON()
    throws Exception
  {
    final var file =
      this.resourceOf("sampler_m0-corrupt_json.jar");

    try (var inventory = ARInventories.open(this.inventoryConfiguration)) {
      final var ex = runFailure(inventory, file);
      assertEquals("error-json", ex.errorCode());
    }
  }

  @Test
  public void testInstrumentInstallMaliciousInstrument()
    throws Exception
  {
    final var file =
      this.resourceOf("sampler_m0-malicious_instrument.jar");

    try (var inventory = ARInventories.open(this.inventoryConfiguration)) {
      final var ex = runFailure(inventory, file);
      assertEquals("error-parsing", ex.errorCode());
    }
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
