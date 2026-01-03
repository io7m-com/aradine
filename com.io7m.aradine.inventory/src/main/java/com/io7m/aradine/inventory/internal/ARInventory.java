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
import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.ARHash;
import com.io7m.aradine.api.ARHashAlgorithm;
import com.io7m.aradine.api.instrument.ARInstrumentData;
import com.io7m.aradine.api.instrument.ARInstrumentException;
import com.io7m.aradine.api.instrument.ARInstrumentID;
import com.io7m.aradine.api.progress.ARProgress;
import com.io7m.aradine.database.api.ARDBType;
import com.io7m.aradine.inventory.api.ARInventoryConfiguration;
import com.io7m.aradine.inventory.api.ARInventoryType;
import com.io7m.aradine.inventory.api.queries.ARQueryBlobPutType;
import com.io7m.aradine.inventory.api.queries.ARQueryInstrumentGetType;
import com.io7m.aradine.inventory.api.queries.ARQueryInstrumentPutType;
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
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * Inventory implementation.
 */

public final class ARInventory implements ARInventoryType
{
  private final ARDBType database;
  private final ARInventoryConfiguration configuration;
  private final ExecutorService databaseExecutor;
  private final ARInventoryBlobDirectory blobDirectory;

  private ARInventory(
    final ARInventoryConfiguration inConfiguration,
    final ARInventoryBlobDirectory inBlobDirectory)
  {
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
    this.database =
      inConfiguration.database();
    this.databaseExecutor =
      inConfiguration.databaseExecutor();
    this.blobDirectory =
      Objects.requireNonNull(inBlobDirectory, "blobDirectory");
  }

  /**
   * Open a inventory.
   *
   * @param configuration The inventory configuration
   *
   * @return A inventory
   */

  public static ARInventoryType open(
    final ARInventoryConfiguration configuration)
  {
    return new ARInventory(
      configuration,
      new ARInventoryBlobDirectory(configuration.dataDirectory())
    );
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
  public Optional<Path> instrumentFile(
    final ARInstrumentID instrumentID)
    throws ARException
  {
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
  public void close()
  {

  }

  interface ARInventoryOpType<T>
  {
    CompletableFuture<T> execute();
  }

  interface ARFutureOpType<T>
  {
    T execute(CompletableFuture<T> op)
      throws Exception;
  }

  private static final class ARInventoryInstrumentInstallOp
    implements ARInventoryOpType<ARInstrumentData>
  {
    private static final int SUBTASK_COUNT = 3;
    private final ARInventory inventory;
    private final Path file;
    private final MimeType type;
    private final Consumer<ARProgress> progressConsumer;
    private final String task;
    private volatile double taskProgress;
    private volatile String subTask;
    private volatile double subtaskProgress;

    private static double taskProgressOf(
      final int subTaskIndex)
    {
      return (double) (subTaskIndex + 1) / (double) SUBTASK_COUNT;
    }

    private ARInventoryInstrumentInstallOp(
      final ARInventory inInventory,
      final Path inFile,
      final MimeType inType,
      final Consumer<ARProgress> inProgressConsumer)
    {
      this.inventory = inInventory;
      this.file = inFile;
      this.type = inType;
      this.progressConsumer = inProgressConsumer;
      this.task = "Installing instrument.";
    }

    private static void checkCancelled(
      final CompletableFuture<?> future)
    {
      if (future.isCancelled()) {
        throw new CancellationException();
      }
    }

    @Override
    public CompletableFuture<ARInstrumentData> execute()
    {
      return this.parseFile()
        .thenCompose(this::copyFile)
        .thenComposeAsync(this::saveBlob, this.inventory.databaseExecutor);
    }

    private CompletableFuture<ARInstrumentData> saveBlob(
      final ARInstrumentData instrument)
    {
      return executeFuture(op -> this.saveBlobOp(instrument, op));
    }

    private ARInstrumentData saveBlobOp(
      final ARInstrumentData instrument,
      final CompletableFuture<ARInstrumentData> future)
      throws Exception
    {
      this.taskProgress = taskProgressOf(2);
      this.subTask = "Saving instrument to database.";
      this.subtaskProgress = 0.0;
      this.publishProgressNow();

      checkCancelled(future);
      try (var transaction = this.inventory.database.openTransaction()) {
        final var blob = instrument.blob();
        transaction.execute(ARQueryBlobPutType.class, blob);
        transaction.execute(ARQueryInstrumentPutType.class, instrument);
        transaction.commit();
        return instrument;
      } finally {
        this.subtaskProgress = 1.0;
        this.publishProgressNow();
      }
    }

    private CompletableFuture<ARInstrumentData> copyFile(
      final ARInstrumentData instrument)
    {
      return executeFuture(op -> this.copyFileOp(instrument, op));
    }

    private ARInstrumentData copyFileOp(
      final ARInstrumentData instrument,
      final CompletableFuture<ARInstrumentData> future)
      throws Exception
    {
      this.taskProgress = taskProgressOf(1);
      this.subTask = "Copying file to blob directory.";
      this.subtaskProgress = 0.0;
      this.publishProgressNow();

      checkCancelled(future);
      this.inventory.blobDirectory.copyIn(
        instrument.blob().hash(),
        this.file,
        progress -> {
          this.subtaskProgress = progress.doubleValue();
          this.publishProgressNow();
        },
        future::isCancelled
      );

      this.subtaskProgress = 1.0;
      this.publishProgressNow();
      return instrument;
    }

    private CompletableFuture<ARInstrumentData> parseFile()
    {
      return executeFuture(this::parseFileOp);
    }

    private ARInstrumentData parseFileOp(
      final CompletableFuture<ARInstrumentData> future)
      throws Exception
    {
      this.taskProgress = taskProgressOf(0);
      this.subTask = "Parsing instrument file.";
      this.subtaskProgress = 0.0;
      this.publishProgressNow();

      checkCancelled(future);
      final var readers = this.inventory.configuration.readers();
      try (var reader = readers.create(this.file)) {
        final var instrument = reader.execute();
        this.subtaskProgress = 1.0;
        this.publishProgressNow();
        return instrument;
      } catch (final ARInstrumentException e) {
        throw new ARException(
          e.getMessage(),
          e,
          e.errorCode(),
          e.attributes(),
          e.remediatingAction()
        );
      }
    }

    private ARProgress progressNow()
    {
      return new ARProgress(
        this.task,
        this.taskProgress,
        this.subTask,
        this.subtaskProgress
      );
    }

    private void publishProgressNow()
    {
      this.progressConsumer.accept(this.progressNow());
    }
  }

  private static final class ARInventoryBlobInstallOp
    implements ARInventoryOpType<ARBlob>
  {
    private static final int SUBTASK_COUNT = 3;

    private final ARInventory inventory;
    private final Path file;
    private final MimeType type;
    private final Consumer<ARProgress> progressConsumer;
    private final String task;
    private volatile double taskProgress;
    private volatile String subTask;
    private volatile double subtaskProgress;
    private long fileSize;

    private static double taskProgressOf(
      final int subTaskIndex)
    {
      return (double) (subTaskIndex + 1) / (double) SUBTASK_COUNT;
    }

    private ARInventoryBlobInstallOp(
      final ARInventory inInventory,
      final Path inFile,
      final MimeType inType,
      final Consumer<ARProgress> inProgressConsumer)
    {
      this.inventory = inInventory;
      this.file = inFile;
      this.type = inType;
      this.progressConsumer = inProgressConsumer;
      this.task = "Installing blob.";
    }

    private static void checkCancelled(
      final CompletableFuture<?> future)
    {
      if (future.isCancelled()) {
        throw new CancellationException();
      }
    }

    @Override
    public CompletableFuture<ARBlob> execute()
    {
      return this.hashFile()
        .thenCompose(this::copyFile)
        .thenComposeAsync(this::saveBlob, this.inventory.databaseExecutor);
    }

    private CompletableFuture<ARBlob> saveBlob(
      final ARHash hash)
    {
      return executeFuture(op -> this.saveBlobOp(hash, op));
    }

    private ARBlob saveBlobOp(
      final ARHash hash,
      final CompletableFuture<ARBlob> future)
      throws Exception
    {
      this.taskProgress = taskProgressOf(2);
      this.subTask = "Saving blob to database.";
      this.subtaskProgress = 0.0;
      this.publishProgressNow();

      try (var transaction = this.inventory.database.openTransaction()) {
        final var blob = new ARBlob(this.fileSize, hash, this.type);
        transaction.execute(ARQueryBlobPutType.class, blob);
        transaction.commit();
        return blob;
      } finally {
        this.subtaskProgress = 1.0;
        this.publishProgressNow();
      }
    }

    private CompletableFuture<ARHash> copyFile(
      final ARHash hash)
    {
      return executeFuture(op -> this.copyFileOp(hash, op));
    }

    private ARHash copyFileOp(
      final ARHash hash,
      final CompletableFuture<ARHash> future)
      throws Exception
    {
      this.taskProgress = taskProgressOf(1);
      this.subTask = "Copying file to blob directory.";
      this.subtaskProgress = 0.0;
      this.publishProgressNow();

      this.inventory.blobDirectory.copyIn(
        hash,
        this.file,
        progress -> {
          this.subtaskProgress = progress.doubleValue();
          this.publishProgressNow();
        },
        future::isCancelled
      );

      this.subtaskProgress = 1.0;
      this.publishProgressNow();
      return hash;
    }

    private CompletableFuture<ARHash> hashFile()
    {
      return executeFuture(this::hashFileOp);
    }

    private ARHash hashFileOp(
      final CompletableFuture<ARHash> future)
      throws Exception
    {
      this.taskProgress = taskProgressOf(0);
      this.subTask = "Computing hash of file.";
      this.subtaskProgress = 0.0;
      this.publishProgressNow();
      this.fileSize = Files.size(this.file);

      final var digest =
        MessageDigest.getInstance("SHA-256");

      final Consumer<STTransferStatistics> statConsumer = stats -> {
        this.subtaskProgress = stats.percentNormalized().orElse(0.0);
        this.publishProgressNow();
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

      this.subtaskProgress = 1.0;
      this.publishProgressNow();
      return new ARHash(
        ARHashAlgorithm.SHA_256,
        HexFormat.of().formatHex(digest.digest())
      );
    }

    private ARProgress progressNow()
    {
      return new ARProgress(
        this.task,
        this.taskProgress,
        this.subTask,
        this.subtaskProgress
      );
    }

    private void publishProgressNow()
    {
      this.progressConsumer.accept(this.progressNow());
    }
  }
}
