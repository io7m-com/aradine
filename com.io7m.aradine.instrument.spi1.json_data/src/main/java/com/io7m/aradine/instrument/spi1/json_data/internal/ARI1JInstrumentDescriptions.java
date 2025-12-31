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

package com.io7m.aradine.instrument.spi1.json_data.internal;

import com.io7m.aradine.instrument.spi1.ARI1Documentation;
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
import com.io7m.aradine.instrument.spi1.json_data.ARI1Schemas;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Functions to transform instrument descriptions.
 */

public final class ARI1JInstrumentDescriptions
{
  private ARI1JInstrumentDescriptions()
  {

  }

  /**
   * Convert to JSON form.
   *
   * @param description The source description
   *
   * @return The JSON form
   */

  public static ARI1JInstrumentDescription toJson(
    final ARI1InstrumentDescription description)
  {
    return new ARI1JInstrumentDescription(
      ARI1Schemas.schema1().toString(),
      toJSONDottedName(description.group()),
      toJSONDottedName(description.identifier()),
      toJSONVersion(description.version()),
      description.metadata(),
      description.parameters()
        .values()
        .stream()
        .map(ARI1JInstrumentDescriptions::toJSONParameter)
        .toList(),
      description.ports()
        .values()
        .stream()
        .map(ARI1JInstrumentDescriptions::toJSONPort)
        .toList()
    );
  }

  private static ARI1JVersion toJSONVersion(
    final ARI1Version version)
  {
    return new ARI1JVersion(
      version.major(),
      version.minor(),
      version.patch(),
      toJSONVersionQualifierOpt(version.qualifier())
    );
  }

  private static Optional<ARI1JVersionQualifier> toJSONVersionQualifierOpt(
    final Optional<ARI1VersionQualifier> qualifier)
  {
    return qualifier.map(q -> new ARI1JVersionQualifier(q.text()));
  }

  private static ARI1JPortDescription toJSONPort(
    final ARI1PortDescription p)
  {
    return new ARI1JPortDescription(
      toJSONPortKind(p.kind()),
      toJSONPortDirection(p.direction()),
      toJSONPortNumber(p.number()),
      p.label(),
      p.semantics(),
      toJSONDocumentationOpt(p.documentation())
    );
  }

  private static Optional<ARI1JDocumentation> toJSONDocumentationOpt(
    final Optional<ARI1Documentation> documentation)
  {
    return documentation.map(ARI1JInstrumentDescriptions::toJSONDocumentation);
  }

  private static ARI1JDocumentation toJSONDocumentation(
    final ARI1Documentation x)
  {
    return new ARI1JDocumentation(
      toJSONDottedName(x.format()),
      x.lines()
    );
  }

  private static ARI1JDottedName toJSONDottedName(
    final ARI1DottedName format)
  {
    return new ARI1JDottedName(format.value());
  }

  private static ARI1JPortNumber toJSONPortNumber(
    final ARI1PortNumber number)
  {
    return new ARI1JPortNumber(number.value());
  }

  private static ARI1JPortDirection toJSONPortDirection(
    final ARI1PortDirection direction)
  {
    return switch (direction) {
      case AR_SOURCE -> ARI1JPortDirection.AR_SOURCE;
      case AR_TARGET -> ARI1JPortDirection.AR_TARGET;
    };
  }

  private static ARI1JPortKind toJSONPortKind(
    final ARI1PortKind kind)
  {
    return switch (kind) {
      case AR_AUDIO -> ARI1JPortKind.AR_AUDIO;
      case AR_NOTE -> ARI1JPortKind.AR_NOTE;
    };
  }

  private static ARI1JParameterDescriptionType toJSONParameter(
    final ARI1ParameterDescriptionType p)
  {
    return switch (p) {
      case final ARI1ParameterDescriptionInteger di -> {
        yield new ARI1JParameterDescriptionInteger(
          toJSONParameterNumber(di.id()),
          di.label(),
          toJSONDocumentationOpt(di.documentation()),
          toJSONDottedName(di.unitOfMeasurement()),
          di.valueMinimum(),
          di.valueMaximum(),
          di.valueDefault()
        );
      }
      case final ARI1ParameterDescriptionReal dr -> {
        yield new ARI1JParameterDescriptionReal(
          toJSONParameterNumber(dr.id()),
          dr.label(),
          toJSONDocumentationOpt(dr.documentation()),
          toJSONDottedName(dr.unitOfMeasurement()),
          dr.valueMinimum(),
          dr.valueMaximum(),
          dr.valueDefault()
        );
      }
      case final ARI1ParameterDescriptionSampleMap ds -> {
        yield new ARI1JParameterDescriptionSampleMap(
          toJSONParameterNumber(ds.id()),
          ds.label(),
          toJSONDocumentationOpt(ds.documentation())
        );
      }
    };
  }

  private static ARI1JParameterNumber toJSONParameterNumber(
    final ARI1ParameterNumber id)
  {
    return new ARI1JParameterNumber(id.value());
  }

  /**
   * Convert from JSON form.
   *
   * @param description The source description
   *
   * @return The v1 form
   */

  public static ARI1InstrumentDescription fromJSON(
    final ARI1JInstrumentDescription description)
  {
    return new ARI1InstrumentDescription(
      fromJSONDottedName(description.group()),
      fromJSONDottedName(description.identifier()),
      fromJSONVersion(description.version()),
      description.metadata(),
      fromJSONParameters(description.parameters()),
      fromJSONPorts(description.ports())
    );
  }

  private static ARI1Version fromJSONVersion(
    final ARI1JVersion version)
  {
    return new ARI1Version(
      version.major(),
      version.minor(),
      version.patch(),
      version.qualifier().map(x -> new ARI1VersionQualifier(x.text()))
    );
  }

  private static Map<ARI1PortNumber, ARI1PortDescription> fromJSONPorts(
    final List<ARI1JPortDescription> ports)
  {
    return ports.stream()
      .map(p -> Map.entry(p.number(), p))
      .map(ARI1JInstrumentDescriptions::mapPortEntry)
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static Map.Entry<ARI1PortNumber, ARI1PortDescription>
  mapPortEntry(
    final Map.Entry<ARI1JPortNumber, ARI1JPortDescription> e)
  {
    return Map.entry(
      fromJSONPortNumber(e.getKey()),
      fromJSONPort(e.getValue())
    );
  }

  private static ARI1PortDescription fromJSONPort(
    final ARI1JPortDescription value)
  {
    return new ARI1PortDescription(
      fromJSONPortKind(value.kind()),
      fromJSONPortDirection(value.direction()),
      fromJSONPortNumber(value.number()),
      value.label(),
      value.semantics(),
      fromJSONDocumentationOpt(value.documentation())
    );
  }

  private static Optional<ARI1Documentation> fromJSONDocumentationOpt(
    final Optional<ARI1JDocumentation> documentation)
  {
    return documentation.map(ARI1JInstrumentDescriptions::fromJSONDocumentation);
  }

  private static ARI1Documentation fromJSONDocumentation(
    final ARI1JDocumentation x)
  {
    return new ARI1Documentation(fromJSONDottedName(x.format()), x.lines());
  }

  private static ARI1DottedName fromJSONDottedName(
    final ARI1JDottedName format)
  {
    return new ARI1DottedName(format.value());
  }

  private static ARI1PortNumber fromJSONPortNumber(
    final ARI1JPortNumber number)
  {
    return new ARI1PortNumber(number.value());
  }

  private static ARI1PortDirection fromJSONPortDirection(
    final ARI1JPortDirection direction)
  {
    return switch (direction) {
      case AR_SOURCE -> ARI1PortDirection.AR_SOURCE;
      case AR_TARGET -> ARI1PortDirection.AR_TARGET;
    };
  }

  private static ARI1PortKind fromJSONPortKind(
    final ARI1JPortKind kind)
  {
    return switch (kind) {
      case AR_AUDIO -> ARI1PortKind.AR_AUDIO;
      case AR_NOTE -> ARI1PortKind.AR_NOTE;
    };
  }

  private static Map<ARI1ParameterNumber, ARI1ParameterDescriptionType>
  fromJSONParameters(
    final List<ARI1JParameterDescriptionType> parameters)
  {
    return parameters.stream()
      .map(p -> Map.entry(p.number(), p))
      .map(ARI1JInstrumentDescriptions::fromJSONParameterEntry)
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static Map.Entry<ARI1ParameterNumber, ARI1ParameterDescriptionType>
  fromJSONParameterEntry(
    final Map.Entry<ARI1JParameterNumber, ARI1JParameterDescriptionType> e)
  {
    return Map.entry(
      fromJSONParameterNumber(e.getKey()),
      fromJSONParameter(e.getValue())
    );
  }

  private static ARI1ParameterDescriptionType
  fromJSONParameter(
    final ARI1JParameterDescriptionType value)
  {
    return switch (value) {
      case final ARI1JParameterDescriptionInteger pi -> {
        yield new ARI1ParameterDescriptionInteger(
          fromJSONParameterNumber(pi.number()),
          pi.label(),
          fromJSONDocumentationOpt(pi.documentation()),
          fromJSONDottedName(pi.unitOfMeasurement()),
          pi.valueMinimum(),
          pi.valueMaximum(),
          pi.valueDefault()
        );
      }
      case final ARI1JParameterDescriptionReal pr -> {
        yield new ARI1ParameterDescriptionReal(
          fromJSONParameterNumber(pr.number()),
          pr.label(),
          fromJSONDocumentationOpt(pr.documentation()),
          fromJSONDottedName(pr.unitOfMeasurement()),
          pr.valueMinimum(),
          pr.valueMaximum(),
          pr.valueDefault()
        );
      }
      case final ARI1JParameterDescriptionSampleMap psm -> {
        yield new ARI1ParameterDescriptionSampleMap(
          fromJSONParameterNumber(psm.number()),
          psm.label(),
          fromJSONDocumentationOpt(psm.documentation())
        );
      }
    };
  }

  private static ARI1ParameterNumber fromJSONParameterNumber(
    final ARI1JParameterNumber id)
  {
    return new ARI1ParameterNumber(id.value());
  }
}
