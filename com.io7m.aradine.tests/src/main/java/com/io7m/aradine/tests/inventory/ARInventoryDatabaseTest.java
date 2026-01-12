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

import com.io7m.aradine.api.data.ARBlob;
import com.io7m.aradine.api.data.ARBytes;
import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.data.ARHash;
import com.io7m.aradine.api.instrument.ARInstrumentData;
import com.io7m.aradine.api.instrument.ARInstrumentDataSummary;
import com.io7m.aradine.api.instrument.ARInstrumentID;
import com.io7m.aradine.api.sample_map.ARSampleMapDataSummary;
import com.io7m.aradine.api.sample_map.ARSampleMapID;
import com.io7m.aradine.instrument.loader.ARInstrumentReaders;
import com.io7m.aradine.inventory.ARInventories;
import com.io7m.aradine.inventory.api.ARInventoryConfiguration;
import com.io7m.aradine.inventory.api.queries.ARQueryBlobDeleteType;
import com.io7m.aradine.inventory.api.queries.ARQueryBlobGetType;
import com.io7m.aradine.inventory.api.queries.ARQueryBlobPutType;
import com.io7m.aradine.inventory.api.queries.ARQueryBlobReferencesType;
import com.io7m.aradine.inventory.api.queries.ARQueryInstrumentDeleteType;
import com.io7m.aradine.inventory.api.queries.ARQueryInstrumentGetType;
import com.io7m.aradine.inventory.api.queries.ARQueryInstrumentListType;
import com.io7m.aradine.inventory.api.queries.ARQueryInstrumentPutType;
import com.io7m.aradine.inventory.api.queries.ARQuerySampleMapDeleteType;
import com.io7m.aradine.inventory.api.queries.ARQuerySampleMapGetType;
import com.io7m.aradine.inventory.api.queries.ARQuerySampleMapListType;
import com.io7m.aradine.inventory.api.queries.ARQuerySampleMapPutType;
import com.io7m.aradine.inventory.api.queries.ARQuerySchemaVersionType;
import com.io7m.aradine.inventory.internal.ARInventory;
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
import java.util.Set;

import static com.io7m.aradine.api.data.ARHashAlgorithm.SHA_256;
import static com.io7m.aradine.inventory.api.queries.ARInventoryUnit.UNIT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class ARInventoryDatabaseTest
{
  private Path directory;
  private Path databaseFile;
  private Path dataDirectory;
  private ARInventoryConfiguration inventoryConfiguration;

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

    this.inventoryConfiguration =
      ARInventoryConfiguration.builder()
        .setDataDirectory(this.dataDirectory)
        .setDatabaseFile(this.databaseFile)
        .setInstrumentReaders(new ARInstrumentReaders())
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
        ARInventory.open(
          ARInventoryConfiguration.builder()
            .setDataDirectory(this.dataDirectory)
            .setDatabaseFile(file)
            .setInstrumentReaders(new ARInstrumentReaders())
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
  public void testBlobPutGetDelete()
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

        transaction.execute(ARQueryBlobDeleteType.class, blob.hash());

        assertEquals(
          Optional.empty(),
          transaction.execute(
            ARQueryBlobGetType.class,
            new ARHash(SHA_256, "abcd")
          )
        );

        transaction.execute(ARQueryBlobDeleteType.class, blob.hash());
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
          new ARQueryBlobReferencesType.References(
            Set.of(identifier),
            Set.of()
          ),
          transaction.execute(
            ARQueryBlobReferencesType.class,
            instrument.blob().hash()
          )
        );

        assertEquals(
          Optional.of(instrument),
          transaction.execute(ARQueryInstrumentGetType.class, identifier)
        );
      }
    }
  }

  @Test
  public void testInstrumentPutGetDelete()
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
          new ARQueryBlobReferencesType.References(
            Set.of(identifier),
            Set.of()
          ),
          transaction.execute(
            ARQueryBlobReferencesType.class,
            instrument.blob().hash()
          )
        );

        assertEquals(
          Optional.of(instrument),
          transaction.execute(ARQueryInstrumentGetType.class, identifier)
        );

        transaction.execute(ARQueryInstrumentDeleteType.class, identifier);

        assertEquals(
          new ARQueryBlobReferencesType.References(
            Set.of(),
            Set.of()
          ),
          transaction.execute(
            ARQueryBlobReferencesType.class,
            instrument.blob().hash()
          )
        );

        assertEquals(
          Optional.empty(),
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


































  @Test
  public void testSampleMapPutGet()
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
          new ARSampleMapID(
            new RDottedName("com.io7m.example"),
            new RDottedName("com.io7m.example"),
            Version.of(1, 0, 0)
          );

        final var sampleMap =
          new ARSampleMapDataSummary(
            identifier,
            "SampleMap 0",
            "A sample map.",
            blob
          );

        assertEquals(
          Optional.empty(),
          transaction.execute(ARQuerySampleMapGetType.class, identifier)
        );
        transaction.execute(ARQueryBlobPutType.class, blob);
        transaction.execute(ARQuerySampleMapPutType.class, sampleMap);
        transaction.commit();

        assertEquals(
          new ARQueryBlobReferencesType.References(
            Set.of(),
            Set.of(identifier)
          ),
          transaction.execute(
            ARQueryBlobReferencesType.class,
            sampleMap.blob().hash()
          )
        );

        assertEquals(
          Optional.of(sampleMap),
          transaction.execute(ARQuerySampleMapGetType.class, identifier)
        );
      }
    }
  }

  @Test
  public void testSampleMapPutGetDelete()
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

        final var sampleMap =
          new ARSampleMapID(
            new RDottedName("com.io7m.example"),
            new RDottedName("com.io7m.example"),
            Version.of(1, 0, 0)
          );

        final var sampleMapDataSummary =
          new ARSampleMapDataSummary(
            sampleMap,
            "SampleMap 0",
            "A sample map.",
            blob
          );

        assertEquals(
          Optional.empty(),
          transaction.execute(ARQuerySampleMapGetType.class, sampleMap)
        );
        transaction.execute(ARQueryBlobPutType.class, blob);
        transaction.execute(ARQuerySampleMapPutType.class, sampleMapDataSummary);
        transaction.commit();

        assertEquals(
          new ARQueryBlobReferencesType.References(
            Set.of(),
            Set.of(sampleMap)
          ),
          transaction.execute(
            ARQueryBlobReferencesType.class,
            sampleMapDataSummary.blob().hash()
          )
        );

        assertEquals(
          Optional.of(sampleMapDataSummary),
          transaction.execute(ARQuerySampleMapGetType.class, sampleMap)
        );

        transaction.execute(ARQuerySampleMapDeleteType.class, sampleMap);

        assertEquals(
          new ARQueryBlobReferencesType.References(
            Set.of(),
            Set.of()
          ),
          transaction.execute(
            ARQueryBlobReferencesType.class,
            sampleMapDataSummary.blob().hash()
          )
        );

        assertEquals(
          Optional.empty(),
          transaction.execute(ARQuerySampleMapGetType.class, sampleMap)
        );
      }
    }
  }

  @Test
  public void testSampleMapList()
    throws Exception
  {
    final var blob =
      new ARBlob(
        100L,
        new ARHash(SHA_256, "abcd"),
        MimeType.of("text", "plain")
      );

    final var sampleMapsSummaries =
      new ArrayList<ARSampleMapDataSummary>(10000);
    final var sampleMapsResults =
      new ArrayList<ARSampleMapDataSummary>(10000);

    for (int index = 0; index < 10000; ++index) {
      final var identifier =
        new ARSampleMapID(
          new RDottedName("com.io7m.example"),
          new RDottedName("com.io7m.example"),
          Version.of(index, 0, 0)
        );

      sampleMapsSummaries.add(
        new ARSampleMapDataSummary(
          identifier,
          "SampleMap " + index,
          "A sample map.",
          blob
        )
      );
    }

    try (var inventory = ARInventories.open(this.inventoryConfiguration)) {
      final var database = inventory.database();
      try (var transaction = database.openTransaction()) {
        transaction.execute(ARQueryBlobPutType.class, blob);
        for (final var sampleMap : sampleMapsSummaries) {
          transaction.execute(ARQuerySampleMapPutType.class, sampleMap);
        }
        transaction.commit();
      }

      try (var transaction = database.openTransaction()) {
        Optional<ARSampleMapID> start = Optional.empty();
        while (true) {
          final var r =
            transaction.execute(
              ARQuerySampleMapListType.class,
              new ARQuerySampleMapListType.Parameters(start, 1000)
            );

          if (r.isEmpty()) {
            break;
          }

          start = Optional.of(r.getLast().identifier());
          sampleMapsResults.addAll(r);
        }
      }
    }

    for (int index = 0; index < 10000; ++index) {
      assertEquals(
        sampleMapsSummaries.get(index),
        sampleMapsResults.get(index)
      );
    }
  }
}
