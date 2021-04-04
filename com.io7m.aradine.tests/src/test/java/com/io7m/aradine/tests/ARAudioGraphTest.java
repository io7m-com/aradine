/*
 * Copyright © 2021 Mark Raynsford <code@io7m.com> http://io7m.com
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

package com.io7m.aradine.tests;

import com.io7m.aradine.graph.api.ARAudioGraphConnectionAudio;
import com.io7m.aradine.graph.api.ARAudioGraphException;
import com.io7m.aradine.graph.api.ARAudioGraphListenerType;
import com.io7m.aradine.graph.api.ARAudioGraphNodeType;
import com.io7m.aradine.graph.api.ARAudioGraphProcessingType;
import com.io7m.aradine.graph.api.ARAudioGraphSettings;
import com.io7m.aradine.graph.api.ARAudioGraphStringsType;
import com.io7m.aradine.graph.api.ARAudioGraphType;
import com.io7m.aradine.graph.vanilla.ARAudioGraph;
import com.io7m.aradine.graph.vanilla.ARAudioGraphStrings;
import com.io7m.aradine.instrument.metadata.ARInstrumentPortID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ARAudioGraphTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ARAudioGraphTest.class);

  private ARMutableServices services;
  private ARAudioGraphType graph;
  private ArrayList<ARAudioGraphNodeType> nodesProcessed;
  private Listener listener;
  private ArrayList<ARAudioGraphConnectionAudio> connections;
  private ARAudioGraphSettings settings;
  private ArrayList<ARAudioGraphConnectionAudio> disconnections;

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.services = new ARMutableServices();
    this.services.putService(
      ARAudioGraphStringsType.class,
      new ARAudioGraphStrings(Locale.ENGLISH)
    );

    this.settings =
      ARAudioGraphSettings.builder()
        .setBufferSamples(512)
        .setSampleRate(48000)
        .build();

    this.graph =
      ARAudioGraph.create(
        this.services,
        this.settings
      );

    this.nodesProcessed =
      new ArrayList<>();
    this.connections =
      new ArrayList<>();
    this.disconnections =
      new ArrayList<>();

    this.listener = new Listener(this);
    this.graph.addListener(this.listener);
  }

  private static final class Listener implements ARAudioGraphListenerType
  {
    private final ARAudioGraphTest graph;

    private Listener(
      final ARAudioGraphTest inGraph)
    {
      this.graph = Objects.requireNonNull(inGraph, "graph");
    }

    @Override
    public void onProcessStarted()
      throws Exception
    {
      LOG.debug("onProcessStarted");
    }

    @Override
    public void onProcess(
      final ARAudioGraphNodeType node)
    {
      LOG.debug("onProcess: {}", node);
      this.graph.nodesProcessed.add(node);
    }

    @Override
    public void onProcessFinished()
      throws Exception
    {
      LOG.debug("onProcessFinished");
    }

    @Override
    public void onPortConnect(
      final ARAudioGraphConnectionAudio connection)
    {
      LOG.debug("onConnect: {}", connection);
      this.graph.connections.add(connection);
    }

    @Override
    public void onPortDisconnect(
      final ARAudioGraphConnectionAudio connection)
      throws Exception
    {
      LOG.debug("onDisconnect: {}", connection);
      this.graph.disconnections.add(connection);
    }
  }

  @Test
  public void testGraphProcessEmpty()
  {
    this.graph.execute(ARAudioGraphProcessingType::process);
    assertEquals(0, this.nodesProcessed.size());
  }

  @Test
  public void testGraphProcessUnconnected()
  {
    final var i =
      this.graph.createAudioSystemSource();
    final var o =
      this.graph.createAudioSystemTarget();

    this.graph.execute(ARAudioGraphProcessingType::process);
    assertEquals(2, this.nodesProcessed.size());
    assertTrue(this.nodesProcessed.contains(i));
    assertTrue(this.nodesProcessed.contains(o));
  }

  @Test
  public void testGraphProcessConnected()
    throws Exception
  {
    final var i =
      this.graph.createAudioSystemSource();
    final var o =
      this.graph.createAudioSystemTarget();

    final var connection = i.port().connect(o.port());
    assertEquals(1, this.connections.size());
    assertEquals(connection, this.connections.remove(0));

    final var inputArray = new float[this.settings.bufferSamples()];
    final var inputBuffer = FloatBuffer.wrap(inputArray);
    final var outputArray = new float[this.settings.bufferSamples()];
    final var outputBuffer = FloatBuffer.wrap(outputArray);

    Arrays.fill(inputArray, 0.25f);
    Arrays.fill(outputArray, 1.0f);

    this.graph.execute(processing -> {
      i.copyIn(inputBuffer);
      processing.process();
      o.copyOut(outputBuffer);
    });
    assertEquals(2, this.nodesProcessed.size());
    assertEquals(i, this.nodesProcessed.remove(0));
    assertEquals(o, this.nodesProcessed.remove(0));

    assertEquals(0.25f, outputArray[0]);
  }

  @Test
  public void testGraphConnectTwice()
    throws Exception
  {
    final var i0 =
      this.graph.createAudioSystemSource();
    final var i1 =
      this.graph.createAudioSystemSource();
    final var o =
      this.graph.createAudioSystemTarget();

    final var connection = i0.port().connect(o.port());
    assertEquals(1, this.connections.size());
    assertEquals(connection, this.connections.remove(0));

    final var ex =
      assertThrows(
        ARAudioGraphException.class, () -> i1.port().connect(o.port()));

    LOG.debug("exception: ", ex);
    assertEquals("errorPortAlreadyConnected", ex.errorCode());
  }

  @Test
  public void testGraphDisconnectNever()
    throws Exception
  {
    final var i =
      this.graph.createAudioSystemSource();
    final var o =
      this.graph.createAudioSystemTarget();

    final var ex =
      assertThrows(
        ARAudioGraphException.class, () -> i.port().disconnect(o.port()));

    LOG.debug("exception: ", ex);
    assertEquals("errorPortNotConnected", ex.errorCode());
  }

  @Test
  public void testGraphSum()
    throws Exception
  {
    final var i0 =
      this.graph.createAudioSystemSource();
    final var i1 =
      this.graph.createAudioSystemSource();
    final var i2 =
      this.graph.createAudioSystemSource();
    final var s =
      this.graph.createAudioSum();
    final var o =
      this.graph.createAudioSystemTarget();

    final var spt0 =
      s.createPortTarget(ARInstrumentPortID.of("0"));
    final var spt1 =
      s.createPortTarget(ARInstrumentPortID.of("1"));
    final var spt2 =
      s.createPortTarget(ARInstrumentPortID.of("2"));

    final var sps0 =
      s.port();

    i0.port().connect(spt0);
    i1.port().connect(spt1);
    i2.port().connect(spt2);
    sps0.connect(o.port());

    final var inputArray = new float[this.settings.bufferSamples()];
    final var inputBuffer = FloatBuffer.wrap(inputArray);
    final var outputArray = new float[this.settings.bufferSamples()];
    final var outputBuffer = FloatBuffer.wrap(outputArray);

    Arrays.fill(inputArray, 1.0f);
    Arrays.fill(outputArray, 0.0f);

    this.graph.execute(processing -> {
      i0.copyIn(inputBuffer);
      i1.copyIn(inputBuffer);
      i2.copyIn(inputBuffer);
      processing.process();
      o.copyOut(outputBuffer);
    });

    assertEquals(5, this.nodesProcessed.size());
    assertEquals(i0, this.nodesProcessed.remove(0));
    assertEquals(i1, this.nodesProcessed.remove(0));
    assertEquals(i2, this.nodesProcessed.remove(0));
    assertEquals(s, this.nodesProcessed.remove(0));
    assertEquals(o, this.nodesProcessed.remove(0));
    assertEquals(3.0f, outputArray[0]);

    i0.port().disconnect(spt0);

    Arrays.fill(inputArray, 1.0f);
    Arrays.fill(outputArray, 0.0f);

    this.graph.execute(processing -> {
      i0.copyIn(inputBuffer);
      i1.copyIn(inputBuffer);
      i2.copyIn(inputBuffer);
      processing.process();
      o.copyOut(outputBuffer);
    });

    assertEquals(5, this.nodesProcessed.size());
    assertEquals(i0, this.nodesProcessed.remove(0));
    assertEquals(i1, this.nodesProcessed.remove(0));
    assertEquals(i2, this.nodesProcessed.remove(0));
    assertEquals(s, this.nodesProcessed.remove(0));
    assertEquals(o, this.nodesProcessed.remove(0));
    assertEquals(2.0f, outputArray[0]);

    i1.port().disconnect(spt1);

    Arrays.fill(inputArray, 1.0f);
    Arrays.fill(outputArray, 0.0f);

    this.graph.execute(processing -> {
      i0.copyIn(inputBuffer);
      i1.copyIn(inputBuffer);
      i2.copyIn(inputBuffer);
      processing.process();
      o.copyOut(outputBuffer);
    });

    assertEquals(5, this.nodesProcessed.size());
    assertEquals(i0, this.nodesProcessed.remove(0));
    assertEquals(i1, this.nodesProcessed.remove(0));
    assertEquals(i2, this.nodesProcessed.remove(0));
    assertEquals(s, this.nodesProcessed.remove(0));
    assertEquals(o, this.nodesProcessed.remove(0));
    assertEquals(1.0f, outputArray[0]);

    i2.port().disconnect(spt2);

    Arrays.fill(inputArray, 1.0f);
    Arrays.fill(outputArray, 10.0f);

    this.graph.execute(processing -> {
      i0.copyIn(inputBuffer);
      i1.copyIn(inputBuffer);
      i2.copyIn(inputBuffer);
      processing.process();
      o.copyOut(outputBuffer);
    });

    assertEquals(5, this.nodesProcessed.size());
    assertEquals(i0, this.nodesProcessed.remove(0));
    assertEquals(i1, this.nodesProcessed.remove(0));
    assertEquals(i2, this.nodesProcessed.remove(0));
    assertEquals(s, this.nodesProcessed.remove(0));
    assertEquals(o, this.nodesProcessed.remove(0));
    assertEquals(0.0f, outputArray[0]);
  }

  @Test
  public void testGraphSumPortDuplicate()
    throws Exception
  {
    final var s =
      this.graph.createAudioSum();

    s.createPortTarget("0");

    final var ex =
      assertThrows(
        ARAudioGraphException.class,
        () -> {
          s.createPortTarget("0");
        });

    LOG.debug("exception: ", ex);
    assertEquals("errorPortAlreadyExists", ex.errorCode());
  }

  @Test
  public void testGraphCycle()
    throws Exception
  {
    final var s0 =
      this.graph.createAudioSum();
    final var s1 =
      this.graph.createAudioSum();

    final var p0 =
      s0.createPortTarget("0");
    final var p1 =
      s1.createPortTarget("0");

    p0.connect(s1.port());

    final var ex =
      assertThrows(
        ARAudioGraphException.class,
        () -> {
          p1.connect(s0.port());
        });

    LOG.debug("exception: ", ex);
    assertEquals("errorCyclic", ex.errorCode());
  }
}
