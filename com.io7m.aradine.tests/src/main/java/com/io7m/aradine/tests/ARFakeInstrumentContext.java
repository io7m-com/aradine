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

package com.io7m.aradine.tests;

import com.io7m.aradine.annotations.ARTimeFrames;
import com.io7m.aradine.annotations.ARTimeMilliseconds;
import com.io7m.aradine.ensemble.internal.v1.context.AREnsSPI1EventBuffer;
import com.io7m.aradine.ensemble.internal.v1.context.AREnsSPI1IntMapMutable;
import com.io7m.aradine.instrument.spi1.ARI1DottedName;
import com.io7m.aradine.instrument.spi1.ARI1EventBufferType;
import com.io7m.aradine.instrument.spi1.ARI1EventType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentContextType;
import com.io7m.aradine.instrument.spi1.ARI1IntMapMutableType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionInteger;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionReal;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionSampleMap;
import com.io7m.aradine.instrument.spi1.ARI1ParameterIntegerType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterNumber;
import com.io7m.aradine.instrument.spi1.ARI1ParameterRealType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterSampleMapType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterType;
import com.io7m.aradine.instrument.spi1.ARI1PortNumber;
import com.io7m.aradine.instrument.spi1.ARI1PortSourceAudioType;
import com.io7m.aradine.instrument.spi1.ARI1PortTargetNoteType;
import com.io7m.aradine.instrument.spi1.ARI1PortType;
import com.io7m.aradine.instrument.spi1.ARI1RNGDeterministicType;
import com.io7m.aradine.instrument.spi1.ARI1SampleMapIdentifiers;
import com.io7m.aradine.instrument.spi1.ARI1SampleMapInstanceID;
import com.io7m.aradine.instrument.spi1.ARI1SampleMapType;
import com.io7m.junreachable.UnimplementedCodeException;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ARFakeInstrumentContext
  implements ARI1InstrumentContextType
{
  public ARFakeInstrumentContext()
  {

  }

  @Override
  public int statusCurrentSampleRate()
  {
    throw new UnimplementedCodeException();
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
    if (Objects.equals(clazz, ARI1ParameterIntegerType.class)) {
      return clazz.cast(
        new ARI1ParameterInteger(
          new ARI1ParameterDescriptionInteger(
            id,
            id.toString(),
            Optional.empty(),
            new ARI1DottedName("com.io7m.example"),
            0L,
            100L,
            50L
          )
        )
      );
    }

    if (Objects.equals(clazz, ARI1ParameterRealType.class)) {
      return clazz.cast(
        new ARI1ParameterReal(
          new ARI1ParameterDescriptionReal(
            id,
            id.toString(),
            Optional.empty(),
            new ARI1DottedName("com.io7m.example"),
            0L,
            100L,
            50L
          )
        )
      );
    }

    if (Objects.equals(clazz, ARI1ParameterSampleMapType.class)) {
      return clazz.cast(
        new ARI1ParameterSampleMap(
          new ARI1ParameterDescriptionSampleMap(
            id,
            id.toString(),
            Optional.empty()
          ),
          ARI1SampleMapIdentifiers.emptyInstanceID()
        )
      );
    }

    throw new IllegalStateException(clazz.getCanonicalName().toString());
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
    if (Objects.equals(clazz, ARI1PortSourceAudioType.class)) {
      return clazz.cast(
        new ARI1PortSourceAudio(id, 1024)
      );
    }

    if (Objects.equals(clazz, ARI1PortTargetNoteType.class)) {
      return clazz.cast(
        new ARI1PortTargetNote(id)
      );
    }

    throw new IllegalStateException(clazz.getCanonicalName().toString());
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
    throw new UnimplementedCodeException();
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
}
