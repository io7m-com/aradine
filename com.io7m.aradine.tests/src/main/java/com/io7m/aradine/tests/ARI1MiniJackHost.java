/*
 * Copyright © 2022 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

import com.io7m.aradine.api.instrument.ARInstrumentInstanceID;
import com.io7m.aradine.instrument.grain_sampler_m0.ARIGM0SamplerFactory;
import com.io7m.aradine.instrument.spi1.ARI1DottedName;
import com.io7m.aradine.instrument.spi1.ARI1EventConfigurationBufferSizeChanged;
import com.io7m.aradine.instrument.spi1.ARI1EventConfigurationParameterChanged;
import com.io7m.aradine.instrument.spi1.ARI1EventConfigurationSampleRateChanged;
import com.io7m.aradine.instrument.spi1.ARI1EventConfigurationType;
import com.io7m.aradine.instrument.spi1.ARI1EventNoteOff;
import com.io7m.aradine.instrument.spi1.ARI1EventNoteOn;
import com.io7m.aradine.instrument.spi1.ARI1EventNotePitchBend;
import com.io7m.aradine.instrument.spi1.ARI1EventNoteType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterNumber;
import com.io7m.aradine.instrument.spi1.ARI1ParameterRealType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterSampleMapType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterType;
import com.io7m.aradine.instrument.spi1.ARI1PortNumber;
import com.io7m.aradine.instrument.spi1.ARI1PortSourceNoteType;
import com.io7m.aradine.instrument.spi1.ARI1PortTargetAudioType;
import com.io7m.aradine.instrument.spi1.ARI1SampleMapID;
import com.io7m.aradine.instrument.spi1.ARI1Version;
import com.io7m.jsamplebuffer.xmedia.SXMSampleBufferRateConverters;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import org.jaudiolibs.jnajack.Jack;
import org.jaudiolibs.jnajack.JackClient;
import org.jaudiolibs.jnajack.JackException;
import org.jaudiolibs.jnajack.JackMidi;
import org.jaudiolibs.jnajack.JackPort;
import org.jaudiolibs.jnajack.JackStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.jaudiolibs.jnajack.JackOptions.JackNoStartServer;
import static org.jaudiolibs.jnajack.JackPortFlags.JackPortIsInput;
import static org.jaudiolibs.jnajack.JackPortFlags.JackPortIsOutput;
import static org.jaudiolibs.jnajack.JackPortFlags.JackPortIsPhysical;
import static org.jaudiolibs.jnajack.JackPortType.AUDIO;
import static org.jaudiolibs.jnajack.JackPortType.MIDI;

public final class ARI1MiniJackHost
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ARI1MiniJackHost.class);

  private ARI1MiniJackHost()
  {

  }

  public static void main(
    final String[] args)
    throws Exception
  {
    final var jack =
      Jack.getInstance();

    final var status =
      EnumSet.noneOf(JackStatus.class);

    final var client =
      jack.openClient("sampler0", EnumSet.of(JackNoStartServer), status);

    final var outL =
      client.registerPort("outL", AUDIO, JackPortIsOutput);
    final var outR =
      client.registerPort("outR", AUDIO, JackPortIsOutput);

    final var inL =
      client.registerPort("inL", AUDIO, JackPortIsInput);
    final var inR =
      client.registerPort("inR", AUDIO, JackPortIsInput);

    final var inM =
      client.registerPort("inM", MIDI, JackPortIsInput);

    final var samplers =
      new ARIGM0SamplerFactory();

    final var services =
      ARI1MiniInstrumentContext.create(
        samplers,
        ARInstrumentInstanceID.ofString("9bac8eb0-f217-4e2b-89a2-8d1a02d4b196"),
        client.getSampleRate(),
        client.getBufferSize()
      );

    final var messages =
      new ConcurrentLinkedQueue<ARI1EventConfigurationType>();

    client.setBuffersizeCallback((c, size) -> {
      services.setBufferSize(size);
      messages.add(new ARI1EventConfigurationBufferSizeChanged());
    });

    client.setSampleRateCallback((c, rate) -> {
      services.setSampleRate(rate);
      messages.add(new ARI1EventConfigurationSampleRateChanged());
    });

    final var sampler =
      samplers.createInstrument(services);

    final var samplerOutL =
      (ARI1PortTargetAudio)
        services.declaredPort(
          new ARI1PortNumber(0),
          ARI1PortTargetAudioType.class
        );

    final var samplerOutR =
      (ARI1PortTargetAudio)
        services.declaredPort(
          new ARI1PortNumber(1),
          ARI1PortTargetAudioType.class
        );

    final var samplerNoteIn =
      (ARI1PortSourceNote)
        services.declaredPort(
          new ARI1PortNumber(2),
          ARI1PortSourceNoteType.class
        );

    final var parameterSampleMap =
      (ARI1ParameterSampleMap)
        services.declaredParameter(
          new ARI1ParameterNumber(0),
          ARI1ParameterSampleMapType.class
        );

    final var parameterSpeed =
      (ARI1ParameterReal)
        services.declaredParameter(
          new ARI1ParameterNumber(1),
          ARI1ParameterRealType.class
        );

    final var parameterGrainJitter =
      (ARI1ParameterReal)
        services.declaredParameter(
          new ARI1ParameterNumber(2),
          ARI1ParameterRealType.class
        );

    final var parameterGrainLength =
      (ARI1ParameterReal)
        services.declaredParameter(
          new ARI1ParameterNumber(3),
          ARI1ParameterRealType.class
        );

    parameterSpeed.valueChange(0, 0.125);
    parameterGrainJitter.valueChange(0, 1);
    parameterGrainLength.valueChange(0, 10.0);

    final var converters =
      new SXMSampleBufferRateConverters();
    final var converter =
      converters.createConverter();

    final var sampleDescriptions = new Int2ObjectRBTreeMap<Path>();
    sampleDescriptions.put(62, Paths.get("60.wav"));
    sampleDescriptions.put(64, Paths.get("62.wav"));
    sampleDescriptions.put(65, Paths.get("61.wav"));
    sampleDescriptions.put(66, Paths.get("63.wav"));

    final var sampleMap =
      new ARI1SampleMapDescription(sampleDescriptions)
        .load(converter, services.statusCurrentSampleRate());

    final var sampleMapId =
      new ARI1SampleMapID(
        new ARI1DottedName("com.io7m.aradine"),
        new ARI1DottedName("com.io7m.aradine.example"),
        ARI1Version.of(1, 0, 0)
      );

    services.sampleMapRegister(
      sampleMapId,
      sampleMap
    );

    messages.add(
      new ARI1EventConfigurationParameterChanged(0, parameterSampleMap.id())
    );

    client.setProcessCallback((c, nframes) -> {
      while (!messages.isEmpty()) {
        final var message = messages.poll();
        if (message instanceof final ARI1EventConfigurationParameterChanged e) {
          if (Objects.equals(e.parameter(), parameterSampleMap.id())) {
            parameterSampleMap.valueChange(0, sampleMapId);
          }
        }
        sampler.receiveEvent(services, message);
      }

      try {
        /* XXX: Obviously need some superclass here that can't be observed by instruments. */
        for (final ARI1ParameterType parameter : services.declaredParameters().values()) {
          switch (parameter) {
            case final ARI1ParameterInteger p -> {
              p.valueChangesClear();
              continue;
            }
            case final ARI1ParameterReal r -> {
              r.valueChangesClear();
              continue;
            }
            case final ARI1ParameterSampleMap s -> {
              s.valueChangesClear();
              continue;
            }
            case null -> {
              continue;
            }
            case ARI1ParameterType _ -> {
              continue;
            }
          }
        }

        final var eventCount =
          JackMidi.getEventCount(inM);
        final var event =
          new JackMidi.Event();

        for (var index = 0; index < eventCount; ++index) {
          JackMidi.eventGet(event, inM, index);
          final var size = event.size();
          final var data = new byte[size];
          event.read(data);

          final var parsedEvent = parseEvent(data, event.time());
          if (parsedEvent == null) {
            continue;
          }

          samplerNoteIn.eventAdd(parsedEvent);
        }
      } catch (final JackException e) {
        throw new RuntimeException(e);
      }

      sampler.process(services);

      final var jackBufferL = outL.getFloatBuffer();
      final var jackBufferR = outR.getFloatBuffer();
      final var outBufferL = samplerOutL.buffer();
      final var outBufferR = samplerOutR.buffer();

      for (int index = 0; index < nframes; ++index) {
        jackBufferL.put(index, (float) outBufferL.get(index));
        jackBufferR.put(index, (float) outBufferR.get(index));
      }

      return true;
    });

    autoconnect(jack, client, List.of(outL, outR));

    client.activate();

    while (true) {
      try {
        Thread.sleep(1000L);

        // parameterSpeed.valueChange(0, Math.random());
        // parameterGrainLength.valueChange(0, Math.random() * 40.0);

        // messages.add(new ARI1EventConfigurationParameterChanged(0, parameterSpeed.number()));
        // messages.add(new ARI1EventConfigurationParameterChanged(0, parameterGrainLength.number()));

      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private static ARI1EventNoteType parseEvent(
    final byte[] data,
    final int time)
  {
    if (data.length > 0) {
      final var status = (data[0] & 0b11110000) >>> 4;
      final var channel = (data[0] & 0b00001111);

      if (status == 9) {
        final var note = (int) data[1] & 0xff;
        final var velo = (int) data[2] & 0xff;
        final var velF = (double) velo / 127.0;
        return new ARI1EventNoteOn(time, note, velF);
      }

      if (status == 8) {
        final var note = (int) data[1] & 0xff;
        final var velo = (int) data[2] & 0xff;
        final var velF = (double) velo / 127.0;
        return new ARI1EventNoteOff(time, note, velF);
      }

      if (status == 14) {
        final var lsb = (int) data[1];
        final var msb = (int) data[2];
        final var val = (msb << 8) | lsb;
        final var valD = ((double) val) / 32768.0;
        final var valS = (valD * 2.0) - 1.0;
        return new ARI1EventNotePitchBend(time, valS);
      }
    }

    return null;
  }

  private static void autoconnect(
    final Jack jack,
    final JackClient client,
    final List<JackPort> outputs)
    throws JackException
  {
    final var physical =
      jack.getPorts(
        client,
        null,
        AUDIO,
        EnumSet.of(JackPortIsInput, JackPortIsPhysical)
      );

    final var count = Math.min(outputs.size(), physical.length);
    for (var index = 0; index < count; index++) {
      jack.connect(client, outputs.get(index).getName(), physical[index]);
    }
  }
}
