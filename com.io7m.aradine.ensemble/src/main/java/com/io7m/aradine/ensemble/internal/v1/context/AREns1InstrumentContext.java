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
import com.io7m.aradine.api.ARCloseables;
import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.system.ARAudioSystemAttributesType;
import com.io7m.aradine.instrument.spi1.ARI1EventBufferType;
import com.io7m.aradine.instrument.spi1.ARI1EventType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentContextType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentDescription;
import com.io7m.aradine.instrument.spi1.ARI1IntMapMutableType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionInteger;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionReal;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionSampleMap;
import com.io7m.aradine.instrument.spi1.ARI1ParameterNumber;
import com.io7m.aradine.instrument.spi1.ARI1ParameterType;
import com.io7m.aradine.instrument.spi1.ARI1PortNumber;
import com.io7m.aradine.instrument.spi1.ARI1PortType;
import com.io7m.aradine.instrument.spi1.ARI1RNGDeterministicType;
import com.io7m.aradine.instrument.spi1.ARI1SampleMapID;
import com.io7m.aradine.instrument.spi1.ARI1SampleMapIdentifiers;
import com.io7m.aradine.instrument.spi1.ARI1SampleMapType;
import com.io7m.jmulticlose.core.CloseableCollectionType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The V1 instrument services. This is a facade presented to version 1
 * instruments.
 */

public final class AREns1InstrumentContext
  implements AutoCloseable, ARI1InstrumentContextType
{
  private final ARAudioSystemAttributesType audioSystemAttributes;
  private final CloseableCollectionType<ARException> closeables;
  private final ARI1InstrumentDescription description;
  private final HashMap<ARI1ParameterNumber, ARI1ParameterType> parameters;
  private final HashMap<ARI1PortNumber, ARI1PortType> ports;
  private Map<ARI1PortNumber, ARI1PortType> portsRead;
  private Map<ARI1ParameterNumber, ARI1ParameterType> parametersRead;

  private AREns1InstrumentContext(
    final ARAudioSystemAttributesType inAudioSystemAttributes,
    final CloseableCollectionType<ARException> inCloseables,
    final ARI1InstrumentDescription inDescription)
  {
    this.audioSystemAttributes =
      Objects.requireNonNull(inAudioSystemAttributes, "AudioSystemAttributes");
    this.closeables =
      Objects.requireNonNull(inCloseables, "Closeables");
    this.description =
      Objects.requireNonNull(inDescription, "Description");
    this.parameters =
      new HashMap<>();
    this.ports =
      new HashMap<>();
  }

  /**
   * Create instrument services.
   *
   * @param inDescription         The instrument description
   * @param audioSystemAttributes The audio system attributes
   *
   * @return The services
   */

  public static AREns1InstrumentContext create(
    final ARI1InstrumentDescription inDescription,
    final ARAudioSystemAttributesType audioSystemAttributes)
  {
    Objects.requireNonNull(inDescription, "Description");

    final var closeables =
      ARCloseables.create();
    final var services =
      new AREns1InstrumentContext(
        audioSystemAttributes,
        closeables,
        inDescription
      );

    services.instantiateParameters();
    services.instantiatePorts();
    return services;
  }

  private void instantiatePorts()
  {
    final var bufferSizeAttribute = this.audioSystemAttributes.bufferSize();
    for (final var entry : this.description.ports().entrySet()) {
      final var portID =
        entry.getKey();
      final var portDescription =
        entry.getValue();
      final var currentBufferSize =
        bufferSizeAttribute.get().intValue();

      switch (portDescription.kind()) {
        case AR_AUDIO -> {
          switch (portDescription.direction()) {
            case AR_SOURCE -> {
              final var port =
                new AREns1PortSourceAudio(this, portID, currentBufferSize);

              this.ports.put(portID, port);
              this.closeables.add(
                bufferSizeAttribute.subscribe((_, newValue) -> {
                  port.setBufferSize(newValue.intValue());
                })
              );
            }
            case AR_TARGET -> {
              final var port =
                new AREns1PortTargetAudio(this, portID, currentBufferSize);

              this.ports.put(portID, port);
              this.closeables.add(
                bufferSizeAttribute.subscribe((_, newValue) -> {
                  port.setBufferSize(newValue.intValue());
                })
              );
            }
          }
        }
        case AR_NOTE -> {
          switch (portDescription.direction()) {
            case AR_SOURCE -> {
              this.ports.put(portID, new AREns1PortSourceNote(this, portID));
            }
            case AR_TARGET -> {
              this.ports.put(portID, new AREns1PortTargetNote(this, portID));
            }
          }
        }
      }
    }

    this.portsRead = Map.copyOf(this.ports);
  }

  private void instantiateParameters()
  {
    for (final var entry : this.description.parameters().entrySet()) {
      final var id = entry.getKey();
      switch (entry.getValue()) {
        case final ARI1ParameterDescriptionInteger d -> {
          this.parameters.put(id, new AREns1ParameterInteger(this, d));
        }
        case final ARI1ParameterDescriptionReal d -> {
          this.parameters.put(id, new AREns1ParameterReal(this, d));
        }
        case final ARI1ParameterDescriptionSampleMap d -> {
          this.parameters.put(
            id,
            new AREns1ParameterSampleMap(
              this, d, ARI1SampleMapIdentifiers.empty()
            )
          );
        }
      }
    }

    this.parametersRead = Map.copyOf(this.parameters);
  }

  @Override
  public int statusCurrentSampleRate()
  {
    return this.audioSystemAttributes.sampleRate().get().intValue();
  }

  @Override
  public int statusCurrentBufferSize()
  {
    return this.audioSystemAttributes.bufferSize().get().intValue();
  }

  @Override
  public Map<ARI1ParameterNumber, ARI1ParameterType> declaredParameters()
  {
    return this.parametersRead;
  }

  @Override
  public <C extends ARI1ParameterType> C declaredParameter(
    final ARI1ParameterNumber id,
    final Class<C> clazz)
  {
    return clazz.cast(this.parametersRead.get(id));
  }

  @Override
  public Map<ARI1PortNumber, ARI1PortType> declaredPorts()
  {
    return this.portsRead;
  }

  @Override
  public <C extends ARI1PortType> C declaredPort(
    final ARI1PortNumber id,
    final Class<C> clazz)
  {
    return clazz.cast(this.portsRead.get(id));
  }

  @Override
  public void eventUnhandled(
    final ARI1EventType event)
  {

  }

  @Override
  public <T extends ARI1EventType> ARI1EventBufferType<T> createEventBuffer()
  {
    return new AREns1EventBuffer<>();
  }

  @Override
  public <T> ARI1IntMapMutableType<T> createIntMap(
    final int size)
  {
    return new AREns1IntMapMutable<>(size);
  }

  @Override
  public ARI1RNGDeterministicType createDeterministicRNG(
    final int seed)
  {
    return new AREns1RNGDeterministic(0x41524144);
  }

  @Override
  public ARI1SampleMapType sampleMapGet(
    final ARI1SampleMapID id)
  {
    throw new IllegalStateException();
  }

  @Override
  public ARI1SampleMapType sampleMapEmpty()
  {
    throw new IllegalStateException();
  }

  @Override
  public @ARTimeMilliseconds double timeMillisecondsPerFrame()
  {
    return this.audioSystemAttributes.timeMillisecondsPerFrame();
  }

  @Override
  public @ARTimeFrames long timeMillisecondsToFrames(
    @ARTimeMilliseconds final double milliseconds)
  {
    return this.audioSystemAttributes.timeMillisecondsToFrames(milliseconds);
  }

  @Override
  public void close()
    throws ARException
  {
    this.closeables.close();
  }
}
