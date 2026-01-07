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

import com.io7m.aradine.api.ARCloseables;
import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.instrument.ARInstrumentExecutableType;
import com.io7m.aradine.api.instrument.ARInstrumentInstanceID;
import com.io7m.aradine.api.instrument.ARInstrumentReference;
import com.io7m.aradine.api.ports.ARPort;
import com.io7m.aradine.api.ports.ARPortID;
import com.io7m.aradine.api.ports.ARPortNumber;
import com.io7m.aradine.api.system.ARAudioSystemAttributesType;
import com.io7m.aradine.database.api.ARDBTransactionType;
import com.io7m.aradine.ensemble.internal.database.AREnsDB;
import com.io7m.aradine.ensemble.internal.database.AREnsQCommandIDNextType;
import com.io7m.aradine.ensemble.internal.database.AREnsQInstrumentDeleteType;
import com.io7m.aradine.ensemble.internal.database.AREnsQInstrumentPutType;
import com.io7m.aradine.ensemble.internal.database.AREnsQPortDeleteType;
import com.io7m.aradine.ensemble.internal.database.AREnsQPortPutType;
import com.io7m.aradine.ensemble.internal.database.AREnsQRedoPeekType;
import com.io7m.aradine.ensemble.internal.database.AREnsQRedoPopType;
import com.io7m.aradine.ensemble.internal.database.AREnsQRedoPushType;
import com.io7m.aradine.ensemble.internal.database.AREnsQUndoPeekType;
import com.io7m.aradine.ensemble.internal.database.AREnsQUndoPopType;
import com.io7m.aradine.ensemble.internal.database.AREnsQUndoPushType;
import com.io7m.aradine.ensemble.internal.events.AREnsEventInstrumentClosed;
import com.io7m.aradine.ensemble.internal.events.AREnsEventInstrumentLoaded;
import com.io7m.aradine.ensemble.internal.events.AREnsEventType;
import com.io7m.aradine.ensemble.internal.graph.AREnsGraph;
import com.io7m.aradine.ensemble.internal.graph.AREnsGraphType;
import com.io7m.aradine.ensemble.internal.v1.commands.AREnsModelCommands1;
import com.io7m.aradine.ensemble.internal.v1.context.AREns1InstrumentContext;
import com.io7m.aradine.instrument.loader.api.ARInstrumentContextConstructorType;
import com.io7m.aradine.instrument.loader.api.ARInstrumentLoaderFactoryType;
import com.io7m.aradine.instrument.loader.api.ARInstrumentPortAssignerType;
import com.io7m.aradine.inventory.api.ARInventoryType;
import com.io7m.jattribute.core.AttributeType;
import com.io7m.jattribute.core.Attributes;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.io7m.aradine.database.api.ARDBUnit.UNIT;

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
  private final ARInstrumentLoaderFactoryType loaders;
  private final ARAudioSystemAttributesType audioSystemAttributes;
  private final CompletableFuture<Void> loading;
  private final AtomicReference<Map<ARInstrumentInstanceID, AREnsInstrumentType>> instruments;
  private final AttributeType<Optional<AREnsModelCommandRecord>> undoTip;
  private final AttributeType<Optional<AREnsModelCommandRecord>> redoTip;
  private final AtomicReference<AREnsGraph> graph;
  private final SubmissionPublisher<AREnsEventType> events;
  private final AREnsModelCommandDirectoryType commands;

  private AREnsModel(
    final CloseableCollectionType<ARException> inResources,
    final ExecutorService inExecutor,
    final AREnsDB inDatabase,
    final ARInventoryType inInventory,
    final ARInstrumentLoaderFactoryType inLoaders,
    final ARAudioSystemAttributesType inAudioSystemAttributesType)
  {
    this.resources =
      Objects.requireNonNull(inResources, "Resources");
    this.executor =
      Objects.requireNonNull(inExecutor, "Executor");
    this.database =
      Objects.requireNonNull(inDatabase, "Database");
    this.inventory =
      Objects.requireNonNull(inInventory, "Inventory");
    this.loaders =
      Objects.requireNonNull(inLoaders, "Loaders");
    this.audioSystemAttributes =
      Objects.requireNonNull(
        inAudioSystemAttributesType,
        "AudioSystemAttributes"
      );

    this.closed =
      new AtomicBoolean(false);
    this.loading =
      new CompletableFuture<>();
    this.instruments =
      new AtomicReference<>(Map.of());
    this.graph =
      new AtomicReference<>(AREnsGraph.create());
    this.events =
      this.resources.add(
        new SubmissionPublisher<>(Runnable::run, 1)
      );
    this.commands =
      AREnsModelCommands1.INSTANCE;

    this.undoTip =
      ATTRIBUTES.withValue(Optional.empty());
    this.redoTip =
      ATTRIBUTES.withValue(Optional.empty());

    this.resources.add(() -> this.onLoadingCompleted(null));
  }

  /**
   * Open or create a model.
   *
   * @param configuration The configuration
   *
   * @return A model
   *
   * @throws ARException On errors
   */

  public static AREnsModelType open(
    final AREnsModelConfiguration configuration)
    throws ARException
  {
    Objects.requireNonNull(configuration, "configuration");

    final var resources =
      ARCloseables.create();

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
        resources.add(AREnsDB.createDatabase(configuration.file()));

      final var model =
        new AREnsModel(
          resources,
          executor,
          database,
          configuration.inventory(),
          configuration.loaders(),
          configuration.systemAttributes()
        );

      model.load();
      return model;
    } catch (final Throwable e) {
      resources.close();
      throw e;
    }
  }

  private static String threadNameOf(
    final Thread thread)
  {
    return "com.io7m.aradine.ensemble.model-%s"
      .formatted(Long.toUnsignedString(thread.threadId()));
  }

  private static ARException errorCommandNotFound(
    final AREnsModelCommandRecord record)
  {
    return new ARException(
      "Command not found.",
      "error-command-not-found",
      Map.ofEntries(
        Map.entry("Command", record.commandClass())
      ),
      Optional.empty()
    );
  }

  private static ARException errorInstrumentNonexistent(
    final ARInstrumentInstanceID instrumentInstanceID)
  {
    return new ARException(
      "Instrument does not exist.",
      "error-instrument-nonexistent",
      Map.ofEntries(
        Map.entry("InstrumentInstanceID", instrumentInstanceID.toString())
      ),
      Optional.empty()
    );
  }

  private void onLoadingCompleted(
    final Void data)
  {
    if (!this.loading.isDone()) {
      LOG.debug("Loading completed.");
      this.loading.complete(data);
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
    throws ARException
  {
    try (var t = this.database.openTransaction()) {
      this.undoTipSet(t.execute(AREnsQUndoPeekType.class, UNIT));
      this.redoTipSet(t.execute(AREnsQRedoPeekType.class, UNIT));
    }
    return null;
  }

  @Override
  public Flow.Publisher<AREnsEventType> events()
  {
    return this.events;
  }

  @Override
  public CompletableFuture<?> loading()
  {
    return this.loading;
  }

  @Override
  public CompletableFuture<?> undo()
  {
    final var future = new CompletableFuture<Void>();
    this.executor.execute(() -> {
      try {
        final var undoOpt = this.undoTip.get();
        if (undoOpt.isEmpty()) {
          return;
        }

        final var undoNow =
          undoOpt.get();
        final var commandOpt =
          this.commands.lookupByCommandRecord(undoNow);

        if (commandOpt.isEmpty()) {
          throw errorCommandNotFound(undoNow);
        }

        final var command = commandOpt.get();
        final var newGraph = this.graph.get().copy();
        try (var transaction = this.database.openTransaction()) {
          try (var ctx = new CommandContext(this, newGraph, transaction)) {
            command.undo(ctx, undoNow.state());
            this.redoStackPush(transaction, command, undoNow.state());
            this.undoStackPop(transaction);
            transaction.commit();
            this.graph.set(newGraph);
            ctx.setSucceeded();
          }
        }
      } catch (final Throwable e) {
        LOG.debug("Command failed: ", e);
        future.completeExceptionally(e);
      } finally {
        future.complete(null);
      }
    });
    return future;
  }

  @Override
  public CompletableFuture<?> redo()
  {
    final var future = new CompletableFuture<Void>();
    this.executor.execute(() -> {
      try {
        final var redoOpt = this.redoTip.get();
        if (redoOpt.isEmpty()) {
          return;
        }

        final var redoNow =
          redoOpt.get();
        final var commandOpt =
          this.commands.lookupByCommandRecord(redoNow);

        if (commandOpt.isEmpty()) {
          throw errorCommandNotFound(redoNow);
        }

        final var command = commandOpt.get();
        final var newGraph = this.graph.get().copy();
        try (var transaction = this.database.openTransaction()) {
          try (var ctx = new CommandContext(this, newGraph, transaction)) {
            command.redo(ctx, redoNow.state());
            this.undoStackPush(transaction, command, redoNow.state());
            this.redoStackPop(transaction);
            transaction.commit();
            this.graph.set(newGraph);
            ctx.setSucceeded();
          }
        }
      } catch (final Throwable e) {
        LOG.debug("Command failed: ", e);
        future.completeExceptionally(e);
      } finally {
        future.complete(null);
      }
    });
    return future;
  }

  @Override
  public <P extends AREnsModelCommandParametersType, S extends AREnsModelCommandStateType>
  CompletableFuture<?>
  executeCommand(
    final AREnsModelCommandType<P, S> command,
    final P parameters)
  {
    Objects.requireNonNull(command, "Command");
    Objects.requireNonNull(parameters, "Parameters");

    final var future = new CompletableFuture<Void>();
    this.executor.execute(() -> {
      try {
        final var newGraph = this.graph.get().copy();
        try (var transaction = this.database.openTransaction()) {
          try (var ctx = new CommandContext(this, newGraph, transaction)) {
            switch (command.execute(ctx, parameters)) {
              case AREnsCommandNotUndoable<S> _ -> {
                // Nothing to do.
              }
              case final AREnsCommandUndoable<S> st -> {
                this.undoStackPush(transaction, command, st.state());
              }
            }
            transaction.commit();
            this.graph.set(newGraph);
            ctx.setSucceeded();
          }
        }
      } catch (final Throwable e) {
        LOG.debug("Command '{}' failed: ", command.description(), e);
        future.completeExceptionally(e);
      } finally {
        future.complete(null);
      }
    });
    return future;
  }

  private AREnsModelCommandRecord undoStackPush(
    final ARDBTransactionType transaction,
    final AREnsModelCommandType<?, ?> command,
    final AREnsModelCommandStateType state)
    throws ARException
  {
    final var record =
      new AREnsModelCommandRecord(
        this.nextCommandID(transaction),
        OffsetDateTime.now(ZoneOffset.UTC),
        command.description(),
        command.getClass().getSimpleName(),
        state
      );

    transaction.execute(AREnsQUndoPushType.class, record);
    transaction.addRunAfterCommit(() -> this.undoTipSet(Optional.of(record)));
    return record;
  }

  private Optional<AREnsModelCommandRecord> undoTipSet(
    final Optional<AREnsModelCommandRecord> record)
  {
    LOG.trace("Undo: {}", record);
    return this.undoTip.set(record);
  }

  private Optional<AREnsModelCommandRecord> redoTipSet(
    final Optional<AREnsModelCommandRecord> record)
  {
    LOG.trace("Redo: {}", record);
    return this.redoTip.set(record);
  }

  private void undoStackPop(
    final ARDBTransactionType transaction)
    throws ARException
  {
    final var recordOpt = transaction.execute(AREnsQUndoPopType.class, UNIT);
    transaction.addRunAfterCommit(() -> this.undoTipSet(recordOpt));
  }

  private void redoStackPop(
    final ARDBTransactionType transaction)
    throws ARException
  {
    final var recordOpt = transaction.execute(AREnsQRedoPopType.class, UNIT);
    transaction.addRunAfterCommit(() -> this.redoTipSet(recordOpt));
  }

  private void redoStackPush(
    final ARDBTransactionType transaction,
    final AREnsModelCommandType<?, ?> command,
    final AREnsModelCommandStateType state)
    throws ARException
  {
    final var record =
      new AREnsModelCommandRecord(
        this.nextCommandID(transaction),
        OffsetDateTime.now(ZoneOffset.UTC),
        command.description(),
        command.getClass().getSimpleName(),
        state
      );

    transaction.execute(AREnsQRedoPushType.class, record);
    transaction.addRunAfterCommit(() -> this.redoTipSet(Optional.of(record)));
  }

  private long nextCommandID(
    final ARDBTransactionType transaction)
    throws ARException
  {
    return transaction.execute(AREnsQCommandIDNextType.class, UNIT)
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

  private AREnsInstrumentType instrumentGet(
    final ARInstrumentInstanceID instrumentInstanceID)
    throws ARException
  {
    final var instrument = this.instruments.get().get(instrumentInstanceID);
    if (instrument == null) {
      throw errorInstrumentNonexistent(instrumentInstanceID);
    }
    return instrument;
  }

  private static final class CommandContext
    implements AREnsModelCommandContextType,
    ARInstrumentPortAssignerType,
    AutoCloseable
  {
    private final AREnsGraph graph;
    private final ARDBTransactionType transaction;
    private final AREnsModel model;
    private final HashMap<ARInstrumentInstanceID, AREnsInstrumentType> instrumentsToRegister;
    private final HashSet<ARInstrumentInstanceID> instrumentsToDeregister;
    private final AtomicBoolean succeeded;

    CommandContext(
      final AREnsModel inModel,
      final AREnsGraph newGraph,
      final ARDBTransactionType inTransaction)
    {
      this.model = inModel;
      this.graph = newGraph;
      this.transaction = inTransaction;
      this.instrumentsToRegister = new HashMap<>();
      this.instrumentsToDeregister = new HashSet<>();
      this.succeeded = new AtomicBoolean(false);
    }

    @Override
    public ARPortID assign(
      final ARInstrumentInstanceID instance,
      final ARPortNumber portNumber)
    {
      final var text =
        String.format("%s:%s", instance, portNumber);
      final var uuid =
        UUID.nameUUIDFromBytes(text.getBytes(StandardCharsets.UTF_8));

      return new ARPortID(uuid);
    }

    @Override
    public AREnsGraphType graph()
    {
      return this.graph;
    }

    @Override
    public ARDBTransactionType databaseTransaction()
    {
      return this.transaction;
    }

    @Override
    public ARInventoryType inventory()
    {
      return this.model.inventory;
    }

    @Override
    public ARInstrumentLoaderFactoryType instrumentLoaders()
    {
      return this.model.loaders;
    }

    @Override
    public ARInstrumentPortAssignerType instrumentPortAssigner()
    {
      return this;
    }

    @Override
    public void instrumentRegister(
      final AREnsInstrumentType instrument)
      throws ARException
    {
      final var instrumentExecutable =
        instrument.executable();
      final var instrumentDescription =
        instrumentExecutable.description();
      final var instanceID =
        instrumentDescription.instanceId();

      final var reference =
        new ARInstrumentReference(
          instanceID,
          instrumentDescription.identifier()
        );

      this.graph.instrumentRegister(reference);
      this.transaction.execute(AREnsQInstrumentPutType.class, reference);
      this.instrumentsToRegister.put(instanceID, instrument);
    }

    @Override
    public void instrumentPortRegister(
      final ARPort port)
      throws ARException
    {
      this.graph.portRegister(port);
      this.transaction.execute(AREnsQPortPutType.class, port);
    }

    @Override
    public void instrumentPortDeregister(
      final ARPort port)
      throws ARException
    {
      this.graph.portDeregister(port);
      this.transaction.execute(AREnsQPortDeleteType.class, port.id());
    }

    @Override
    public void instrumentDeregister(
      final AREnsInstrumentType instrument)
      throws ARException
    {
      final var instrumentExecutable =
        instrument.executable();
      final var instrumentDescription =
        instrumentExecutable.description();
      final var instanceID =
        instrumentDescription.instanceId();

      this.graph.instrumentDeregister(instanceID);
      this.transaction.execute(AREnsQInstrumentDeleteType.class, instanceID);
      this.instrumentsToDeregister.add(instanceID);
    }

    @Override
    public AREnsInstrumentType instrumentGet(
      final ARInstrumentInstanceID instrumentInstanceID)
      throws ARException
    {
      final var instrument =
        this.instrumentsToRegister.get(instrumentInstanceID);

      if (instrument == null) {
        return this.model.instrumentGet(instrumentInstanceID);
      }

      throw errorInstrumentNonexistent(instrumentInstanceID);
    }

    @Override
    public AREnsInstrumentV1 instrumentLoad(
      final ARInstrumentInstanceID instanceID,
      final ARInstrumentLoaderFactoryType loaders,
      final Path file)
      throws ARException
    {
      Objects.requireNonNull(instanceID, "InstanceID");
      Objects.requireNonNull(loaders, "Loaders");
      Objects.requireNonNull(file, "File");

      final var contextSaved =
        new AtomicReference<AREns1InstrumentContext>();

      final ARInstrumentContextConstructorType constructor =
        description -> {
          final var context =
            AREns1InstrumentContext.create(
              description,
              CommandContext.this.model.audioSystemAttributes
            );
          contextSaved.set(context);
          return context;
        };

      final ARInstrumentExecutableType executable;
      try (var loader = loaders.createLoader(constructor, file)) {
        executable = loader.execute(this, instanceID);
      }

      return new AREnsInstrumentV1(
        instanceID,
        executable,
        contextSaved.get()
      );
    }

    @Override
    public void close()
    {
      if (this.succeeded.get()) {
        this.closeRegisterInstruments();
      }
    }

    private void closeRegisterInstruments()
    {
      final var nextInstrumentSet =
        new HashMap<>(this.model.instruments.get());
      final var instrumentsToClose =
        new HashSet<AREnsInstrumentType>(this.instrumentsToDeregister.size());

      /*
       * Calculate the set of resulting instruments by adding new instruments
       * and removing instruments that will be closed, and then atomically
       * update the resulting set in the model.
       */

      nextInstrumentSet.putAll(this.instrumentsToRegister);
      for (final var entry : this.instrumentsToDeregister) {
        instrumentsToClose.add(nextInstrumentSet.remove(entry));
      }
      this.model.instruments.set(Map.copyOf(nextInstrumentSet));

      /*
       * Now that all the instruments have been set in the model, publish
       * events for each closed and each new instrument.
       */

      for (final var instrument : instrumentsToClose) {
        final var description =
          instrument.executable().description();

        try (var _ = instrument) {
          this.model.events.submit(
            new AREnsEventInstrumentClosed(
              description.instanceId(),
              description.identifier()
            )
          );
        } catch (final ARException e) {
          LOG.debug("Failed to close instrument: ", e);
        }
      }

      for (final var entry : this.instrumentsToRegister.entrySet()) {
        final var instanceID =
          entry.getKey();
        final var description =
          entry.getValue().executable().description();

        try {
          this.model.events.submit(
            new AREnsEventInstrumentLoaded(instanceID, description.identifier())
          );
        } catch (final Exception e) {
          // Nothing we can do about this.
        }
      }
    }

    public void setSucceeded()
    {
      this.succeeded.set(true);
    }
  }
}
