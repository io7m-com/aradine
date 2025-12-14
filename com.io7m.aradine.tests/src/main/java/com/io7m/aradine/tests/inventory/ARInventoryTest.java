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

import com.io7m.aradine.inventory.ARInventories;
import com.io7m.aradine.inventory.api.ARInventoryConfiguration;
import com.io7m.aradine.inventory.api.ARInventoryHash;
import com.io7m.aradine.inventory.api.queries.ARQueryBlobGetType;
import com.io7m.mime2045.core.MimeType;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.io7m.aradine.inventory.api.ARInventoryHashAlgorithm.SHA_256;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ARInventoryTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ARInventoryTest.class);

  private Path databaseFile;
  private Path dataDirectory;
  private ARInventoryConfiguration inventoryConfiguration;
  private Path directory;

  @BeforeEach
  public void setup(
    final @TempDir Path directory,
    final @TempDir Path dataDirectory)
    throws IOException
  {
    this.directory =
      directory;
    this.dataDirectory =
      dataDirectory;
    this.databaseFile =
      directory.resolve("inventory.db");
    this.dataDirectory =
      directory.resolve("data");
    this.inventoryConfiguration =
      ARInventoryConfiguration.builder()
        .setDatabaseFile(this.databaseFile)
        .setDataDirectory(this.dataDirectory)
        .build();

    Files.createDirectories(this.directory);
    Files.createDirectories(this.dataDirectory);
  }

  @AfterEach
  public void tearDown()
    throws IOException
  {
    FileUtils.deleteDirectory(this.directory.toFile());
    FileUtils.deleteDirectory(this.dataDirectory.toFile());
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
        progress -> {
          LOG.debug("{}", progress);
        }
      ).get();
    }

    LOG.debug("Checking database...");
    try (var inventory = ARInventories.open(this.inventoryConfiguration)) {
      final var database = inventory.database();
      final var transaction = database.openTransaction();
      final var blob =
        transaction.execute(
          ARQueryBlobGetType.class,
          new ARInventoryHash(SHA_256, "3733cd977ff8eb18b987357e22ced99f46097f31ecb239e878ae63760e83e4d5")
        );
      assertTrue(blob.isPresent());
    }
  }
}
