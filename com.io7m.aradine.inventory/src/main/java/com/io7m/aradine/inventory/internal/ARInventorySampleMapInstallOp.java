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

import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.progress.ARProgress;
import com.io7m.aradine.api.sample_map.ARSampleMapDataSummary;
import com.io7m.aradine.inventory.api.queries.ARQueryBlobPutType;
import com.io7m.aradine.inventory.api.queries.ARQuerySampleMapPutType;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

final class ARInventorySampleMapInstallOp
  implements ARInventoryOpType<ARSampleMapDataSummary>
{
  private static final int SUBTASK_COUNT = 3;
  private final ARInventory inventory;
  private final Path file;
  private final Consumer<ARProgress> progressConsumer;
  private final String task;
  private volatile double taskProgress;
  private volatile String subTask;
  private volatile double subtaskProgress;

  ARInventorySampleMapInstallOp(
    final ARInventory inInventory,
    final Path inFile,
    final Consumer<ARProgress> inProgressConsumer)
  {
    this.inventory = inInventory;
    this.file = inFile;
    this.progressConsumer = inProgressConsumer;
    this.task = "Installing sample map.";
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
  public CompletableFuture<ARSampleMapDataSummary> execute()
  {
    return this.parseFile()
      .thenCompose(this::copyFile)
      .thenComposeAsync(this::saveBlob, this.inventory.databaseExecutor());
  }

  private CompletableFuture<ARSampleMapDataSummary> saveBlob(
    final ARSampleMapDataSummary sampleMap)
  {
    return ARInventory.executeFuture(op -> this.saveBlobOp(sampleMap, op));
  }

  private ARSampleMapDataSummary saveBlobOp(
    final ARSampleMapDataSummary sampleMap,
    final CompletableFuture<ARSampleMapDataSummary> future)
    throws Exception
  {
    this.taskProgress = taskProgressOf(2);
    this.subTask = "Saving sample map to database.";
    this.subtaskProgress = 0.0;
    this.publishProgressNow();

    checkCancelled(future);
    try (var transaction = this.inventory.database().openTransaction()) {
      final var blob = sampleMap.blob();
      transaction.execute(ARQueryBlobPutType.class, blob);
      transaction.execute(ARQuerySampleMapPutType.class, sampleMap);
      transaction.commit();
      return sampleMap;
    } finally {
      this.subtaskProgress = 1.0;
      this.publishProgressNow();
    }
  }

  private CompletableFuture<ARSampleMapDataSummary> copyFile(
    final ARSampleMapDataSummary sampleMap)
  {
    return ARInventory.executeFuture(op -> this.copyFileOp(sampleMap, op));
  }

  private ARSampleMapDataSummary copyFileOp(
    final ARSampleMapDataSummary sampleMap,
    final CompletableFuture<ARSampleMapDataSummary> future)
    throws Exception
  {
    this.taskProgress = taskProgressOf(1);
    this.subTask = "Copying file to blob directory.";
    this.subtaskProgress = 0.0;
    this.publishProgressNow();

    checkCancelled(future);
    this.inventory.blobDirectory().copyIn(
      sampleMap.blob().hash(),
      this.file,
      progress -> {
        this.subtaskProgress = progress.doubleValue();
        this.publishProgressNow();
      },
      future::isCancelled
    );

    this.subtaskProgress = 1.0;
    this.publishProgressNow();
    return sampleMap;
  }

  private CompletableFuture<ARSampleMapDataSummary> parseFile()
  {
    return ARInventory.executeFuture(this::parseFileOp);
  }

  private ARSampleMapDataSummary parseFileOp(
    final CompletableFuture<ARSampleMapDataSummary> future)
    throws Exception
  {
    this.taskProgress = taskProgressOf(0);
    this.subTask = "Parsing sample map file.";
    this.subtaskProgress = 0.0;
    this.publishProgressNow();

    final var probes = this.inventory.configuration().sampleMapProbes();
    int index = 0;
    for (final var probe : probes) {
      this.subtaskProgress = (double) index / ((double) probes.size());
      checkCancelled(future);
      this.publishProgressNow();

      final var dataOpt = probe.probe(this.file);
      if (dataOpt.isPresent()) {
        this.subtaskProgress = 1.0;
        this.publishProgressNow();
        return dataOpt.get();
      }
      ++index;
    }

    throw new ARException(
      "Sample map is not supported",
      "error-sample-map-unsupported",
      Map.of("File", this.file.toString()),
      Optional.empty()
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
