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

package com.io7m.aradine.ensemble.internal.model;

import com.io7m.aradine.api.ARCloseables;
import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.instrument.ARInstrumentDescription;
import com.io7m.aradine.api.parameters.ARParameterDescriptionInteger;
import com.io7m.aradine.api.parameters.ARParameterDescriptionReal;
import com.io7m.aradine.api.parameters.ARParameterDescriptionSampleMap;
import com.io7m.aradine.api.parameters.ARParameterID;
import com.io7m.aradine.api.ports.ARPortID;
import com.io7m.aradine.api.sample_map.ARSampleMapIdentifiers;
import com.io7m.aradine.api.system.ARAudioSystemAttributesType;
import com.io7m.jmulticlose.core.CloseableCollectionType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The core instrument context.
 */

public final class AREnsInstrumentContext
  implements AutoCloseable
{
  private final ARAudioSystemAttributesType audioSystemAttributes;
  private final ARInstrumentDescription description;
  private final CloseableCollectionType<ARException> closeables;
  private final HashMap<ARPortID, AREnsPortInstanceType> ports;
  private final HashMap<ARParameterID, AREnsParameterType> parameters;
  private Map<ARPortID, AREnsPortInstanceType> portsRead;
  private Map<ARParameterID, AREnsParameterType> parametersRead;

  private AREnsInstrumentContext(
    final ARAudioSystemAttributesType inAudioSystemAttributes,
    final ARInstrumentDescription inDescription,
    final CloseableCollectionType<ARException> inCloseables)
  {
    this.audioSystemAttributes =
      Objects.requireNonNull(inAudioSystemAttributes, "AudioSystemAttributes");
    this.description =
      Objects.requireNonNull(inDescription, "Description");
    this.closeables =
      Objects.requireNonNull(inCloseables, "Closeables");
    this.ports =
      new HashMap<>(this.description.ports().size());
    this.parameters =
      new HashMap<>(this.description.parameters().size());
  }

  /**
   * Create instrument services.
   *
   * @param audioSystemAttributes The audio system attributes
   * @param description           The instrument description
   *
   * @return The services
   */

  public static AREnsInstrumentContext create(
    final ARAudioSystemAttributesType audioSystemAttributes,
    final ARInstrumentDescription description)
  {
    Objects.requireNonNull(audioSystemAttributes, "AudioSystemAttributes");
    Objects.requireNonNull(description, "Description");

    final var closeables =
      ARCloseables.create();
    final var services =
      new AREnsInstrumentContext(
        audioSystemAttributes,
        description,
        closeables
      );

    services.instantiateParameters();
    services.instantiatePorts();
    return services;
  }

  /**
   * @return The ports
   */

  public Map<ARPortID, AREnsPortInstanceType> ports()
  {
    return this.portsRead;
  }

  /**
   * @return The parameters
   */

  public Map<ARParameterID, AREnsParameterType> parameters()
  {
    return this.parametersRead;
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
          final var port =
            new AREnsPortAudio(this, portDescription, currentBufferSize);

          this.ports.put(portID, port);
          this.closeables.add(
            bufferSizeAttribute.subscribe((_, newValue) -> {
              port.setBufferSize(newValue.intValue());
            })
          );
        }
        case AR_NOTE -> {
          this.ports.put(portID, new AREnsPortNote(this, portDescription));
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
        case final ARParameterDescriptionInteger d -> {
          this.parameters.put(id, new AREnsParameterInteger(this, d));
        }
        case final ARParameterDescriptionReal d -> {
          this.parameters.put(id, new AREnsParameterReal(this, d));
        }
        case final ARParameterDescriptionSampleMap d -> {
          this.parameters.put(
            id,
            new AREnsParameterSampleMap(
              this, d, ARSampleMapIdentifiers.emptyInstanceID()
            )
          );
        }
      }
    }

    this.parametersRead = Map.copyOf(this.parameters);
  }

  @Override
  public void close()
    throws ARException
  {
    this.closeables.close();
  }
}
