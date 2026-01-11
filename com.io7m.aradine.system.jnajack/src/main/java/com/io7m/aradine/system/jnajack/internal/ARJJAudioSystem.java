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

package com.io7m.aradine.system.jnajack.internal;

import com.io7m.aradine.api.ARCloseables;
import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.system.ARAudioSystemAttributes;
import com.io7m.aradine.api.system.ARAudioSystemAttributesType;
import com.io7m.aradine.api.system.ARAudioSystemEventPortConnected;
import com.io7m.aradine.api.system.ARAudioSystemEventPortDisconnected;
import com.io7m.aradine.api.system.ARAudioSystemEventProcessingError;
import com.io7m.aradine.api.system.ARAudioSystemEventType;
import com.io7m.aradine.api.system.ARAudioSystemEventXRun;
import com.io7m.aradine.api.system.ARAudioSystemType;
import com.io7m.aradine.system.jnajack.ARJJConfiguration;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import org.jaudiolibs.jnajack.Jack;
import org.jaudiolibs.jnajack.JackClient;
import org.jaudiolibs.jnajack.JackException;
import org.jaudiolibs.jnajack.JackPort;
import org.jaudiolibs.jnajack.JackPortConnectCallback;
import org.jaudiolibs.jnajack.JackPortFlags;
import org.jaudiolibs.jnajack.JackPortType;
import org.jaudiolibs.jnajack.JackStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

import static org.jaudiolibs.jnajack.JackOptions.JackNoStartServer;

/**
 * A {@code jnajack} audio system.
 */

public final class ARJJAudioSystem
  implements ARAudioSystemType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ARJJAudioSystem.class);

  private final CloseableCollectionType<ARException> resources;
  private final ARAudioSystemAttributesType attributes;
  private final JackClient client;
  private final SortedMap<String, JackPort> targetAudios;
  private final SortedMap<String, JackPort> targetMidis;
  private final SortedMap<String, JackPort> sourceAudios;
  private final SortedMap<String, JackPort> sourceMidis;
  private final SubmissionPublisher<ARAudioSystemEventType> events;

  private ARJJAudioSystem(
    final CloseableCollectionType<ARException> inResources,
    final ARAudioSystemAttributesType inAttributes,
    final JackClient inClient,
    final SortedMap<String, JackPort> inTargetAudios,
    final SortedMap<String, JackPort> inTargetMidis,
    final SortedMap<String, JackPort> inSourceAudios,
    final SortedMap<String, JackPort> inSourceMidis)
  {
    this.resources =
      Objects.requireNonNull(inResources, "Resources");
    this.attributes =
      Objects.requireNonNull(inAttributes, "Attributes");
    this.client =
      Objects.requireNonNull(inClient, "Client");
    this.targetAudios =
      inTargetAudios;
    this.targetMidis =
      inTargetMidis;
    this.sourceAudios =
      inSourceAudios;
    this.sourceMidis =
      inSourceMidis;
    this.events =
      this.resources.add(new SubmissionPublisher<>());
  }

  /**
   * Create an audio system.
   *
   * @param configuration The configuration
   *
   * @return The audio system
   *
   * @throws ARException On errors
   */

  public static ARAudioSystemType create(
    final ARJJConfiguration configuration)
    throws ARException
  {
    Objects.requireNonNull(configuration, "Configuration");

    final var resources =
      ARCloseables.create();
    final var status =
      EnumSet.noneOf(JackStatus.class);

    try {
      final var attributes =
        new ARAudioSystemAttributes();

      LOG.trace("Obtaining Jack instance...");
      final var jack = Jack.getInstance();

      final var client = openClient(configuration, jack, status, resources);
      configureNumericCallbacks(client, attributes);

      final var targetCount =
        configuration.portTargetCount();
      final var sourceCount =
        configuration.portSourceCount();

      final var targetAudios =
        new TreeMap<String, JackPort>();
      final var targetMidis =
        new TreeMap<String, JackPort>();
      final var sourceAudios =
        new TreeMap<String, JackPort>();
      final var sourceMidis =
        new TreeMap<String, JackPort>();

      registerOutputAudioPorts(client, targetCount, targetAudios);
      registerOutputMIDIPorts(client, targetCount, targetMidis);
      registerInputAudioPorts(client, sourceCount, sourceAudios);
      registerInputMIDIPorts(client, sourceCount, sourceMidis);

      final var system =
        new ARJJAudioSystem(
          resources,
          attributes,
          client,
          targetAudios,
          targetMidis,
          sourceAudios,
          sourceMidis
        );

      LOG.trace("Registering process callback...");
      client.setProcessCallback(system::onProcess);

      LOG.trace("Registering XRUN callback...");
      client.setXrunCallback(_ -> {
        system.events.submit(new ARAudioSystemEventXRun());
      });

      LOG.trace("Registering connection callback...");
      client.setPortConnectCallback(new ARPortConnectionCallback(system));

      LOG.trace("Activating Jack client...");
      client.activate();
      return system;
    } catch (final Throwable e) {
      LOG.trace("Failed to open Jack client: ", e);
      resources.close();
      throw ARJJExceptions.wrap(e);
    }
  }

  private static void registerInputMIDIPorts(
    final JackClient client,
    final int sourceCount,
    final TreeMap<String, JackPort> sourceMidis)
    throws JackException
  {
    LOG.trace("Registering {} input MIDI ports...", sourceCount);
    for (var index = 0; index < sourceCount; ++index) {
      final var name = "MIDI-Input-%d".formatted(Integer.valueOf(index));
      LOG.trace("Register: {}", name);
      sourceMidis.put(
        name,
        client.registerPort(
          name,
          JackPortType.MIDI,
          JackPortFlags.JackPortIsInput
        )
      );
    }
  }

  private static void registerInputAudioPorts(
    final JackClient client,
    final int sourceCount,
    final TreeMap<String, JackPort> sourceAudios)
    throws JackException
  {
    LOG.trace("Registering {} input audio ports...", sourceCount);
    for (var index = 0; index < sourceCount; ++index) {
      final var name = "Audio-Input-%d".formatted(Integer.valueOf(index));
      LOG.trace("Register: {}", name);
      sourceAudios.put(
        name,
        client.registerPort(
          name,
          JackPortType.AUDIO,
          JackPortFlags.JackPortIsInput
        )
      );
    }
  }

  private static void registerOutputMIDIPorts(
    final JackClient client,
    final int targetCount,
    final TreeMap<String, JackPort> targetMidis)
    throws JackException
  {
    LOG.trace("Registering {} output MIDI ports...", targetCount);
    for (var index = 0; index < targetCount; ++index) {
      final var name = "MIDI-Output-%d".formatted(Integer.valueOf(index));
      LOG.trace("Register: {}", name);
      targetMidis.put(
        name,
        client.registerPort(
          name,
          JackPortType.MIDI,
          JackPortFlags.JackPortIsOutput
        )
      );
    }
  }

  private static void registerOutputAudioPorts(
    final JackClient client,
    final int targetCount,
    final TreeMap<String, JackPort> targetAudios)
    throws JackException
  {
    LOG.trace("Registering {} output audio ports...", targetCount);
    for (var index = 0; index < targetCount; ++index) {
      final var name = "Audio-Output-%d".formatted(Integer.valueOf(index));
      LOG.trace("Register: {}", name);
      targetAudios.put(
        name,
        client.registerPort(
          name,
          JackPortType.AUDIO,
          JackPortFlags.JackPortIsOutput
        )
      );
    }
  }

  private static void configureNumericCallbacks(
    final JackClient client,
    final ARAudioSystemAttributes attributes)
    throws JackException
  {
    LOG.trace("Setting buffer size callback...");
    client.setBuffersizeCallback((_, size) -> {
      attributes.setBufferSize(size);
    });
    LOG.trace("Setting sample rate callback...");
    client.setSampleRateCallback((_, rate) -> {
      attributes.setSampleRate(rate);
    });
    LOG.trace("Jack sample rate: {}", client.getSampleRate());
    LOG.trace("Jack buffer size: {}", client.getBufferSize());
  }

  private static JackClient openClient(
    final ARJJConfiguration configuration,
    final Jack jack,
    final EnumSet<JackStatus> status,
    final CloseableCollectionType<ARException> resources)
    throws JackException
  {
    LOG.trace("Opening Jack client ({})...", configuration.applicationName());
    final var client =
      jack.openClient(
        configuration.applicationName(),
        EnumSet.of(JackNoStartServer),
        status
      );
    resources.add(client::close);
    return client;
  }

  private boolean onProcess(
    final JackClient jackClient,
    final int numFrames)
  {
    try {
      this.copyAudioIn(numFrames);
      this.copyMIDIIn(numFrames);
      this.doProcess(numFrames);
      this.copyAudioOut(numFrames);
      this.copyMIDIOut(numFrames);
      return true;
    } catch (final Throwable e) {
      try {
        this.events.submit(new ARAudioSystemEventProcessingError(e));
      } catch (Throwable _) {
        // Ignore
      }
      return true;
    }
  }

  private void copyMIDIIn(
    final int numFrames)
  {

  }

  private void copyAudioIn(
    final int numFrames)
  {

  }

  private void doProcess(
    final int numFrames)
  {

  }

  private void copyMIDIOut(
    final int numFrames)
  {

  }

  private void copyAudioOut(
    final int numFrames)
  {
    for (final var entry : this.targetAudios.entrySet()) {
      final var port = entry.getValue();
      final var jackBuffer = port.getFloatBuffer();
      for (int index = 0; index < numFrames; ++index) {
        jackBuffer.put(index, 0.0f);
      }
    }
  }

  @Override
  public String toString()
  {
    return "[ARJJAudioSystem 0x%s]"
      .formatted(Integer.toUnsignedString(this.hashCode(), 16));
  }

  @Override
  public Flow.Publisher<ARAudioSystemEventType> events()
  {
    return this.events;
  }

  @Override
  public ARAudioSystemAttributesType attributes()
  {
    return this.attributes;
  }

  @Override
  public void close()
    throws ARException
  {
    this.resources.close();
  }

  private static final class ARPortConnectionCallback
    implements JackPortConnectCallback
  {
    private final ARJJAudioSystem system;

    ARPortConnectionCallback(
      final ARJJAudioSystem inSystem)
    {
      this.system =
        Objects.requireNonNull(inSystem, "System");
    }

    @Override
    public void portsConnected(
      final JackClient ignored,
      final String portName1,
      final String portName2)
    {
      this.system.events.submit(
        new ARAudioSystemEventPortConnected(portName1, portName2)
      );
    }

    @Override
    public void portsDisconnected(
      final JackClient ignored,
      final String portName1,
      final String portName2)
    {
      this.system.events.submit(
        new ARAudioSystemEventPortDisconnected(portName1, portName2)
      );
    }
  }
}
