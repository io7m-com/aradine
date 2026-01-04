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

import com.io7m.aradine.api.progress.ARProgress;
import com.io7m.aradine.api.sample_map.ARSampleMapID;
import com.io7m.aradine.inventory.api.queries.ARQueryBlobDeleteType;
import com.io7m.aradine.inventory.api.queries.ARQueryBlobReferencesType;
import com.io7m.aradine.inventory.api.queries.ARQuerySampleMapDeleteType;
import com.io7m.aradine.inventory.api.queries.ARQuerySampleMapGetType;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

final class ARInventorySampleMapUninstallOp
  implements ARInventoryOpType<Object>
{
  private final ARInventory inventory;
  private final ARSampleMapID instrumentID;
  private final Consumer<ARProgress> progressConsumer;
  private String task;
  private String subTask;
  private double taskProgress;
  private double subtaskProgress;

  ARInventorySampleMapUninstallOp(
    final ARInventory inInventory,
    final ARSampleMapID inSampleMapID,
    final Consumer<ARProgress> inProgressConsumer)
  {
    this.inventory = inInventory;
    this.instrumentID = inSampleMapID;
    this.progressConsumer = inProgressConsumer;

    this.task = "Uninstalling sample map.";
    this.subTask = "Uninstalling sample map.";
    this.taskProgress = 0.0;
    this.subtaskProgress = 0.0;
  }

  @Override
  public CompletableFuture<Object> execute()
  {
    final var future = new CompletableFuture<>();
    this.inventory.databaseExecutor().execute(() -> {
      this.publishProgressNow();

      try {
        try (var t = this.inventory.database().openTransaction()) {
          final var instrumentOpt =
            t.execute(ARQuerySampleMapGetType.class, this.instrumentID);

          if (instrumentOpt.isPresent()) {
            final var sampleMap =
              instrumentOpt.get();
            final var blobHash =
              sampleMap.blob().hash();

            t.execute(
              ARQuerySampleMapDeleteType.class,
              this.instrumentID
            );

            final var refs =
              t.execute(ARQueryBlobReferencesType.class, blobHash);

            if (refs.isEmpty()) {
              this.subTask = "Deleting blob.";
              this.publishProgressNow();

              t.execute(ARQueryBlobDeleteType.class, blobHash);
              this.inventory.blobDirectory().delete(blobHash);

              this.subtaskProgress = 1.0;
              this.publishProgressNow();
            }
            t.commit();

            this.subTask = "Completed.";
            this.subtaskProgress = 1.0;
            this.taskProgress = 1.0;
            this.publishProgressNow();
          }
        }
      } catch (final Throwable e) {
        future.completeExceptionally(e);
      } finally {
        future.complete(null);
      }
    });
    return future;
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
