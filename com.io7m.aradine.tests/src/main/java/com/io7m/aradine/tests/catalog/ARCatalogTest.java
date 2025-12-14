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

package com.io7m.aradine.tests.catalog;

import com.io7m.aradine.catalog.ARCatalogs;
import com.io7m.aradine.catalog.api.ARCatalogBlob;
import com.io7m.aradine.catalog.api.ARCatalogConfiguration;
import com.io7m.aradine.catalog.api.ARCatalogHash;
import com.io7m.aradine.catalog.api.queries.ARQueryBlobGetType;
import com.io7m.aradine.catalog.api.queries.ARQueryBlobPutType;
import com.io7m.aradine.catalog.api.queries.ARQuerySchemaVersionType;
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
import java.util.Optional;

import static com.io7m.aradine.catalog.api.ARCatalogHashAlgorithm.SHA_256;
import static com.io7m.aradine.catalog.api.queries.ARCatalogUnit.UNIT;
import static java.nio.file.StandardOpenOption.CREATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ARCatalogTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ARCatalogTest.class);

  private Path databaseFile;
  private Path dataDirectory;
  private ARCatalogConfiguration catalogConfiguration;
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
      directory.resolve("catalog.db");
    this.dataDirectory =
      directory.resolve("data");
    this.catalogConfiguration =
      ARCatalogConfiguration.builder()
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

    try (var catalog = ARCatalogs.open(this.catalogConfiguration)) {
      LOG.debug("Installing blob...");
      catalog.blobInstall(
        file,
        MimeType.of("text", "plain"),
        progress -> {
          LOG.debug("{}", progress);
        }
      ).get();
    }

    LOG.debug("Checking database...");
    try (var catalog = ARCatalogs.open(this.catalogConfiguration)) {
      final var database = catalog.database();
      final var transaction = database.openTransaction();
      final var blob =
        transaction.execute(
          ARQueryBlobGetType.class,
          new ARCatalogHash(SHA_256, "3733cd977ff8eb18b987357e22ced99f46097f31ecb239e878ae63760e83e4d5")
        );
      assertTrue(blob.isPresent());
    }
  }
}
