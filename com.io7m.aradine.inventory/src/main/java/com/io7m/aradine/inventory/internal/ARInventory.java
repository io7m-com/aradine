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

package com.io7m.aradine.inventory.internal;

import com.io7m.aradine.api.ARBlob;
import com.io7m.aradine.api.ARCloseables;
import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.instrument.ARInstrumentData;
import com.io7m.aradine.api.instrument.ARInstrumentDataSummary;
import com.io7m.aradine.api.instrument.ARInstrumentID;
import com.io7m.aradine.api.progress.ARProgress;
import com.io7m.aradine.api.sample_map.ARSampleMapDataSummary;
import com.io7m.aradine.api.sample_map.ARSampleMapID;
import com.io7m.aradine.database.api.ARDBType;
import com.io7m.aradine.inventory.api.ARInventoryConfiguration;
import com.io7m.aradine.inventory.api.ARInventoryType;
import com.io7m.aradine.inventory.api.queries.ARQueryInstrumentGetType;
import com.io7m.aradine.inventory.api.queries.ARQueryInstrumentListType;
import com.io7m.aradine.inventory.api.queries.ARQuerySampleMapGetType;
import com.io7m.aradine.inventory.api.queries.ARQuerySampleMapListType;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import com.io7m.mime2045.core.MimeType;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static com.io7m.aradine.inventory.api.queries.ARQueryInstrumentListType.Parameters;

/**
 * Inventory implementation.
 */

public final class ARInventory implements ARInventoryType
{
  private final ARDBType database;
  private final CloseableCollectionType<ARException> resources;
  private final ARInventoryConfiguration configuration;
  private final ExecutorService databaseExecutor;
  private final ARInventoryBlobDirectory blobDirectory;

  private ARInventory(
    final CloseableCollectionType<ARException> inResources,
    final ARInventoryConfiguration inConfiguration,
    final ARInventoryDB inDatabase,
    final ExecutorService inExecutor,
    final ARInventoryBlobDirectory inBlobDirectory)
  {
    this.resources =
      Objects.requireNonNull(inResources, "Resources");
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
    this.database =
      Objects.requireNonNull(inDatabase, "Database");
    this.databaseExecutor =
      Objects.requireNonNull(inExecutor, "Executor");
    this.blobDirectory =
      Objects.requireNonNull(inBlobDirectory, "blobDirectory");
  }

  /**
   * Open a inventory.
   *
   * @param configuration The inventory configuration
   *
   * @return A inventory
   *
   * @throws ARException On errors
   */

  public static ARInventoryType open(
    final ARInventoryConfiguration configuration)
    throws ARException
  {
    final var resources =
      ARCloseables.create();

    try {
      final var database =
        resources.add(
          ARInventoryDB.createDatabase(configuration.databaseFile())
        );
      final var executor =
        resources.add(
          Executors.newSingleThreadExecutor(r -> {
            final var thread = new Thread(r);
            thread.setName(threadNameOf(thread));
            return thread;
          })
        );

      return new ARInventory(
        resources,
        configuration,
        database,
        executor,
        new ARInventoryBlobDirectory(configuration.dataDirectory())
      );
    } catch (final Throwable e) {
      resources.close();
      throw e;
    }
  }

  private static String threadNameOf(
    final Thread thread)
  {
    return "com.io7m.aradine.inventory-%s"
      .formatted(Long.toUnsignedString(thread.threadId()));
  }

  static <T> CompletableFuture<T> executeFuture(
    final ARFutureOpType<T> op)
  {
    final var future = new CompletableFuture<T>();
    Thread.ofVirtual()
      .start(() -> {
        try {
          future.complete(op.execute(future));
        } catch (final Throwable e) {
          future.completeExceptionally(e);
        }
      });
    return future;
  }

  ARInventoryConfiguration configuration()
  {
    return this.configuration;
  }

  ARInventoryBlobDirectory blobDirectory()
  {
    return this.blobDirectory;
  }

  ExecutorService databaseExecutor()
  {
    return this.databaseExecutor;
  }

  @Override
  public ARDBType database()
  {
    return this.database;
  }

  @Override
  public CompletableFuture<ARBlob> blobInstall(
    final Path file,
    final MimeType type,
    final Consumer<ARProgress> progressConsumer)
  {
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(progressConsumer, "progressConsumer");

    return new ARInventoryBlobInstallOp(this, file, type, progressConsumer)
      .execute();
  }

  @Override
  public CompletableFuture<ARInstrumentID> instrumentInstall(
    final Path file,
    final Consumer<ARProgress> progressConsumer)
  {
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(progressConsumer, "progressConsumer");

    return new ARInventoryInstrumentInstallOp(
      this,
      file,
      INSTRUMENT_JAR_MIME_TYPE,
      progressConsumer
    ).execute()
      .thenApply(ARInstrumentData::identifier);
  }

  @Override
  public CompletableFuture<?> instrumentUninstall(
    final ARInstrumentID instrumentID,
    final Consumer<ARProgress> progressConsumer)
  {
    Objects.requireNonNull(instrumentID, "InstrumentID");
    Objects.requireNonNull(progressConsumer, "ProgressConsumer");

    return new ARInventoryInstrumentUninstallOp(
      this, instrumentID, progressConsumer)
      .execute();
  }

  @Override
  public Optional<Path> instrumentFile(
    final ARInstrumentID instrumentID)
    throws ARException
  {
    Objects.requireNonNull(instrumentID, "InstrumentID");

    try (var t = this.database.openTransaction()) {
      final var instrumentOpt =
        t.execute(ARQueryInstrumentGetType.class, instrumentID);
      if (instrumentOpt.isEmpty()) {
        return Optional.empty();
      }
      final var instrument = instrumentOpt.get();
      return this.blobDirectory.get(instrument.blob().hash());
    }
  }

  @Override
  public CompletableFuture<List<ARInstrumentDataSummary>> instrumentList(
    final Optional<ARInstrumentID> start,
    final int limit)
  {
    Objects.requireNonNull(start, "Start");

    final var future = new CompletableFuture<List<ARInstrumentDataSummary>>();
    this.databaseExecutor.execute(() -> {
      try {
        try (var t = this.database.openTransaction()) {
          final var p = new Parameters(start, limit);
          future.complete(t.execute(ARQueryInstrumentListType.class, p));
        }
      } catch (final Throwable e) {
        future.completeExceptionally(e);
      }
    });
    return future;
  }

  @Override
  public CompletableFuture<ARSampleMapID> sampleMapInstall(
    final Path file,
    final Consumer<ARProgress> progressConsumer)
  {
    return new ARInventorySampleMapInstallOp(this, file, progressConsumer)
      .execute()
      .thenApply(ARSampleMapDataSummary::identifier);
  }

  @Override
  public CompletableFuture<?> sampleMapUninstall(
    final ARSampleMapID sampleMap,
    final Consumer<ARProgress> progressConsumer)
  {
    return new ARInventorySampleMapUninstallOp(
      this, sampleMap, progressConsumer)
      .execute();
  }

  @Override
  public Optional<Path> sampleMapFile(
    final ARSampleMapID sampleMap)
    throws ARException
  {
    try (var t = this.database.openTransaction()) {
      final var sampleMapOpt =
        t.execute(ARQuerySampleMapGetType.class, sampleMap);
      if (sampleMapOpt.isEmpty()) {
        return Optional.empty();
      }
      final var instrument = sampleMapOpt.get();
      return this.blobDirectory.get(instrument.blob().hash());
    }
  }

  @Override
  public CompletableFuture<List<ARSampleMapDataSummary>> sampleMapList(
    final Optional<ARSampleMapID> start,
    final int limit)
  {
    Objects.requireNonNull(start, "Start");

    final var future = new CompletableFuture<List<ARSampleMapDataSummary>>();
    this.databaseExecutor.execute(() -> {
      try {
        try (var t = this.database.openTransaction()) {
          final var p = new ARQuerySampleMapListType.Parameters(start, limit);
          future.complete(t.execute(ARQuerySampleMapListType.class, p));
        }
      } catch (final Throwable e) {
        future.completeExceptionally(e);
      }
    });
    return future;
  }

  @Override
  public void close()
  {

  }
}
