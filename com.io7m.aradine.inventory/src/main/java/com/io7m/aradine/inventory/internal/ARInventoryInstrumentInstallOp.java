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

package com.io7m.aradine.inventory.internal;

import com.io7m.aradine.api.instrument.ARInstrumentData;
import com.io7m.aradine.api.progress.ARProgress;
import com.io7m.aradine.inventory.api.queries.ARQueryBlobPutType;
import com.io7m.aradine.inventory.api.queries.ARQueryInstrumentPutType;
import com.io7m.mime2045.core.MimeType;

import java.nio.file.Path;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

final class ARInventoryInstrumentInstallOp
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

  ARInventoryInstrumentInstallOp(
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

  private static double taskProgressOf(
    final int subTaskIndex)
  {
    return (double) (subTaskIndex + 1) / (double) SUBTASK_COUNT;
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
      .thenComposeAsync(this::saveBlob, this.inventory.databaseExecutor());
  }

  private CompletableFuture<ARInstrumentData> saveBlob(
    final ARInstrumentData instrument)
  {
    return ARInventory.executeFuture(op -> this.saveBlobOp(instrument, op));
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
    try (var transaction = this.inventory.database().openTransaction()) {
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
    return ARInventory.executeFuture(op -> this.copyFileOp(instrument, op));
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
    this.inventory.blobDirectory().copyIn(
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
    return ARInventory.executeFuture(this::parseFileOp);
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
    final var readers = this.inventory.configuration().instrumentReaders();
    try (var reader = readers.create(this.file)) {
      final var instrument = reader.execute();
      this.subtaskProgress = 1.0;
      this.publishProgressNow();
      return instrument;
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
