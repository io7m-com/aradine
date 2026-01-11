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
import com.io7m.aradine.instrument.spi1.ARI1DottedName;
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
import com.io7m.aradine.instrument.spi1.ARI1Version;
import com.io7m.aradine.instrument.spi1.ARI1VersionQualifier;
import com.io7m.lanark.core.RDottedName;
import com.io7m.verona.core.Version;
import com.io7m.verona.core.VersionQualifier;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Functions to convert instrument descriptions.
 */

public final class AREnsSPI1InstrumentDescriptions
{
  private AREnsSPI1InstrumentDescriptions()
  {

  }

  /**
   * Convert an SPI1 instrument description to a core description.
   *
   * @param instanceID  The instance ID
   * @param description The description
   *
   * @return The resulting description
   */

  public static ARInstrumentDescription ofV1(
    final ARInstrumentInstanceID instanceID,
    final ARI1InstrumentDescription description)
  {
    return new ARInstrumentDescription(
      instanceID,
      ofV1Identifier(
        description.group(),
        description.identifier(),
        description.version()
      ),
      ofV1Ports(instanceID, description.ports()),
      ofV1Parameters(instanceID, description.parameters()),
      ARInstrumentRole.AR_INSTRUMENT
    );
  }

  private static Map<ARPortID, ARPortDescription> ofV1Ports(
    final ARInstrumentInstanceID instanceID,
    final Map<ARI1PortNumber, ARI1PortDescription> ports)
  {
    return ports.entrySet()
      .stream()
      .map(e -> ofV1PortEntry(instanceID, e))
      .collect(
        Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue)
      );
  }

  private static Map.Entry<ARPortID, ARPortDescription> ofV1PortEntry(
    final ARInstrumentInstanceID instanceID,
    final Map.Entry<ARI1PortNumber, ARI1PortDescription> e)
  {
    final var number =
      new ARPortNumber(e.getKey().value());

    return Map.entry(
      ARPortID.ofInstancePort(instanceID, number),
      ofV1PortDescription(instanceID, e.getValue())
    );
  }

  private static ARPortDescription ofV1PortDescription(
    final ARInstrumentInstanceID instanceID,
    final ARI1PortDescription value)
  {
    final var number =
      new ARPortNumber(value.number().value());

    return new ARPortDescription(
      instanceID,
      ARPortID.ofInstancePort(instanceID, number),
      ofV1PortKind(value.kind()),
      ofV1PortDirection(value.direction()),
      number,
      value.label(),
      value.semantics()
    );
  }

  private static ARPortDirection ofV1PortDirection(
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

  private static ARPortKind ofV1PortKind(
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

  private static Map<ARParameterID, ARParameterDescriptionType>
  ofV1Parameters(
    final ARInstrumentInstanceID instanceID,
    final Map<ARI1ParameterNumber, ARI1ParameterDescriptionType> ports)
  {
    return ports.entrySet()
      .stream()
      .map(e -> ofV1ParameterEntry(instanceID, e))
      .collect(
        Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue)
      );
  }

  private static Map.Entry<ARParameterID, ARParameterDescriptionType>
  ofV1ParameterEntry(
    final ARInstrumentInstanceID instanceID,
    final Map.Entry<ARI1ParameterNumber, ARI1ParameterDescriptionType> e)
  {
    final var number =
      new ARParameterNumber(e.getKey().value());

    return Map.entry(
      ARParameterID.ofInstanceParameter(instanceID, number),
      ofV1ParameterDescription(instanceID, e.getValue())
    );
  }

  private static ARParameterDescriptionType ofV1ParameterDescription(
    final ARInstrumentInstanceID instanceID,
    final ARI1ParameterDescriptionType value)
  {
    return switch (value) {
      case final ARI1ParameterDescriptionInteger i -> {
        yield ofV1ParameterDescriptionInteger(instanceID, i);
      }
      case final ARI1ParameterDescriptionReal r -> {
        yield ofV1ParameterDescriptionReal(instanceID, r);
      }
      case final ARI1ParameterDescriptionSampleMap sm -> {
        yield ofV1ParameterDescriptionSampleMap(instanceID, sm);
      }
    };
  }

  private static ARParameterDescriptionType ofV1ParameterDescriptionSampleMap(
    final ARInstrumentInstanceID instanceID,
    final ARI1ParameterDescriptionSampleMap sm)
  {
    final var number =
      new ARParameterNumber(sm.id().value());

    return new ARParameterDescriptionSampleMap(
      instanceID,
      number,
      ARParameterID.ofInstanceParameter(instanceID, number),
      sm.label()
    );
  }

  private static ARParameterDescriptionType ofV1ParameterDescriptionReal(
    final ARInstrumentInstanceID instanceID,
    final ARI1ParameterDescriptionReal r)
  {
    final var number =
      new ARParameterNumber(r.id().value());

    return new ARParameterDescriptionReal(
      instanceID,
      number,
      ARParameterID.ofInstanceParameter(instanceID, number),
      r.label(),
      ofV1DottedName(r.unitOfMeasurement()),
      r.valueMinimum(),
      r.valueMaximum(),
      r.valueDefault()
    );
  }

  private static ARParameterDescriptionType ofV1ParameterDescriptionInteger(
    final ARInstrumentInstanceID instanceID,
    final ARI1ParameterDescriptionInteger i)
  {
    final var number =
      new ARParameterNumber(i.id().value());

    return new ARParameterDescriptionInteger(
      instanceID,
      number,
      ARParameterID.ofInstanceParameter(instanceID, number),
      i.label(),
      ofV1DottedName(i.unitOfMeasurement()),
      i.valueMinimum(),
      i.valueMaximum(),
      i.valueDefault()
    );
  }

  private static ARInstrumentID ofV1Identifier(
    final ARI1DottedName group,
    final ARI1DottedName identifier,
    final ARI1Version version)
  {
    return new ARInstrumentID(
      ofV1DottedName(group),
      ofV1DottedName(identifier),
      ofV1Version(version)
    );
  }

  private static Version ofV1Version(
    final ARI1Version version)
  {
    return new Version(
      version.major(),
      version.minor(),
      version.patch(),
      version.qualifier()
        .map(AREnsSPI1InstrumentDescriptions::ofV1VersionQualifier)
    );
  }

  private static VersionQualifier ofV1VersionQualifier(
    final ARI1VersionQualifier q)
  {
    return new VersionQualifier(q.text());
  }

  private static RDottedName ofV1DottedName(
    final ARI1DottedName name)
  {
    return new RDottedName(name.value());
  }
}
