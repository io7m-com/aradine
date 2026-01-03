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

import com.io7m.aradine.api.ARBlob;
import com.io7m.aradine.api.ARBytes;
import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.ARHash;
import com.io7m.aradine.api.instrument.ARInstrumentData;
import com.io7m.aradine.api.instrument.ARInstrumentDataSummary;
import com.io7m.aradine.api.instrument.ARInstrumentID;
import com.io7m.aradine.database.api.ARDBConfiguration;
import com.io7m.aradine.database.api.ARDBType;
import com.io7m.aradine.database.sqlite3.ARDBFactory;
import com.io7m.aradine.ensemble.internal.database.AREnsDB;
import com.io7m.aradine.instrument.loader.ARInstrumentReaders;
import com.io7m.aradine.inventory.ARInventories;
import com.io7m.aradine.inventory.api.ARInventoryConfiguration;
import com.io7m.aradine.inventory.api.queries.ARQueryBlobGetType;
import com.io7m.aradine.inventory.api.queries.ARQueryBlobPutType;
import com.io7m.aradine.inventory.api.queries.ARQueryInstrumentGetType;
import com.io7m.aradine.inventory.api.queries.ARQueryInstrumentListType;
import com.io7m.aradine.inventory.api.queries.ARQueryInstrumentPutType;
import com.io7m.aradine.inventory.api.queries.ARQuerySchemaVersionType;
import com.io7m.lanark.core.RDottedName;
import com.io7m.mime2045.core.MimeType;
import com.io7m.verona.core.Version;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.io7m.aradine.api.ARHashAlgorithm.SHA_256;
import static com.io7m.aradine.inventory.api.queries.ARInventoryUnit.UNIT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class ARInventoryDatabaseTest
{
  private Path directory;
  private Path databaseFile;
  private Path dataDirectory;
  private ARInventoryConfiguration inventoryConfiguration;
  private ARDBType database;
  private ExecutorService databaseExecutor;

  @BeforeEach
  public void setup()
    throws Exception
  {
    this.directory =
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
        .setDataDirectory(this.dataDirectory)
        .setDatabase(this.database)
        .setDatabaseExecutor(this.databaseExecutor)
        .setReaders(new ARInstrumentReaders())
        .build();
  }

  @AfterEach
  public void tearDown()
  {
    try {
      FileUtils.deleteDirectory(this.directory.toFile());
    } catch (final IOException e) {
      // Don't care
    }
    this.databaseExecutor.close();
  }

  @Test
  public void testOpenGarbage()
    throws Exception
  {
    final var file =
      this.directory.resolve("test.aens");

    final var rng = SecureRandom.getInstanceStrong();
    final var data = new byte[65536];
    rng.nextBytes(data);
    Files.write(file, data);

    final var ex = assertThrows(
      ARException.class, () -> {
        new ARDBFactory()
          .open(
            ARDBConfiguration.builder()
              .addAllQueries(ARInventories.queries())
              .setApplicationId(0x10203040)
              .setApplicationIdText(new RDottedName("com.io7m.aradine.example"))
              .setDatabaseFile(file)
              .build()
          );
      });
    assertEquals("error-file-not-database", ex.errorCode());
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
            new ARHash(SHA_256, "abcd")
          )
        );

        final var blob =
          new ARBlob(
            100L,
            new ARHash(SHA_256, "abcd"),
            MimeType.of("text", "plain")
          );

        transaction.execute(ARQueryBlobPutType.class, blob);

        assertEquals(
          Optional.of(blob),
          transaction.execute(
            ARQueryBlobGetType.class,
            new ARHash(SHA_256, "abcd")
          )
        );
      }
    }
  }

  @Test
  public void testInstrumentPutGet()
    throws Exception
  {
    try (var inventory = ARInventories.open(this.inventoryConfiguration)) {
      final var database = inventory.database();
      try (var transaction = database.openTransaction()) {
        final var blob =
          new ARBlob(
            100L,
            new ARHash(SHA_256, "abcd"),
            MimeType.of("text", "plain")
          );

        final var identifier =
          new ARInstrumentID(
            new RDottedName("com.io7m.example"),
            new RDottedName("com.io7m.example"),
            Version.of(1, 0, 0)
          );

        final var instrument =
          new ARInstrumentData(
            identifier,
            new RDottedName("com.io7m.aradine.metadata.json"),
            new ARBytes("{}".getBytes(StandardCharsets.UTF_8)),
            "Instrument 0",
            "An instrument.",
            blob
          );

        assertEquals(
          Optional.empty(),
          transaction.execute(ARQueryInstrumentGetType.class, identifier)
        );
        transaction.execute(ARQueryBlobPutType.class, blob);
        transaction.execute(ARQueryInstrumentPutType.class, instrument);
        transaction.commit();

        assertEquals(
          Optional.of(instrument),
          transaction.execute(ARQueryInstrumentGetType.class, identifier)
        );
      }
    }
  }

  @Test
  public void testInstrumentList()
    throws Exception
  {
    final var blob =
      new ARBlob(
        100L,
        new ARHash(SHA_256, "abcd"),
        MimeType.of("text", "plain")
      );

    final var instruments =
      new ArrayList<ARInstrumentData>(10000);
    final var instrumentsSummaries =
      new ArrayList<ARInstrumentDataSummary>(10000);
    final var instrumentsResults =
      new ArrayList<ARInstrumentDataSummary>(10000);

    for (int index = 0; index < 10000; ++index) {
      final var identifier =
        new ARInstrumentID(
          new RDottedName("com.io7m.example"),
          new RDottedName("com.io7m.example"),
          Version.of(index, 0, 0)
        );

      final var instrument =
        new ARInstrumentData(
          identifier,
          new RDottedName("com.io7m.aradine.metadata.json"),
          new ARBytes("{}".getBytes(StandardCharsets.UTF_8)),
          "Instrument " + index,
          "An instrument.",
          blob
        );

      instruments.add(instrument);
      instrumentsSummaries.add(
        new ARInstrumentDataSummary(
          identifier,
          instrument.title(),
          instrument.description()
        )
      );
    }

    try (var inventory = ARInventories.open(this.inventoryConfiguration)) {
      final var database = inventory.database();
      try (var transaction = database.openTransaction()) {
        transaction.execute(ARQueryBlobPutType.class, blob);
        for (final var instrument : instruments) {
          transaction.execute(ARQueryInstrumentPutType.class, instrument);
        }
        transaction.commit();
      }

      try (var transaction = database.openTransaction()) {
        Optional<ARInstrumentID> start = Optional.empty();
        while (true) {
          final var r =
            transaction.execute(
              ARQueryInstrumentListType.class,
              new ARQueryInstrumentListType.Parameters(start, 1000)
            );

          if (r.isEmpty()) {
            break;
          }

          start = Optional.of(r.getLast().identifier());
          instrumentsResults.addAll(r);
        }
      }
    }

    for (int index = 0; index < 10000; ++index) {
      assertEquals(
        instrumentsSummaries.get(index),
        instrumentsResults.get(index)
      );
    }
  }
}
