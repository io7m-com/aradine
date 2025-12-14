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

import com.io7m.aradine.inventory.api.ARInventoryBlob;
import com.io7m.aradine.inventory.api.ARInventoryConfiguration;
import com.io7m.aradine.inventory.api.ARInventoryDatabaseType;
import com.io7m.aradine.inventory.api.ARInventoryException;
import com.io7m.aradine.inventory.api.ARInventoryHash;
import com.io7m.aradine.inventory.api.ARInventoryHashAlgorithm;
import com.io7m.aradine.inventory.api.ARInventoryProgress;
import com.io7m.aradine.inventory.api.ARInventoryType;
import com.io7m.aradine.inventory.api.queries.ARQueryBlobPutType;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import com.io7m.mime2045.core.MimeType;
import com.io7m.streamtime.core.STTimedInputStream;
import com.io7m.streamtime.core.STTransferStatistics;
import org.apache.commons.io.output.NullOutputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Inventory implementation.
 */

public final class ARInventory implements ARInventoryType
{
  private static final ARInventoryDBFactory DATABASES =
    new ARInventoryDBFactory();

  private final ARInventoryDB database;
  private final ARInventoryConfiguration configuration;
  private final ExecutorService databaseExecutor;
  private final ARInventoryBlobDirectory blobDirectory;
  private final CloseableCollectionType<ARInventoryException> resources;

  private ARInventory(
    final ARInventoryConfiguration inConfiguration,
    final CloseableCollectionType<ARInventoryException> inResources,
    final ARInventoryDB inDatabase,
    final ExecutorService inDatabaseExecutor,
    final ARInventoryBlobDirectory inBlobDirectory)
  {
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
    this.resources =
      Objects.requireNonNull(inResources, "resources");
    this.database =
      Objects.requireNonNull(inDatabase, "database");
    this.databaseExecutor =
      Objects.requireNonNull(inDatabaseExecutor, "databaseExecutor");
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
   * @throws ARInventoryException On errors
   */

  public static ARInventoryType open(
    final ARInventoryConfiguration configuration)
    throws ARInventoryException
  {
    final var closeables =
      ARCloseables.create();

    try {
      final var databaseExecutor =
        Executors.newSingleThreadExecutor(r -> {
          final var thread = new Thread(r);
          thread.setName(
            "com.io7m.aradine.inventory.database-%d"
              .formatted(Long.valueOf(thread.threadId()))
          );
          return thread;
        });

      closeables.add(databaseExecutor);

      final var database =
        closeables.add(DATABASES.open(configuration.databaseFile()));

      return new ARInventory(
        configuration,
        closeables,
        database,
        databaseExecutor,
        new ARInventoryBlobDirectory(configuration.dataDirectory())
      );
    } catch (final Throwable e) {
      closeables.close();
      throw e;
    }
  }

  @Override
  public ARInventoryDatabaseType database()
  {
    return this.database;
  }

  @Override
  public CompletableFuture<ARInventoryBlob> blobInstall(
    final Path file,
    final MimeType type,
    final Consumer<ARInventoryProgress> progressConsumer)
  {
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(progressConsumer, "progressConsumer");

    return new ARInventoryBlobInstallOp(this, file, type, progressConsumer)
      .execute();
  }

  @Override
  public void close()
    throws ARInventoryException
  {
    this.resources.close();
  }

  interface ARInventoryOpType<T>
  {
    CompletableFuture<T> execute();
  }

  private static final class ARInventoryBlobInstallOp
    implements ARInventoryOpType<ARInventoryBlob>
  {
    private final ARInventory inventory;
    private final Path file;
    private final MimeType type;
    private final Consumer<ARInventoryProgress> progressConsumer;
    private final int taskCount;
    private volatile int taskIndex;
    private volatile long fileSize;
    private volatile String task = "";

    private ARInventoryBlobInstallOp(
      final ARInventory inInventory,
      final Path inFile,
      final MimeType inType,
      final Consumer<ARInventoryProgress> inProgressConsumer)
    {
      this.inventory = inInventory;
      this.file = inFile;
      this.type = inType;
      this.progressConsumer = inProgressConsumer;
      this.taskCount = 3;
    }

    @Override
    public CompletableFuture<ARInventoryBlob> execute()
    {
      return this.hashFile()
        .thenCompose(this::copyFile)
        .thenComposeAsync(this::saveBlob, this.inventory.databaseExecutor);
    }

    private CompletableFuture<ARInventoryBlob> saveBlob(
      final ARInventoryHash hash)
    {
      return executeFuture(op -> this.saveBlobOp(hash, op));
    }

    private ARInventoryBlob saveBlobOp(
      final ARInventoryHash hash,
      final CompletableFuture<ARInventoryBlob> future)
      throws Exception
    {
      this.task = "Saving blob to database.";
      this.taskIndex = 2;
      this.publishProgressNow(OptionalDouble.empty());

      try (var transaction = this.inventory.database.openTransaction()) {
        final var blob = new ARInventoryBlob(this.fileSize, hash, this.type);
        transaction.execute(ARQueryBlobPutType.class, blob);
        transaction.commit();
        return blob;
      } finally {
        this.publishProgressNow(OptionalDouble.of(1.0));
      }
    }

    private CompletableFuture<ARInventoryHash> copyFile(
      final ARInventoryHash hash)
    {
      return executeFuture(op -> this.copyFileOp(hash, op));
    }

    private ARInventoryHash copyFileOp(
      final ARInventoryHash hash,
      final CompletableFuture<ARInventoryHash> future)
      throws Exception
    {
      this.task = "Copying file to blob directory.";
      this.taskIndex = 1;
      this.publishProgressNow(OptionalDouble.empty());

      this.inventory.blobDirectory.copyIn(
        hash,
        this.file,
        progress -> this.progressNow(OptionalDouble.of(progress)),
        future::isCancelled
      );

      this.publishProgressNow(OptionalDouble.of(1.0));
      return hash;
    }

    private CompletableFuture<ARInventoryHash> hashFile()
    {
      return executeFuture(this::hashFileOp);
    }

    private ARInventoryHash hashFileOp(
      final CompletableFuture<ARInventoryHash> future)
      throws Exception
    {
      this.task = "Computing hash of file.";
      this.taskIndex = 0;
      this.publishProgressNow(OptionalDouble.empty());
      this.fileSize = Files.size(this.file);

      final var digest =
        MessageDigest.getInstance("SHA-256");

      final Consumer<STTransferStatistics> statConsumer = stats -> {
        this.publishProgressNow(stats.percentNormalized());
      };

      try (var stream = Files.newInputStream(this.file)) {
        try (var timedStream = new STTimedInputStream(statConsumer, stream)) {
          final var nullOut =
            NullOutputStream.nullOutputStream();

          try (var outStream = new DigestOutputStream(nullOut, digest)) {
            final var buffer = new byte[4096];
            while (true) {
              checkCancelled(future);
              final var r = timedStream.read(buffer);
              if (r == -1) {
                break;
              }
              outStream.write(buffer, 0, r);
            }
          }
        }
      }

      this.publishProgressNow(OptionalDouble.of(1.0));
      return new ARInventoryHash(
        ARInventoryHashAlgorithm.SHA_256,
        HexFormat.of().formatHex(digest.digest())
      );
    }

    private static void checkCancelled(
      final CompletableFuture<?> future)
    {
      if (future.isCancelled()) {
        throw new CancellationException();
      }
    }

    private ARInventoryProgress progressNow(
      final OptionalDouble progress)
    {
      return new ARInventoryProgress(
        this.task,
        this.taskCount,
        this.taskIndex + 1,
        progress.orElse(0.0)
      );
    }

    private void publishProgressNow(
      final OptionalDouble progress)
    {
      this.progressConsumer.accept(this.progressNow(progress));
    }
  }

  interface ARFutureOpType<T>
  {
    T execute(CompletableFuture<T> op)
      throws Exception;
  }

  private static <T> CompletableFuture<T> executeFuture(
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
}
