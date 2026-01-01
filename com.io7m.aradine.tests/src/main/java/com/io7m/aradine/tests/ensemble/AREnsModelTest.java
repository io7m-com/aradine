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

import com.io7m.aradine.database.api.ARDBConfiguration;
import com.io7m.aradine.database.api.ARDBType;
import com.io7m.aradine.database.sqlite3.ARDBFactory;
import com.io7m.aradine.ensemble.internal.model.AREnsModel;
import com.io7m.aradine.instrument.loader.ARInstrumentReaders;
import com.io7m.aradine.inventory.ARInventories;
import com.io7m.aradine.inventory.api.ARInventoryConfiguration;
import com.io7m.aradine.inventory.api.ARInventoryType;
import com.io7m.lanark.core.RDottedName;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AREnsModelTest
{
  private Path directory;
  private Path dataDirectory;
  private Path databaseFile;
  private ARDBType database;
  private ExecutorService databaseExecutor;
  private ARInventoryConfiguration inventoryConfiguration;
  private ARInventoryType inventory;

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

    this.inventory =
      ARInventories.open(this.inventoryConfiguration);
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

    this.databaseExecutor.close();
  }

  @Test
  public void testOpenClose()
    throws Exception
  {
    final var file = this.directory.resolve("file.aens");
    try (var model = AREnsModel.open(this.inventory, file)) {

    }
  }
}
