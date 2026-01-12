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

package com.io7m.aradine.ensemble.internal.v1.context;

import com.io7m.aradine.annotations.ARTimeFrames;
import com.io7m.aradine.annotations.ARTimeMilliseconds;
import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.audiosystem.ARAudioSystemUsableType;
import com.io7m.aradine.api.parameters.ARParameterID;
import com.io7m.aradine.api.ports.ARPortID;
import com.io7m.aradine.ensemble.internal.model.AREnsInstrumentContext;
import com.io7m.aradine.ensemble.internal.model.AREnsParameterInteger;
import com.io7m.aradine.ensemble.internal.model.AREnsParameterReal;
import com.io7m.aradine.ensemble.internal.model.AREnsParameterSampleMap;
import com.io7m.aradine.ensemble.internal.model.AREnsParameterType;
import com.io7m.aradine.ensemble.internal.model.AREnsPortAudio;
import com.io7m.aradine.ensemble.internal.model.AREnsPortNote;
import com.io7m.aradine.ensemble.internal.model.AREnsPortType;
import com.io7m.aradine.instrument.spi1.ARI1EventBufferType;
import com.io7m.aradine.instrument.spi1.ARI1EventType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentContextType;
import com.io7m.aradine.instrument.spi1.ARI1IntMapMutableType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterNumber;
import com.io7m.aradine.instrument.spi1.ARI1ParameterType;
import com.io7m.aradine.instrument.spi1.ARI1PortNumber;
import com.io7m.aradine.instrument.spi1.ARI1PortType;
import com.io7m.aradine.instrument.spi1.ARI1RNGDeterministicType;
import com.io7m.aradine.instrument.spi1.ARI1SampleMapInstanceID;
import com.io7m.aradine.instrument.spi1.ARI1SampleMapType;
import com.io7m.junreachable.UnimplementedCodeException;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The instrument context adapter for SPI1. This adapter wraps a core API
 * instrument context and exposes the methods required by SPI1 instruments.
 */

public final class AREnsSPI1InstrumentContextAdapter
  implements ARI1InstrumentContextType, AutoCloseable
{
  private final ARAudioSystemUsableType audioSystem;
  private final AREnsInstrumentContext context;
  private Map<ARPortID, ARI1PortType> ports;
  private Map<ARParameterID, ARI1ParameterType> parameters;
  private Map<ARI1PortNumber, ARI1PortType> portsByNumber;
  private Map<ARI1ParameterNumber, ARI1ParameterType> parametersByNumber;

  private AREnsSPI1InstrumentContextAdapter(
    final ARAudioSystemUsableType inAudioSystem,
    final AREnsInstrumentContext inContext)
  {
    this.audioSystem =
      Objects.requireNonNull(inAudioSystem, "AudioSystem");
    this.context =
      Objects.requireNonNull(inContext, "Context");
  }

  /**
   * Create a new context.
   *
   * @param audioSystem The audio system attributes
   * @param context     The core context
   *
   * @return An SPI1 context
   */

  public static AREnsSPI1InstrumentContextAdapter wrap(
    final ARAudioSystemUsableType audioSystem,
    final AREnsInstrumentContext context)
  {
    Objects.requireNonNull(audioSystem, "AudioSystem");
    Objects.requireNonNull(context, "Context");

    final var adapter =
      new AREnsSPI1InstrumentContextAdapter(audioSystem, context);

    adapter.wrapParameters();
    adapter.wrapPorts();
    return adapter;
  }

  private static Map.Entry<ARI1PortNumber, ARI1PortType>
  mapPortNumberEntry(
    final Map.Entry<ARPortID, ARI1PortType> entry)
  {
    return Map.entry(
      new ARI1PortNumber(entry.getValue().id().value()),
      entry.getValue()
    );
  }

  private static Map.Entry<ARI1ParameterNumber, ARI1ParameterType>
  mapParameterNumberEntry(
    final Map.Entry<ARParameterID, ARI1ParameterType> entry)
  {
    return Map.entry(
      new ARI1ParameterNumber(entry.getValue().id().value()),
      entry.getValue()
    );
  }

  /**
   * @return The core API context
   */

  public AREnsInstrumentContext baseContext()
  {
    return this.context;
  }

  private void wrapPorts()
  {
    this.ports =
      this.context.ports()
        .entrySet()
        .stream()
        .map(this::wrapPortEntry)
        .collect(
          Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue)
        );

    this.portsByNumber =
      this.ports.entrySet()
        .stream()
        .map(AREnsSPI1InstrumentContextAdapter::mapPortNumberEntry)
        .collect(
          Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue)
        );
  }

  private Map.Entry<ARPortID, ARI1PortType>
  wrapPortEntry(
    final Map.Entry<ARPortID, AREnsPortType> entry)
  {
    return Map.entry(entry.getKey(), this.wrapPort(entry.getValue()));
  }

  private ARI1PortType wrapPort(
    final AREnsPortType value)
  {
    return switch (value) {
      case final AREnsPortAudio portAudio -> {
        yield new AREnsSPI1PortAudioAdapter(this, portAudio);
      }
      case final AREnsPortNote portNote -> {
        yield new AREnsSPI1PortNoteAdapter(this, portNote);
      }
    };
  }

  private void wrapParameters()
  {
    this.parameters =
      this.context.parameters()
        .entrySet()
        .stream()
        .map(this::wrapParameterEntry)
        .collect(
          Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue)
        );

    this.parametersByNumber =
      this.parameters.entrySet()
        .stream()
        .map(AREnsSPI1InstrumentContextAdapter::mapParameterNumberEntry)
        .collect(
          Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue)
        );
  }

  private Map.Entry<ARParameterID, ARI1ParameterType>
  wrapParameterEntry(
    final Map.Entry<ARParameterID, AREnsParameterType> entry)
  {
    return Map.entry(entry.getKey(), this.wrapParameter(entry.getValue()));
  }

  private ARI1ParameterType wrapParameter(
    final AREnsParameterType value)
  {
    return switch (value) {
      case final AREnsParameterInteger i -> {
        yield new AREnsSPI1ParameterIntegerAdapter(this, i);
      }
      case final AREnsParameterReal r -> {
        yield new AREnsSPI1ParameterRealAdapter(this, r);
      }
      case final AREnsParameterSampleMap sm -> {
        yield new AREnsSPI1ParameterSampleMapAdapter(this, sm);
      }
    };
  }

  @Override
  public int statusCurrentSampleRate()
  {
    return this.audioSystem.attributes().sampleRate().get().intValue();
  }

  @Override
  public int statusCurrentBufferSize()
  {
    return this.audioSystem.attributes().bufferSize().get().intValue();
  }

  @Override
  public Map<ARI1ParameterNumber, ARI1ParameterType> declaredParameters()
  {
    return this.parametersByNumber;
  }

  @Override
  public <C extends ARI1ParameterType> C declaredParameter(
    final ARI1ParameterNumber id,
    final Class<C> clazz)
  {
    final var r = this.parametersByNumber.get(id);
    if (r == null) {
      throw new IllegalArgumentException(
        "No such parameter: %s".formatted(id)
      );
    }
    return clazz.cast(r);
  }

  @Override
  public Map<ARI1PortNumber, ARI1PortType> declaredPorts()
  {
    return this.portsByNumber;
  }

  @Override
  public <C extends ARI1PortType> C declaredPort(
    final ARI1PortNumber id,
    final Class<C> clazz)
  {
    final var r = this.portsByNumber.get(id);
    if (r == null) {
      throw new IllegalArgumentException(
        "No such port: %s".formatted(id)
      );
    }
    return clazz.cast(r);
  }

  @Override
  public void eventUnhandled(
    final ARI1EventType event)
  {
    throw new UnimplementedCodeException();
  }

  @Override
  public <T extends ARI1EventType> ARI1EventBufferType<T> createEventBuffer()
  {
    return new AREnsSPI1EventBuffer<>();
  }

  @Override
  public <T> ARI1IntMapMutableType<T> createIntMap(
    final int size)
  {
    return new AREnsSPI1IntMapMutable<>(size);
  }

  @Override
  public ARI1RNGDeterministicType createDeterministicRNG(
    final int seed)
  {
    return new AREnsSPI1RNGDeterministic(seed);
  }

  @Override
  public ARI1SampleMapType sampleMapGet(
    final ARI1SampleMapInstanceID id)
  {
    throw new UnimplementedCodeException();
  }

  @Override
  public ARI1SampleMapType sampleMapEmpty()
  {
    throw new UnimplementedCodeException();
  }

  @Override
  public @ARTimeMilliseconds double timeMillisecondsPerFrame()
  {
    throw new UnimplementedCodeException();
  }

  @Override
  public @ARTimeFrames long timeMillisecondsToFrames(
    @ARTimeMilliseconds final double milliseconds)
  {
    throw new UnimplementedCodeException();
  }

  @Override
  public void close()
    throws ARException
  {
    this.context.close();
  }
}
