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

package com.io7m.aradine.ensemble.internal.model;

import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.instrument.ARInstrumentInstanceID;
import com.io7m.aradine.api.instrument.ARInstrumentType;
import com.io7m.aradine.database.api.ARDBException;
import com.io7m.aradine.database.api.ARDBTransactionType;
import com.io7m.aradine.database.api.ARDBUnit;
import com.io7m.aradine.ensemble.internal.database.AREnsDB;
import com.io7m.aradine.ensemble.internal.database.AREnsQRedoPeekType;
import com.io7m.aradine.ensemble.internal.database.AREnsQUndoPeekType;
import com.io7m.aradine.ensemble.internal.database.AREnsQUndoPushType;
import com.io7m.aradine.inventory.api.ARInventoryType;
import com.io7m.jattribute.core.AttributeType;
import com.io7m.jattribute.core.Attributes;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The ensemble model.
 */

public final class AREnsModel implements AREnsModelType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(AREnsModel.class);

  private static final Attributes ATTRIBUTES =
    Attributes.create(throwable -> {
      LOG.error("Uncaught attribute exception: ", throwable);
    });

  private final AtomicBoolean closed;
  private final CloseableCollectionType<ARException> resources;
  private final ExecutorService executor;
  private final AREnsDB database;
  private final ARInventoryType inventory;
  private final CompletableFuture<Void> loading;
  private final ConcurrentSkipListMap<ARInstrumentInstanceID, ARInstrumentType> instruments;
  private final AttributeType<Optional<AREnsModelCommandRecord>> undoTip;
  private final AttributeType<Optional<AREnsModelCommandRecord>> redoTip;

  private AREnsModel(
    final CloseableCollectionType<ARException> inResources,
    final ExecutorService inExecutor,
    final AREnsDB inDatabase,
    final ARInventoryType inInventory)
  {
    this.resources =
      Objects.requireNonNull(inResources, "Resources");
    this.executor =
      Objects.requireNonNull(inExecutor, "Executor");
    this.database =
      Objects.requireNonNull(inDatabase, "Database");
    this.inventory =
      Objects.requireNonNull(inInventory, "Inventory");

    this.closed =
      new AtomicBoolean(false);
    this.loading =
      new CompletableFuture<>();
    this.instruments =
      new ConcurrentSkipListMap<>();

    this.undoTip =
      ATTRIBUTES.withValue(Optional.empty());
    this.redoTip =
      ATTRIBUTES.withValue(Optional.empty());

    this.resources.add(() -> this.onLoadingCompleted(null));
  }

  private void onLoadingCompleted(
    final Void data)
  {
    if (!this.loading.isDone()) {
      LOG.debug("Loading completed.");
      this.loading.complete(data);
    }
  }

  /**
   * Open or create a model.
   *
   * @param inventory The local inventory
   * @param file      The model file
   *
   * @return A model
   *
   * @throws ARException On errors
   */

  public static AREnsModelType open(
    final ARInventoryType inventory,
    final Path file)
    throws ARException
  {
    Objects.requireNonNull(inventory, "Inventory");
    Objects.requireNonNull(file, "File");

    final var resources =
      AREnsCloseables.create();

    try {
      final var executor =
        resources.add(
          Executors.newSingleThreadExecutor(r -> {
            final var thread = new Thread(r);
            thread.setName(threadNameOf(thread));
            return thread;
          })
        );

      final var database =
        resources.add(AREnsDB.createDatabase(file));

      final var model =
        new AREnsModel(resources, executor, database, inventory);

      model.load();
      return model;
    } catch (final Throwable e) {
      resources.close();
      throw e;
    }
  }

  private void load()
  {
    this.executor.execute(() -> {
      try {
        this.onLoadingCompleted(this.taskLoad());
      } catch (final Throwable e) {
        this.loading.completeExceptionally(e);
      }
    });
  }

  private Void taskLoad()
    throws ARDBException
  {
    try (var t = this.database.openTransaction()) {
      this.undoTip.set(t.execute(AREnsQUndoPeekType.class, ARDBUnit.UNIT));
      this.redoTip.set(t.execute(AREnsQRedoPeekType.class, ARDBUnit.UNIT));
    }
    return null;
  }

  private static String threadNameOf(
    final Thread thread)
  {
    return "com.io7m.aradine.ensemble.model-%s"
      .formatted(Long.toUnsignedString(thread.threadId()));
  }

  @Override
  public CompletableFuture<?> loading()
  {
    return this.loading;
  }

  @Override
  public CompletableFuture<?> executeCommand(
    final AREnsModelCommandType command)
  {
    final var future = new CompletableFuture<Void>();
    this.executor.execute(() -> {
      try {
        final var commandContext =
          new CommandContext();
        final var ops =
          command.compile(commandContext);

        try (var transaction = this.database.openTransaction()) {
          for (final var op : ops) {
            op.onExecute(new OpContext(commandContext));
          }
          final var record =
            this.undoStackPush(transaction, command.description(), ops);
          transaction.commit();
          this.undoTip.set(Optional.of(record));
        }
      } catch (final Throwable e) {
        LOG.debug("Command '{}' failed: ", command.description(), e);
        future.completeExceptionally(e);
      }
    });
    return future;
  }

  private AREnsModelCommandRecord undoStackPush(
    final ARDBTransactionType transaction,
    final String description,
    final List<AREnsModelOpType> ops)
    throws ARDBException
  {
    final var record =
      new AREnsModelCommandRecord(
        this.undoNextID(),
        OffsetDateTime.now(ZoneOffset.UTC),
        description,
        ops
      );

    transaction.execute(AREnsQUndoPushType.class, record);
    return record;
  }

  private long undoNextID()
  {
    return this.undoTip.get()
      .map(r -> Long.valueOf(r.id() + 1L))
      .orElse(Long.valueOf(0L))
      .longValue();
  }

  @Override
  public void close()
    throws ARException
  {
    if (this.closed.compareAndSet(false, true)) {
      this.resources.close();

      try {
        this.executor.awaitTermination(10L, TimeUnit.SECONDS);
      } catch (final InterruptedException e) {
        throw AREnsExceptions.wrap(e);
      }
    }
  }

  @Override
  public boolean isClosed()
  {
    return this.closed.get();
  }

  private static final class CommandContext
    implements AREnsModelCommandContextType
  {
    CommandContext()
    {

    }
  }

  private static final class OpContext
    implements AREnsModelOpContextType
  {
    private final CommandContext commandContext;

    OpContext(final CommandContext inCommandContext)
    {
      this.commandContext = inCommandContext;
    }
  }
}
