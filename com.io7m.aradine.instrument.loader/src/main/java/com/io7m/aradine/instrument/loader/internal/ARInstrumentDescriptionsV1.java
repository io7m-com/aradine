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

package com.io7m.aradine.instrument.loader.internal;

import com.io7m.aradine.api.instrument.ARInstrumentDescription;
import com.io7m.aradine.api.instrument.ARInstrumentID;
import com.io7m.aradine.api.instrument.ARInstrumentInstanceID;
import com.io7m.aradine.api.instrument.ARInstrumentRole;
import com.io7m.aradine.api.parameters.ARParameterDescriptionInteger;
import com.io7m.aradine.api.parameters.ARParameterDescriptionReal;
import com.io7m.aradine.api.parameters.ARParameterDescriptionSampleMap;
import com.io7m.aradine.api.parameters.ARParameterDescriptionType;
import com.io7m.aradine.api.parameters.ARParameterID;
import com.io7m.aradine.api.parameters.ARParameterNumber;
import com.io7m.aradine.api.ports.ARPortDescription;
import com.io7m.aradine.api.ports.ARPortDirection;
import com.io7m.aradine.api.ports.ARPortID;
import com.io7m.aradine.api.ports.ARPortKind;
import com.io7m.aradine.api.ports.ARPortNumber;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentDescription;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionInteger;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionReal;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionSampleMap;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterNumber;
import com.io7m.aradine.instrument.spi1.ARI1PortDescription;
import com.io7m.aradine.instrument.spi1.ARI1PortDirection;
import com.io7m.aradine.instrument.spi1.ARI1PortKind;
import com.io7m.aradine.instrument.spi1.ARI1PortNumber;
import com.io7m.lanark.core.RDottedName;
import com.io7m.verona.core.Version;
import com.io7m.verona.core.VersionQualifier;

import java.util.Map;
import java.util.stream.Collectors;

final class ARInstrumentDescriptionsV1
{
  private ARInstrumentDescriptionsV1()
  {

  }

  static ARInstrumentDescription fromV1(
    final ARInstrumentInstanceID instanceID,
    final ARI1InstrumentDescription description)
  {
    final var rawVersion =
      description.version();

    final var instrumentID =
      new ARInstrumentID(
        new RDottedName(description.group().value()),
        new RDottedName(description.identifier().value()),
        new Version(
          rawVersion.major(),
          rawVersion.minor(),
          rawVersion.patch(),
          rawVersion.qualifier()
            .map(x -> new VersionQualifier(x.text()))
        )
      );

    return new ARInstrumentDescription(
      instanceID,
      instrumentID,
      fromV1Ports(instanceID, description.ports()),
      fromV1Parameters(instanceID, description.parameters()),
      ARInstrumentRole.AR_INSTRUMENT
    );
  }

  private static Map<ARParameterID, ARParameterDescriptionType> fromV1Parameters(
    final ARInstrumentInstanceID instanceID,
    final Map<ARI1ParameterNumber, ARI1ParameterDescriptionType> parameters)
  {
    return parameters.entrySet()
      .stream()
      .map(e -> fromV1ParameterEntry(instanceID, e))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static Map<ARPortID, ARPortDescription>
  fromV1Ports(
    final ARInstrumentInstanceID instanceID,
    final Map<ARI1PortNumber, ARI1PortDescription> ports)
  {
    return ports.entrySet()
      .stream()
      .map(e -> fromV1PortEntry(instanceID, e))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static Map.Entry<ARPortID, ARPortDescription>
  fromV1PortEntry(
    final ARInstrumentInstanceID instanceID,
    final Map.Entry<ARI1PortNumber, ARI1PortDescription> entry)
  {
    return Map.entry(
      fromV1PortID(instanceID, entry.getKey()),
      fromV1Port(instanceID, entry.getValue())
    );
  }

  private static Map.Entry<ARParameterID, ARParameterDescriptionType>
  fromV1ParameterEntry(
    final ARInstrumentInstanceID instanceID,
    final Map.Entry<ARI1ParameterNumber, ARI1ParameterDescriptionType> entry)
  {
    return Map.entry(
      fromV1ParameterID(instanceID, entry.getKey()),
      fromV1Parameter(instanceID, entry.getValue())
    );
  }

  private static ARPortDescription fromV1Port(
    final ARInstrumentInstanceID instanceID,
    final ARI1PortDescription port)
  {
    final var number =
      fromV1PortNumber(port.number());

    return new ARPortDescription(
      instanceID,
      ARPortID.ofInstancePort(instanceID, number),
      fromV1PortKind(port.kind()),
      fromV1PortDirection(port.direction()),
      fromV1PortNumber(port.number()),
      port.label(),
      port.semantics()
    );
  }

  private static ARPortNumber fromV1PortNumber(
    final ARI1PortNumber number)
  {
    return new ARPortNumber(number.value());
  }

  private static ARPortDirection fromV1PortDirection(
    final ARI1PortDirection direction)
  {
    return switch (direction) {
      case AR_SOURCE -> {
        yield ARPortDirection.AR_SOURCE;
      }
      case AR_TARGET -> {
        yield ARPortDirection.AR_TARGET;
      }
    };
  }

  private static ARPortKind fromV1PortKind(
    final ARI1PortKind kind)
  {
    return switch (kind) {
      case AR_AUDIO -> {
        yield ARPortKind.AR_AUDIO;
      }
      case AR_NOTE -> {
        yield ARPortKind.AR_NOTE;
      }
    };
  }

  private static ARPortID fromV1PortID(
    final ARInstrumentInstanceID instanceID,
    final ARI1PortNumber id)
  {
    final var number = fromV1PortNumber(id);
    return ARPortID.ofInstancePort(instanceID, number);
  }

  private static ARParameterDescriptionType fromV1Parameter(
    final ARInstrumentInstanceID instanceID,
    final ARI1ParameterDescriptionType parameter)
  {
    final var number =
      fromV1ParameterNumber(parameter.id());

    return switch (parameter) {
      case final ARI1ParameterDescriptionInteger i -> {
        yield new ARParameterDescriptionInteger(
          instanceID,
          number,
          fromV1ParameterID(instanceID, parameter.id()),
          i.label(),
          new RDottedName(i.unitOfMeasurement().value()),
          i.valueMinimum(),
          i.valueMaximum(),
          i.valueDefault()
        );
      }
      case final ARI1ParameterDescriptionReal r -> {
        yield new ARParameterDescriptionReal(
          instanceID,
          number,
          fromV1ParameterID(instanceID, parameter.id()),
          r.label(),
          new RDottedName(r.unitOfMeasurement().value()),
          r.valueMinimum(),
          r.valueMaximum(),
          r.valueDefault()
        );
      }
      case final ARI1ParameterDescriptionSampleMap sm -> {
        yield new ARParameterDescriptionSampleMap(
          instanceID,
          number,
          fromV1ParameterID(instanceID, parameter.id()),
          sm.label()
        );
      }
    };
  }

  private static ARParameterNumber fromV1ParameterNumber(
    final ARI1ParameterNumber number)
  {
    return new ARParameterNumber(number.value());
  }

  private static ARParameterID fromV1ParameterID(
    final ARInstrumentInstanceID instanceID,
    final ARI1ParameterNumber id)
  {
    final var number = fromV1ParameterNumber(id);
    return ARParameterID.ofInstanceParameter(instanceID, number);
  }
}
