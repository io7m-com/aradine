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

package com.io7m.aradine.ensemble.internal.graph;

import com.io7m.aradine.api.instrument.ARInstrumentInstanceID;
import com.io7m.jaffirm.core.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An executor for a graph.
 */

public final class AREnsGraphExecutor
{
  private final Executor executorService;
  private final List<AREnsGraphExecutionStep> steps;
  private final Map<ARInstrumentInstanceID, Runnable> processables;
  private final Duration taskTimeout;

  private AREnsGraphExecutor(
    final Executor inExecutorService,
    final List<AREnsGraphExecutionStep> inSteps,
    final Map<ARInstrumentInstanceID, Runnable> inProcessables,
    final Duration inTaskTimeout)
  {
    this.executorService =
      Objects.requireNonNull(inExecutorService, "ThreadService");
    this.steps =
      List.copyOf(inSteps);
    this.processables =
      Map.copyOf(inProcessables);
    this.taskTimeout =
      Objects.requireNonNull(inTaskTimeout, "TaskTimeout");
  }

  /**
   * Create a new executor.
   *
   * @param executor     The executor service
   * @param steps        The execution steps for the graph
   * @param processables The per-instrument runnables
   * @param taskTimeout  The timeout value for processing
   *
   * @return A graph executor
   */

  public static AREnsGraphExecutor create(
    final Executor executor,
    final List<AREnsGraphExecutionStep> steps,
    final Map<ARInstrumentInstanceID, Runnable> processables,
    final Duration taskTimeout)
  {
    for (final var step : steps) {
      Preconditions.checkPrecondition(
        processables.containsKey(step.instrument()),
        "Step must have a matching processable."
      );
      for (final var depend : step.waitFor()) {
        Preconditions.checkPrecondition(
          processables.containsKey(depend),
          "Step must have a matching processable."
        );
      }
    }

    return new AREnsGraphExecutor(
      executor,
      steps,
      processables,
      taskTimeout
    );
  }

  private static void waitForDependencies(
    final AREnsGraphExecutionStep step,
    final Object2ObjectOpenHashMap<ARInstrumentInstanceID, CompletableFuture<Void>> taskFutures,
    final long waitMilliseconds)
    throws InterruptedException, ExecutionException, TimeoutException
  {
    for (final var wait : step.waitFor()) {
      final var waitFuture = taskFutures.get(wait);
      waitFuture.get(waitMilliseconds, TimeUnit.MILLISECONDS);
    }
  }

  /**
   * Execute the graph.
   *
   * @return The per-instrument futures
   */

  public Map<ARInstrumentInstanceID, CompletableFuture<?>> execute()
  {
    if (this.steps.isEmpty()) {
      return Map.of();
    }

    final var taskFutures =
      this.createTaskFutures();
    final var waitMilliseconds =
      this.taskTimeout.toMillis();

    for (final var step : this.steps) {
      final var instanceID =
        step.instrument();
      final var processable =
        this.processables.get(instanceID);
      final var futureThis =
        taskFutures.get(instanceID);

      this.executorService.execute(() -> {
        try {
          waitForDependencies(step, taskFutures, waitMilliseconds);
          processable.run();
        } catch (final Throwable e) {
          futureThis.completeExceptionally(e);
        } finally {
          futureThis.complete(null);
        }
      });
    }

    return Map.copyOf(taskFutures);
  }

  private Object2ObjectOpenHashMap<ARInstrumentInstanceID, CompletableFuture<Void>> createTaskFutures()
  {
    final var taskFutures =
      new Object2ObjectOpenHashMap<ARInstrumentInstanceID, CompletableFuture<Void>>(
        this.processables.size()
      );

    for (final var instanceID : this.processables.keySet()) {
      taskFutures.put(instanceID, new CompletableFuture<>());
    }
    return taskFutures;
  }
}
