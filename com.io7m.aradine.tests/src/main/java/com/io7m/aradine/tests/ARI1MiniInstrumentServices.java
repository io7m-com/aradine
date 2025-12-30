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

import com.io7m.aradine.annotations.ARTimeFrames;
import com.io7m.aradine.annotations.ARTimeMilliseconds;
import com.io7m.aradine.api.instrument.ARInstrumentInstanceID;
import com.io7m.aradine.instrument.spi1.ARI1EventBufferType;
import com.io7m.aradine.instrument.spi1.ARI1EventType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentDescription;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentFactoryType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentServicesType;
import com.io7m.aradine.instrument.spi1.ARI1IntMapMutableType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionInteger;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionReal;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionSampleMap;
import com.io7m.aradine.instrument.spi1.ARI1ParameterId;
import com.io7m.aradine.instrument.spi1.ARI1ParameterType;
import com.io7m.aradine.instrument.spi1.ARI1PortDescriptionInputAudio;
import com.io7m.aradine.instrument.spi1.ARI1PortDescriptionInputNote;
import com.io7m.aradine.instrument.spi1.ARI1PortDescriptionOutputAudio;
import com.io7m.aradine.instrument.spi1.ARI1PortId;
import com.io7m.aradine.instrument.spi1.ARI1PortType;
import com.io7m.aradine.instrument.spi1.ARI1RNGDeterministicType;
import com.io7m.aradine.instrument.spi1.ARI1SampleMapType;
import com.io7m.aradine.instrument.spi1.json_data.ARI1InstrumentParsers;
import com.io7m.jattribute.core.AttributeSubscriptionType;
import com.io7m.jattribute.core.AttributeType;
import com.io7m.jattribute.core.Attributes;
import com.io7m.jmulticlose.core.CloseableCollection;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import com.io7m.jmulticlose.core.ClosingResourceFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ARI1MiniInstrumentServices
  implements ARI1InstrumentServicesType, AutoCloseable
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ARI1MiniInstrumentServices.class);

  private final ARI1InstrumentDescription instrumentDescription;
  private final ARInstrumentInstanceID instanceID;
  private final AttributeType<Integer> sampleRate;
  private final AttributeType<Integer> bufferSize;
  private final ARI1SampleMapEmpty emptyMap;
  private final CloseableCollectionType<ClosingResourceFailedException> closeables;
  private final Map<ARI1ParameterId, ARI1ParameterType> parameters;
  private final Map<ARI1PortId, ARI1PortType> ports;
  private final ConcurrentHashMap<URI, ARI1SampleMapType> sampleMaps;
  private final AttributeSubscriptionType sampleRateSubscription;
  private double millisecondsPerFrame;

  private ARI1MiniInstrumentServices(
    final CloseableCollectionType<ClosingResourceFailedException> inCloseables,
    final ARI1InstrumentDescription inInstrumentDescription,
    final ARInstrumentInstanceID inInstance,
    final AttributeType<Integer> inSampleRate,
    final AttributeType<Integer> inBufferSize,
    final Map<ARI1ParameterId, ARI1ParameterType> inParameters,
    final Map<ARI1PortId, ARI1PortType> inPorts)
  {
    this.closeables =
      Objects.requireNonNull(inCloseables, "closeables");
    this.instrumentDescription =
      Objects.requireNonNull(
        inInstrumentDescription,
        "inInstrumentDescription");
    this.instanceID =
      Objects.requireNonNull(inInstance, "instance");
    this.sampleRate =
      Objects.requireNonNull(inSampleRate, "inSampleRate");
    this.bufferSize =
      Objects.requireNonNull(inBufferSize, "inBufferSize");
    this.emptyMap =
      new ARI1SampleMapEmpty();
    this.sampleMaps =
      new ConcurrentHashMap<>();
    this.parameters =
      Map.copyOf(inParameters);
    this.ports =
      Map.copyOf(inPorts);
    this.millisecondsPerFrame =
      1.0 / (inSampleRate.get().doubleValue() * 1000.0);

    this.sampleRateSubscription =
      this.closeables.add(
        this.sampleRate.subscribe((_, newRate) -> {
          this.millisecondsPerFrame =
            1.0 / (newRate.doubleValue() * 1000.0);
        })
      );
  }

  public static ARI1MiniInstrumentServices create(
    final ARI1InstrumentFactoryType instrumentFactory,
    final ARInstrumentInstanceID instance,
    final int sampleRate,
    final int bufferSize)
    throws Exception
  {
    final var attributes =
      Attributes.create(ex -> LOG.error("exception: ", ex));

    final var sampleRateAttribute =
      attributes.withValue(Integer.valueOf(sampleRate));
    final var bufferSizeAttribute =
      attributes.withValue(Integer.valueOf(bufferSize));

    final var closeables =
      CloseableCollection.create();

    final var parsers = new ARI1InstrumentParsers();
    final ARI1InstrumentDescription instrumentDescription;
    try (var stream = instrumentFactory.openInstrumentDescription()) {
      Objects.requireNonNull(stream, "stream");

      instrumentDescription = parsers.parse(
        URI.create("aradine:instrument"),
        stream
      );
    }

    final var parameters =
      instantiateParameters(instrumentDescription);
    final var ports =
      instantiatePorts(bufferSizeAttribute, closeables, instrumentDescription);

    return new ARI1MiniInstrumentServices(
      closeables,
      instrumentDescription,
      instance,
      sampleRateAttribute,
      bufferSizeAttribute,
      parameters,
      ports
    );
  }

  private static HashMap<ARI1PortId, ARI1PortType> instantiatePorts(
    final AttributeType<Integer> bufferSizeAttribute,
    final CloseableCollectionType<ClosingResourceFailedException> closeables,
    final ARI1InstrumentDescription instrumentDescription)
  {
    final var ports = new HashMap<ARI1PortId, ARI1PortType>();
    for (final var entry : instrumentDescription.ports().entrySet()) {
      final var id = entry.getKey();
      final var description = entry.getValue();

      final var currentBufferSize = bufferSizeAttribute.get().intValue();
      switch (description) {
        case final ARI1PortDescriptionOutputAudio _ -> {
          final var port =
            new ARI1PortOutputAudio(id, currentBufferSize);
          ports.put(id, port);
          closeables.add(
            bufferSizeAttribute.subscribe((_, newValue) -> {
              port.setBufferSize(newValue.intValue());
            })
          );
          continue;
        }
        case final ARI1PortDescriptionInputAudio _ -> {
          final var port = new ARI1PortInputAudio(id, currentBufferSize);
          ports.put(id, port);
          closeables.add(
            bufferSizeAttribute.subscribe((_, newValue) -> {
              port.setBufferSize(newValue.intValue());
            })
          );
          continue;
        }
        case final ARI1PortDescriptionInputNote _ -> {
          ports.put(id, new ARI1PortInputNote(id));
          continue;
        }
      }

    }
    return ports;
  }

  private static HashMap<ARI1ParameterId, ARI1ParameterType> instantiateParameters(
    final ARI1InstrumentDescription instrumentDescription)
  {
    final var parameters = new HashMap<ARI1ParameterId, ARI1ParameterType>();
    for (final var entry : instrumentDescription.parameters().entrySet()) {
      final var id = entry.getKey();
      final var description = entry.getValue();
      switch (description) {
        case final ARI1ParameterDescriptionInteger d -> {
          parameters.put(id, new ARI1ParameterInteger(d));
        }
        case final ARI1ParameterDescriptionReal d -> {
          parameters.put(id, new ARI1ParameterReal(d));
        }
        case final ARI1ParameterDescriptionSampleMap d -> {
          parameters.put(
            id,
            new ARI1ParameterSampleMap(
              d,
              URI.create("aradine:unspecified")));
          continue;
        }
      }
    }
    return parameters;
  }

  @Override
  public ARI1EventBufferType createEventBuffer()
  {
    return new ARI1EventBuffer();
  }

  @Override
  public <T> ARI1IntMapMutableType<T> createIntMap(
    final int size)
  {
    return new ARI1IntMapMutable<>(size);
  }

  @Override
  public ARI1RNGDeterministicType createDeterministicRNG(
    final int seed)
  {
    return new ARI1RNGDeterministic(seed);
  }

  @Override
  public int statusCurrentSampleRate()
  {
    return this.sampleRate.get().intValue();
  }

  @Override
  public int statusCurrentBufferSize()
  {
    return this.bufferSize.get().intValue();
  }

  public void sampleMapRegister(
    final URI uri,
    final ARI1SampleMapType sampleMap)
  {
    this.sampleMaps.put(uri, sampleMap);
  }

  @Override
  public ARI1SampleMapType sampleMapGet(
    final URI uri)
  {
    final var map = this.sampleMaps.get(uri);
    if (map == null) {
      LOG.warn(
        "[{} {}] requested nonexistent sample map: {}",
        this.instrumentDescription.identifier(),
        this.instrumentDescription.version(),
        uri
      );
      return this.emptyMap;
    }

    return map;
  }

  @Override
  public ARI1SampleMapType sampleMapEmpty()
  {
    return this.emptyMap;
  }

  public void setBufferSize(
    final int size)
  {
    this.bufferSize.set(Integer.valueOf(size));
  }

  public void setSampleRate(
    final int rate)
  {
    this.sampleRate.set(Integer.valueOf(rate));
  }

  @Override
  public void eventUnhandled(
    final ARI1EventType event)
  {
    LOG.warn(
      "[{} {}] unhandled event: {}",
      this.instrumentDescription.identifier(),
      this.instrumentDescription.version(),
      event
    );
  }

  @Override
  public Map<ARI1ParameterId, ARI1ParameterType> declaredParameters()
  {
    return this.parameters;
  }

  @Override
  public <C extends ARI1ParameterType> C declaredParameter(
    final ARI1ParameterId id,
    final Class<C> clazz)
  {
    return clazz.cast(this.parameters.get(id));
  }

  @Override
  public Map<ARI1PortId, ARI1PortType> declaredPorts()
  {
    return this.ports;
  }

  @Override
  public <C extends ARI1PortType> C declaredPort(
    final ARI1PortId id,
    final Class<C> clazz)
  {
    return clazz.cast(this.ports.get(id));
  }

  @Override
  public void close()
    throws Exception
  {
    this.closeables.close();
  }

  @Override
  public @ARTimeMilliseconds double timeMillisecondsPerFrame()
  {
    return this.millisecondsPerFrame;
  }

  @Override
  public @ARTimeFrames long timeMillisecondsToFrames(
    final @ARTimeMilliseconds double milliseconds)
  {
    final var rate = (double) this.statusCurrentSampleRate();
    return Math.round((rate * (milliseconds / 1000.0)));
  }

  @Override
  public UUID idInstance()
  {
    return this.instanceID.value();
  }
}
