/*
 * Copyright © 2025 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

package com.io7m.aradine.tests.instrument.loader;

import com.io7m.aradine.annotations.ARTimeFrames;
import com.io7m.aradine.annotations.ARTimeMilliseconds;
import com.io7m.aradine.api.instrument.ARInstrumentException;
import com.io7m.aradine.instrument.loader.api.ARInstrumentLoaderServicesConstructorType;
import com.io7m.aradine.instrument.spi1.ARI1EventBufferType;
import com.io7m.aradine.instrument.spi1.ARI1EventType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentDescription;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentServicesType;
import com.io7m.aradine.instrument.spi1.ARI1IntMapMutableType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionInteger;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionReal;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionSampleMap;
import com.io7m.aradine.instrument.spi1.ARI1ParameterNumber;
import com.io7m.aradine.instrument.spi1.ARI1ParameterType;
import com.io7m.aradine.instrument.spi1.ARI1PortNumber;
import com.io7m.aradine.instrument.spi1.ARI1PortType;
import com.io7m.aradine.instrument.spi1.ARI1RNGDeterministicType;
import com.io7m.aradine.instrument.spi1.ARI1SampleMapType;
import com.io7m.aradine.tests.ARI1EventBuffer;
import com.io7m.aradine.tests.ARI1IntMapMutable;
import com.io7m.aradine.tests.ARI1ParameterInteger;
import com.io7m.aradine.tests.ARI1ParameterReal;
import com.io7m.aradine.tests.ARI1ParameterSampleMap;
import com.io7m.aradine.tests.ARI1PortSourceAudio;
import com.io7m.aradine.tests.ARI1PortSourceNote;
import com.io7m.aradine.tests.ARI1PortTargetAudio;
import com.io7m.aradine.tests.ARI1PortTargetNote;
import com.io7m.junreachable.UnimplementedCodeException;

import java.net.URI;
import java.util.Map;

public final class ARInstrumentLoaderServicesConstructor
  implements ARInstrumentLoaderServicesConstructorType
{
  public ARInstrumentLoaderServicesConstructor()
  {

  }

  @Override
  public ARI1InstrumentServicesType createServicesV1(
    final ARI1InstrumentDescription description)
    throws ARInstrumentException
  {
    return new Services1(description);
  }

  public static final class Services1
    implements ARI1InstrumentServicesType
  {
    private final ARI1InstrumentDescription description;

    Services1(
      final ARI1InstrumentDescription description)
    {
      this.description = description;
    }

    @Override
    public int statusCurrentSampleRate()
    {
      return 44100;
    }

    @Override
    public int statusCurrentBufferSize()
    {
      return 1024;
    }

    @Override
    public Map<ARI1ParameterNumber, ARI1ParameterType> declaredParameters()
    {
      throw new UnimplementedCodeException();
    }

    @Override
    public <C extends ARI1ParameterType> C declaredParameter(
      final ARI1ParameterNumber id,
      final Class<C> clazz)
    {
      return clazz.cast(
        switch (this.description.parameters().get(id)) {
          case final ARI1ParameterDescriptionInteger pi -> {
            yield new ARI1ParameterInteger(pi);
          }
          case final ARI1ParameterDescriptionReal pr -> {
            yield new ARI1ParameterReal(pr);
          }
          case final ARI1ParameterDescriptionSampleMap psm -> {
            yield new ARI1ParameterSampleMap(psm, URI.create("urn:unused"));
          }
          case null -> {
            throw new IllegalArgumentException(
              "No such parameter: %s".formatted(id)
            );
          }
        }
      );
    }

    @Override
    public Map<ARI1PortNumber, ARI1PortType> declaredPorts()
    {
      throw new UnimplementedCodeException();
    }

    @Override
    public <C extends ARI1PortType> C declaredPort(
      final ARI1PortNumber id,
      final Class<C> clazz)
    {
      final var port = this.description.ports().get(id);
      if (port == null) {
        throw new IllegalArgumentException(
          "No such parameter: %s".formatted(id)
        );
      }

      return clazz.cast(
        switch (port.kind()) {
          case AR_AUDIO -> {
            yield switch (port.direction()) {
              case AR_SOURCE -> new ARI1PortSourceAudio(id, 1024);
              case AR_TARGET -> new ARI1PortTargetAudio(id, 1024);
            };
          }
          case AR_NOTE -> {
            yield switch (port.direction()) {
              case AR_SOURCE -> new ARI1PortSourceNote(id);
              case AR_TARGET -> new ARI1PortTargetNote(id);
            };
          }
        }
      );
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
      return new ARI1EventBuffer<>();
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
      throw new UnimplementedCodeException();
    }

    @Override
    public ARI1SampleMapType sampleMapGet(
      final URI uri)
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
  }
}
