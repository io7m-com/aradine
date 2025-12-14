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
import com.io7m.aradine.inventory.api.ARInventoryBlob;
import com.io7m.aradine.inventory.api.ARInventoryConfiguration;
import com.io7m.aradine.inventory.api.ARInventoryHash;
import com.io7m.aradine.inventory.api.queries.ARQueryBlobGetType;
import com.io7m.aradine.inventory.api.queries.ARQueryBlobPutType;
import com.io7m.aradine.inventory.api.queries.ARQuerySchemaVersionType;
import com.io7m.mime2045.core.MimeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Optional;

import static com.io7m.aradine.inventory.api.ARInventoryHashAlgorithm.SHA_256;
import static com.io7m.aradine.inventory.api.queries.ARInventoryUnit.UNIT;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class ARInventoryDatabaseTest
{
  private Path directory;
  private Path databaseFile;
  private Path dataDirectory;
  private ARInventoryConfiguration inventoryConfiguration;

  @BeforeEach
  public void setup(
    final @TempDir Path directory)
  {
    this.directory =
      directory;
    this.databaseFile =
      directory.resolve("inventory.db");
    this.dataDirectory =
      directory.resolve("data");
    this.inventoryConfiguration =
      ARInventoryConfiguration.builder()
        .setDatabaseFile(this.databaseFile)
        .setDataDirectory(this.dataDirectory)
        .build();
  }

  @Test
  public void testOpenClose()
    throws Exception
  {
    try (var inventory = ARInventories.open(this.inventoryConfiguration)) {
      final var database = inventory.database();
      try (var transaction = database.openTransaction()) {
        assertEquals(
          1,
          transaction.execute(ARQuerySchemaVersionType.class, UNIT)
        );
      }
    }
  }

  @Test
  public void testBlobPutGet()
    throws Exception
  {
    try (var inventory = ARInventories.open(this.inventoryConfiguration)) {
      final var database = inventory.database();
      try (var transaction = database.openTransaction()) {
        assertEquals(
          Optional.empty(),
          transaction.execute(
            ARQueryBlobGetType.class,
            new ARInventoryHash(SHA_256, "abcd")
          )
        );

        final var blob =
          new ARInventoryBlob(
            100L,
            new ARInventoryHash(SHA_256, "abcd"),
            MimeType.of("text", "plain")
          );

        transaction.execute(ARQueryBlobPutType.class, blob);

        assertEquals(
          Optional.of(blob),
          transaction.execute(
            ARQueryBlobGetType.class,
            new ARInventoryHash(SHA_256, "abcd")
          )
        );
      }
    }
  }
}
