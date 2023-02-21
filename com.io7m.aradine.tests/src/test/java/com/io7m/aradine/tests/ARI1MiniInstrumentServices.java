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

import com.io7m.aradine.instrument.spi1.ARI1EventBufferType;
import com.io7m.aradine.instrument.spi1.ARI1EventConfigurationType;
import com.io7m.aradine.instrument.spi1.ARI1EventType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentDescriptionType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentFactoryType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentServicesType;
import com.io7m.aradine.instrument.spi1.ARI1IntMapMutableType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionIntegerType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionRealType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionSampleMapType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterId;
import com.io7m.aradine.instrument.spi1.ARI1ParameterType;
import com.io7m.aradine.instrument.spi1.ARI1PortDescriptionInputAudioType;
import com.io7m.aradine.instrument.spi1.ARI1PortDescriptionInputNoteType;
import com.io7m.aradine.instrument.spi1.ARI1PortDescriptionOutputAudioType;
import com.io7m.aradine.instrument.spi1.ARI1PortId;
import com.io7m.aradine.instrument.spi1.ARI1PortType;
import com.io7m.aradine.instrument.spi1.ARI1PropertyIntType;
import com.io7m.aradine.instrument.spi1.ARI1SampleMapType;
import com.io7m.aradine.instrument.spi1.xml.ARI1InstrumentParsers;
import com.io7m.jattribute.core.Attributes;
import com.io7m.jmulticlose.core.CloseableCollection;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import com.io7m.jmulticlose.core.ClosingResourceFailedException;
import com.io7m.jsamplebuffer.api.SampleBufferRateConverterType;
import com.io7m.jsamplebuffer.xmedia.SXMSampleBufferRateConverters;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ARI1MiniInstrumentServices
  implements ARI1InstrumentServicesType, AutoCloseable
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ARI1MiniInstrumentServices.class);

  private final ARI1InstrumentDescriptionType instrumentDescription;
  private final ARI1PropertyInt sampleRate;
  private final ARI1PropertyInt bufferSize;
  private final ARI1SampleMapEmpty emptyMap;
  private final CloseableCollectionType<ClosingResourceFailedException> closeables;
  private final ExecutorService ioExecutor;
  private final SampleBufferRateConverterType converter;
  private final Map<ARI1ParameterId, ARI1ParameterType> parameters;
  private final Map<ARI1PortId, ARI1PortType> ports;

  private ARI1MiniInstrumentServices(
    final CloseableCollectionType<ClosingResourceFailedException> inCloseables,
    final ExecutorService inIOExecutor,
    final SampleBufferRateConverterType inConverter,
    final ARI1InstrumentDescriptionType inInstrumentDescription,
    final ARI1PropertyInt inSampleRate,
    final ARI1PropertyInt inBufferSize,
    final Map<ARI1ParameterId, ARI1ParameterType> inParameters,
    final Map<ARI1PortId, ARI1PortType> inPorts)
  {
    this.closeables =
      Objects.requireNonNull(inCloseables, "closeables");
    this.ioExecutor =
      Objects.requireNonNull(inIOExecutor, "inIOExecutor");
    this.converter =
      Objects.requireNonNull(inConverter, "inConverter");
    this.instrumentDescription =
      Objects.requireNonNull(inInstrumentDescription, "inInstrumentDescription");
    this.sampleRate =
      Objects.requireNonNull(inSampleRate, "inSampleRate");
    this.bufferSize =
      Objects.requireNonNull(inBufferSize, "inBufferSize");
    this.emptyMap =
      new ARI1SampleMapEmpty();
    this.parameters =
      Map.copyOf(inParameters);
    this.ports =
      Map.copyOf(inPorts);
  }

  public static ARI1MiniInstrumentServices create(
    final ARI1InstrumentFactoryType instrumentFactory,
    final int sampleRate,
    final int bufferSize)
    throws Exception
  {
    final var attributes =
      Attributes.create(ex -> LOG.error("exception: ", ex));

    final var sampleRateAttribute =
      new ARI1PropertyInt(attributes, sampleRate);
    final var bufferSizeAttribute =
      new ARI1PropertyInt(attributes, bufferSize);

    final var closeables =
      CloseableCollection.create();

    final var parsers = new ARI1InstrumentParsers();
    final ARI1InstrumentDescriptionType instrumentDescription;
    try (var stream = instrumentFactory.openInstrumentDescription()) {
      instrumentDescription = parsers.parse(
        URI.create("aradine:instrument"),
        stream
      );
    }

    final var parameters =
      instantiateParameters(instrumentDescription);
    final var ports =
      instantiatePorts(bufferSizeAttribute, closeables, instrumentDescription);

    final var executor =
      Executors.newFixedThreadPool(4, r -> {
        final var thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName("com.io7m.aradine.tests.io." + thread.getId());
        thread.setPriority(Thread.MIN_PRIORITY);
        return thread;
      });

    final var converters =
      new SXMSampleBufferRateConverters();

    return new ARI1MiniInstrumentServices(
      closeables,
      executor,
      converters.createConverter(),
      instrumentDescription,
      sampleRateAttribute,
      bufferSizeAttribute,
      parameters,
      ports
    );
  }

  private static HashMap<ARI1PortId, ARI1PortType> instantiatePorts(
    final ARI1PropertyInt bufferSizeAttribute,
    final CloseableCollectionType<ClosingResourceFailedException> closeables,
    final ARI1InstrumentDescriptionType instrumentDescription)
  {
    final var ports = new HashMap<ARI1PortId, ARI1PortType>();
    for (final var entry : instrumentDescription.ports().entrySet()) {
      final var id = entry.getKey();
      final var description = entry.getValue();

      if (description instanceof ARI1PortDescriptionOutputAudioType) {
        final var port = new ARI1PortOutputAudio(id, bufferSizeAttribute.get());
        ports.put(id, port);
        closeables.add(
          bufferSizeAttribute.subscribe((oldValue, newValue) -> {
            port.setBufferSize(newValue);
          })
        );
        continue;
      }

      if (description instanceof ARI1PortDescriptionInputAudioType) {
        final var port = new ARI1PortInputAudio(id, bufferSizeAttribute.get());
        ports.put(id, port);
        closeables.add(
          bufferSizeAttribute.subscribe((oldValue, newValue) -> {
            port.setBufferSize(newValue);
          })
        );
        continue;
      }

      if (description instanceof ARI1PortDescriptionInputNoteType) {
        ports.put(id, new ARI1PortInputNote(id));
        continue;
      }
    }
    return ports;
  }

  private static HashMap<ARI1ParameterId, ARI1ParameterType> instantiateParameters(
    final ARI1InstrumentDescriptionType instrumentDescription)
  {
    final var parameters = new HashMap<ARI1ParameterId, ARI1ParameterType>();
    for (final var entry : instrumentDescription.parameters().entrySet()) {
      final var id = entry.getKey();
      final var description = entry.getValue();
      if (description instanceof ARI1ParameterDescriptionIntegerType d) {
        parameters.put(id, new ARI1ParameterInteger(d));
        continue;
      }
      if (description instanceof ARI1ParameterDescriptionRealType d) {
        parameters.put(id, new ARI1ParameterReal(d));
        continue;
      }
      if (description instanceof ARI1ParameterDescriptionSampleMapType d) {
        parameters.put(id, new ARI1ParameterSampleMap(d, URI.create("aradine:unspecified")));
        continue;
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
  public ARI1PropertyIntType statusCurrentSampleRate()
  {
    return this.sampleRate;
  }

  @Override
  public ARI1PropertyIntType statusCurrentBufferSize()
  {
    return this.bufferSize;
  }

  @Override
  public CompletableFuture<ARI1SampleMapType> sampleMapOpen(
    final URI uri)
  {
    return switch (uri.getScheme()) {
      case "file" -> {
        final var future = new CompletableFuture<ARI1SampleMapType>();
        this.ioExecutor.execute(() -> {
          final var timeThen = Instant.now();

          try {
            final var sampleDescriptions = new Int2ObjectRBTreeMap<Path>();
            sampleDescriptions.put(62, Paths.get("60.wav"));
            sampleDescriptions.put(64, Paths.get("62.wav"));
            sampleDescriptions.put(65, Paths.get("61.wav"));
            sampleDescriptions.put(66, Paths.get("63.wav"));
            future.complete(
              new ARI1SampleMapDescription(sampleDescriptions)
                .load(this.converter, this.sampleRate.get())
            );
          } catch (final Throwable e) {
            future.completeExceptionally(e);
          } finally {
            final var timeNow = Instant.now();
            LOG.debug("sampleMapOpen: {}", Duration.between(timeThen, timeNow));
          }
        });
        yield future;
      }
      default -> CompletableFuture.completedFuture(this.emptyMap);
    };
  }

  @Override
  public ARI1SampleMapType sampleMapOpenEmpty()
  {
    return this.emptyMap;
  }

  public void setBufferSize(
    final int size)
  {
    this.bufferSize.set(size);
  }

  public void setSampleRate(
    final int rate)
  {
    this.sampleRate.set(rate);
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
}
