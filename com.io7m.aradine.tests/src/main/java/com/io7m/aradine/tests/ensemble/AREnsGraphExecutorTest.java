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

package com.io7m.aradine.tests.ensemble;

import com.io7m.aradine.api.instrument.ARInstrumentInstanceID;
import com.io7m.aradine.ensemble.internal.graph.AREnsGraphExecutionStep;
import com.io7m.aradine.ensemble.internal.graph.AREnsGraphExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.channels.NotYetConnectedException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class AREnsGraphExecutorTest
{
  private ThreadFactory threadFactory;

  private static void awaitAll(
    final Map<ARInstrumentInstanceID, CompletableFuture<?>> m)
    throws Exception
  {
    final var futures = new CompletableFuture<?>[m.size()];
    m.values().toArray(futures);
    final var all = CompletableFuture.allOf(futures);
    all.get(5L, TimeUnit.SECONDS);
  }

  @BeforeEach
  public void setup()
  {
    final var threadIDs = new AtomicInteger();

    this.threadFactory =
      r -> {
        final var thread = new Thread(r);
        thread.setName("G%s".formatted(threadIDs.incrementAndGet()));
        return thread;
      };
  }

  private static final class DirectExecutor
    implements Executor, AutoCloseable
  {
    DirectExecutor()
    {

    }

    @Override
    public void execute(
      final Runnable command)
    {
      command.run();
    }

    @Override
    public void close()
    {

    }
  }

  @Test
  public void testSynchronousCrashInstrument()
    throws Exception
  {
    try (var srv = new DirectExecutor()) {
      final var ins0 =
        new CrashInstrument("b74d7e9e-61b4-4014-9c6c-f9b79d605684");

      final var executor =
        AREnsGraphExecutor.create(
          srv,
          List.of(
            new AREnsGraphExecutionStep(0L, Set.of(), ins0.id)
          ),
          Map.ofEntries(
            Map.entry(ins0.id, ins0)
          ),
          Duration.ofSeconds(1L)
        );

      final var m = executor.execute();
      assertInstanceOf(
        NotYetConnectedException.class,
        assertThrows(ExecutionException.class, () -> awaitAll(m))
          .getCause()
      );
    }
  }

  @Test
  public void testSynchronousNoSteps()
    throws Exception
  {
    try (var srv = new DirectExecutor()) {
      final var sequence =
        new AtomicInteger(0);
      final var ins0 =
        new Instrument(sequence, "b74d7e9e-61b4-4014-9c6c-f9b79d605684");

      final var executor =
        AREnsGraphExecutor.create(
          srv,
          List.of(),
          Map.ofEntries(
            Map.entry(ins0.id, ins0)
          ),
          Duration.ofSeconds(1L)
        );

      final var m = executor.execute();
      awaitAll(m);
      assertNull(ins0.executedName);
    }
  }

  @Test
  public void testAsynchronousNoSteps()
    throws Exception
  {
    try (var srv = Executors.newFixedThreadPool(3, this.threadFactory)) {
      final var sequence =
        new AtomicInteger(0);
      final var ins0 =
        new Instrument(sequence, "b74d7e9e-61b4-4014-9c6c-f9b79d605684");

      final var executor =
        AREnsGraphExecutor.create(
          srv,
          List.of(),
          Map.ofEntries(
            Map.entry(ins0.id, ins0)
          ),
          Duration.ofSeconds(1L)
        );

      final var m = executor.execute();
      awaitAll(m);
      assertNull(ins0.executedName);
    }
  }

  @Test
  public void testSynchronousOneStep()
    throws Exception
  {
    try (var srv = new DirectExecutor()) {
      final var sequence =
        new AtomicInteger(0);
      final var ins0 =
        new Instrument(sequence, "b74d7e9e-61b4-4014-9c6c-f9b79d605684");

      final var executor =
        AREnsGraphExecutor.create(
          srv,
          List.of(
            new AREnsGraphExecutionStep(0L, Set.of(), ins0.id)
          ),
          Map.ofEntries(
            Map.entry(ins0.id, ins0)
          ),
          Duration.ofSeconds(1L)
        );

      final var m = executor.execute();
      awaitAll(m);
      assertEquals(1, ins0.executed);
      assertEquals("main", ins0.executedName);
    }
  }

  @Test
  public void testAsynchronousOneStep()
    throws Exception
  {
    try (var srv = Executors.newFixedThreadPool(3, this.threadFactory)) {
      final var sequence =
        new AtomicInteger(0);
      final var ins0 =
        new Instrument(sequence, "b74d7e9e-61b4-4014-9c6c-f9b79d605684");

      final var executor =
        AREnsGraphExecutor.create(
          srv,
          List.of(
            new AREnsGraphExecutionStep(0L, Set.of(), ins0.id)
          ),
          Map.ofEntries(
            Map.entry(ins0.id, ins0)
          ),
          Duration.ofSeconds(1L)
        );

      final var m = executor.execute();
      awaitAll(m);
      assertEquals(1, ins0.executed);
      assertEquals("G1", ins0.executedName);
    }
  }

  @Test
  public void testSynchronousLinearDependency()
    throws Exception
  {
    try (var srv = new DirectExecutor()) {
      final var sequence =
        new AtomicInteger(0);
      final var ins0 =
        new Instrument(sequence, "b74d7e9e-61b4-4014-9c6c-f9b79d605684");
      final var ins1 =
        new Instrument(sequence, "c09621b1-b0f8-433a-9d83-aeca340b9752");
      final var ins2 =
        new Instrument(sequence, "de019aaf-222f-443f-9e49-55b4a2a3ab1b");

      final var executor =
        AREnsGraphExecutor.create(
          srv,
          List.of(
            new AREnsGraphExecutionStep(0L, Set.of(), ins0.id),
            new AREnsGraphExecutionStep(1L, Set.of(ins0.id), ins1.id),
            new AREnsGraphExecutionStep(2L, Set.of(ins1.id), ins2.id)
          ),
          Map.ofEntries(
            Map.entry(ins0.id, ins0),
            Map.entry(ins1.id, ins1),
            Map.entry(ins2.id, ins2)
          ),
          Duration.ofSeconds(1L)
        );

      final var m = executor.execute();
      awaitAll(m);
      assertEquals(1, ins0.executed);
      assertEquals(2, ins1.executed);
      assertEquals(3, ins2.executed);
    }
  }

  @Test
  public void testAsynchronousLinearDependency()
    throws Exception
  {
    try (var srv = Executors.newFixedThreadPool(3, this.threadFactory)) {
      final var sequence =
        new AtomicInteger(0);
      final var ins0 =
        new Instrument(sequence, "b74d7e9e-61b4-4014-9c6c-f9b79d605684");
      final var ins1 =
        new Instrument(sequence, "c09621b1-b0f8-433a-9d83-aeca340b9752");
      final var ins2 =
        new Instrument(sequence, "de019aaf-222f-443f-9e49-55b4a2a3ab1b");

      final var executor =
        AREnsGraphExecutor.create(
          srv,
          List.of(
            new AREnsGraphExecutionStep(0L, Set.of(), ins0.id),
            new AREnsGraphExecutionStep(1L, Set.of(ins0.id), ins1.id),
            new AREnsGraphExecutionStep(2L, Set.of(ins1.id), ins2.id)
          ),
          Map.ofEntries(
            Map.entry(ins0.id, ins0),
            Map.entry(ins1.id, ins1),
            Map.entry(ins2.id, ins2)
          ),
          Duration.ofSeconds(1L)
        );

      final var m = executor.execute();
      awaitAll(m);
      assertEquals(1, ins0.executed);
      assertEquals("G1", ins0.executedName);
      assertEquals(2, ins1.executed);
      assertEquals("G2", ins1.executedName);
      assertEquals(3, ins2.executed);
      assertEquals("G3", ins2.executedName);
    }
  }

  @Test
  public void testSynchronousBiDependency()
    throws Exception
  {
    try (var srv = new DirectExecutor()) {
      final var sequence =
        new AtomicInteger(0);
      final var ins0 =
        new Instrument(sequence, "b74d7e9e-61b4-4014-9c6c-f9b79d605684");
      final var ins1 =
        new Instrument(sequence, "c09621b1-b0f8-433a-9d83-aeca340b9752");
      final var ins2 =
        new Instrument(sequence, "de019aaf-222f-443f-9e49-55b4a2a3ab1b");

      final var executor =
        AREnsGraphExecutor.create(
          srv,
          List.of(
            new AREnsGraphExecutionStep(0L, Set.of(), ins0.id),
            new AREnsGraphExecutionStep(1L, Set.of(), ins1.id),
            new AREnsGraphExecutionStep(2L, Set.of(ins0.id, ins1.id), ins2.id)
          ),
          Map.ofEntries(
            Map.entry(ins0.id, ins0),
            Map.entry(ins1.id, ins1),
            Map.entry(ins2.id, ins2)
          ),
          Duration.ofSeconds(1L)
        );

      final var m = executor.execute();
      awaitAll(m);
      assertEquals(1, ins0.executed);
      assertEquals(2, ins1.executed);
      assertEquals(3, ins2.executed);
    }
  }

  @Test
  public void testAsynchronousBiDependency0()
    throws Exception
  {
    try (var srv = Executors.newFixedThreadPool(3, this.threadFactory)) {
      final var sequence =
        new AtomicInteger(0);
      final var ins0 =
        new Instrument(sequence, "b74d7e9e-61b4-4014-9c6c-f9b79d605684");
      final var ins1 =
        new Instrument(sequence, "c09621b1-b0f8-433a-9d83-aeca340b9752");
      final var ins2 =
        new Instrument(sequence, "de019aaf-222f-443f-9e49-55b4a2a3ab1b");

      final var executor =
        AREnsGraphExecutor.create(
          srv,
          List.of(
            new AREnsGraphExecutionStep(0L, Set.of(), ins0.id),
            new AREnsGraphExecutionStep(1L, Set.of(), ins1.id),
            new AREnsGraphExecutionStep(2L, Set.of(ins0.id, ins1.id), ins2.id)
          ),
          Map.ofEntries(
            Map.entry(ins0.id, ins0),
            Map.entry(ins1.id, ins1),
            Map.entry(ins2.id, ins2)
          ),
          Duration.ofSeconds(1L)
        );

      final var m = executor.execute();
      awaitAll(m);
      assertEquals(3, ins2.executed);
    }
  }

  @Test
  public void testAsynchronousBiDependency1()
    throws Exception
  {
    try (var srv = Executors.newFixedThreadPool(3, this.threadFactory)) {
      final var sequence =
        new AtomicInteger(0);

      final var instruments = new ArrayList<Instrument>();
      for (int index = 0; index < 10; ++index) {
        instruments.add(
          new Instrument(
            sequence,
            UUID.nameUUIDFromBytes(
              Integer.toString(index).getBytes(StandardCharsets.UTF_8)
            ).toString()
          )
        );
      }

      final var ins0 =
        new Instrument(sequence, "b74d7e9e-61b4-4014-9c6c-f9b79d605684");

      final var steps =
        new ArrayList<AREnsGraphExecutionStep>();

      for (int index = 0; index < 10; ++index) {
        steps.add(
          new AREnsGraphExecutionStep(
            index,
            Set.of(),
            instruments.get(index).id)
        );
      }

      steps.add(
        new AREnsGraphExecutionStep(
          steps.getLast().index() + 1L,
          instruments.stream()
            .map(x -> x.id)
            .collect(Collectors.toSet()),
          ins0.id
        )
      );

      final Map<ARInstrumentInstanceID, Runnable> instrumentMap =
        Stream.concat(instruments.stream(), Stream.of(ins0))
          .collect(Collectors.toMap(i -> i.id, i -> i));

      final var executor =
        AREnsGraphExecutor.create(
          srv,
          steps,
          instrumentMap,
          Duration.ofSeconds(1L)
        );

      final var m = executor.execute();
      awaitAll(m);
      assertEquals(11, ins0.executed);
    }
  }

  private static final class Instrument
    implements Runnable
  {
    private final ARInstrumentInstanceID id;
    private final AtomicInteger sequence;
    private volatile int executed;
    private volatile String executedName;

    private Instrument(
      final AtomicInteger sequence,
      final String inId)
    {
      this.sequence = sequence;
      this.id = ARInstrumentInstanceID.ofString(
        Objects.requireNonNull(inId, "ID")
      );
    }

    @Override
    public void run()
    {
      final var thread = Thread.currentThread();
      this.executed = this.sequence.addAndGet(1);
      this.executedName = thread.getName();
      System.out.printf(
        "Execute [%s] [%s] (%s)%n",
        thread.getName(),
        this.id,
        Integer.valueOf(this.executed)
      );
    }
  }

  private static final class CrashInstrument
    implements Runnable
  {
    private final ARInstrumentInstanceID id;

    private CrashInstrument(
      final String inId)
    {
      this.id = ARInstrumentInstanceID.ofString(
        Objects.requireNonNull(inId, "ID")
      );
    }

    @Override
    public void run()
    {
      throw new NotYetConnectedException();
    }
  }
}
