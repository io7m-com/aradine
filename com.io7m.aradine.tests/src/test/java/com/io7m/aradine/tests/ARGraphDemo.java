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

import com.io7m.aradine.graph.api.ARAudioGraphSettings;
import com.io7m.aradine.graph.api.ARAudioGraphStringsType;
import com.io7m.aradine.graph.api.ARAudioGraphSystemSourceAudioType;
import com.io7m.aradine.graph.api.ARAudioGraphSystemTargetAudioType;
import com.io7m.aradine.graph.api.ARAudioGraphType;
import com.io7m.aradine.graph.vanilla.ARAudioGraph;
import com.io7m.aradine.graph.vanilla.ARAudioGraphStrings;
import com.io7m.aradine.services.api.ARServiceDirectoryType;
import org.jaudiolibs.jnajack.Jack;
import org.jaudiolibs.jnajack.JackClient;
import org.jaudiolibs.jnajack.JackException;
import org.jaudiolibs.jnajack.JackPort;
import org.jaudiolibs.jnajack.JackStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;

import static org.jaudiolibs.jnajack.JackOptions.JackNoStartServer;
import static org.jaudiolibs.jnajack.JackPortFlags.JackPortIsInput;
import static org.jaudiolibs.jnajack.JackPortFlags.JackPortIsOutput;
import static org.jaudiolibs.jnajack.JackPortType.AUDIO;

public final class ARGraphDemo
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ARGraphDemo.class);

  private ARGraphDemo()
  {

  }

  private static final class Client implements Closeable
  {
    private final ARAudioGraphSystemSourceAudioType graphSource0;
    private final ARAudioGraphSystemTargetAudioType graphTarget0;
    private final ARAudioGraphType graph;
    private final Jack jack;
    private final JackClient client;
    private final JackPort input0;
    private final JackPort output0;

    Client(
      final ARServiceDirectoryType services,
      final Jack inJack)
      throws JackException
    {
      this.jack = Objects.requireNonNull(inJack, "jack");

      LOG.debug("creating client");
      final var status =
        EnumSet.noneOf(JackStatus.class);
      this.client =
        this.jack.openClient(
          "aradine",
          EnumSet.of(JackNoStartServer),
          status
        );

      LOG.debug("created client {}", this.client);
      LOG.debug(
        "sample rate: {}",
        Integer.valueOf(this.client.getSampleRate()));
      LOG.debug(
        "buffer size: {}",
        Integer.valueOf(this.client.getBufferSize()));
      LOG.debug("creating port");

      this.input0 =
        this.client.registerPort("input", AUDIO, JackPortIsInput);
      this.output0 =
        this.client.registerPort("output", AUDIO, JackPortIsOutput);

      final var settings =
        ARAudioGraphSettings.builder()
          .setSampleRate(this.client.getSampleRate())
          .setBufferSamples(this.client.getBufferSize())
          .build();

      this.graph = ARAudioGraph.create(services, settings);
      this.graphSource0 = this.graph.createSystemSourceAudio();
      this.graphTarget0 = this.graph.createSystemTargetAudio();
      this.graphSource0.port().connect(this.graphTarget0.port());

      LOG.debug("setting process callback");
      this.client.setProcessCallback(this::process);
    }

    private boolean process(
      final JackClient ignored,
      final int numFrames)
    {
      this.graph.execute(process -> {
        this.graphSource0.copyIn(this.input0.getFloatBuffer());
        process.process();
        this.graphTarget0.copyOut(this.output0.getFloatBuffer());
      });
      return true;
    }

    void start()
      throws JackException
    {
      LOG.debug("activating client");
      this.client.activate();
    }

    @Override
    public void close()
    {
      this.client.close();
    }
  }

  public static void main(
    final String[] args)
    throws Exception
  {
    final var jack = Jack.getInstance();

    final var services = new ARMutableServices();
    services.putService(
      ARAudioGraphStringsType.class,
      new ARAudioGraphStrings(Locale.ENGLISH)
    );

    try (var client = new Client(services, jack)) {
      client.start();

      while (true) {
        try {
          Thread.sleep(1_000L);
        } catch (final InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }
}
