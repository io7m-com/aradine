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

import com.io7m.aradine.instrument.spi1.ARI1InstrumentDescription;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionInteger;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionReal;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionSampleMap;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterId;
import com.io7m.aradine.instrument.spi1.ARI1PortDescriptionInputAudio;
import com.io7m.aradine.instrument.spi1.ARI1PortDescriptionInputNote;
import com.io7m.aradine.instrument.spi1.ARI1PortDescriptionInputType;
import com.io7m.aradine.instrument.spi1.ARI1PortDescriptionOutputAudio;
import com.io7m.aradine.instrument.spi1.ARI1PortDescriptionOutputType;
import com.io7m.aradine.instrument.spi1.ARI1PortDescriptionType;
import com.io7m.aradine.instrument.spi1.ARI1PortId;
import com.io7m.aradine.instrument.spi1.json_data.ARI1Schemas;

import java.util.List;
import java.util.Map;
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
      description.identifier(),
      description.version(),
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

  private static ARI1JPortDescriptionType toJSONPort(
    final ARI1PortDescriptionType p)
  {
    return switch (p) {
      case final ARI1PortDescriptionInputType pi -> {
        yield switch (pi) {
          case final ARI1PortDescriptionInputAudio ia -> {
            yield new ARI1JPortDescriptionInputAudio(
              ia.id(),
              ia.label(),
              ia.semantics(),
              ia.documentation()
            );
          }
          case final ARI1PortDescriptionInputNote in -> {
            yield new ARI1JPortDescriptionInputNote(
              in.id(),
              in.label(),
              in.semantics(),
              in.documentation()
            );
          }
        };
      }
      case final ARI1PortDescriptionOutputType po -> {
        yield switch (po) {
          case final ARI1PortDescriptionOutputAudio oa -> {
            yield new ARI1JPortDescriptionOutputAudio(
              oa.id(),
              oa.label(),
              oa.semantics(),
              oa.documentation()
            );
          }
        };
      }
    };
  }

  private static ARI1JParameterDescriptionType toJSONParameter(
    final ARI1ParameterDescriptionType p)
  {
    return switch (p) {
      case final ARI1ParameterDescriptionInteger di -> {
        yield new ARI1JParameterDescriptionInteger(
          di.id(),
          di.label(),
          di.documentation(),
          di.unitOfMeasurement(),
          di.valueMinimum(),
          di.valueMaximum(),
          di.valueDefault()
        );
      }
      case final ARI1ParameterDescriptionReal dr -> {
        yield new ARI1JParameterDescriptionReal(
          dr.id(),
          dr.label(),
          dr.documentation(),
          dr.unitOfMeasurement(),
          dr.valueMinimum(),
          dr.valueMaximum(),
          dr.valueDefault()
        );
      }
      case final ARI1ParameterDescriptionSampleMap ds -> {
        yield new ARI1JParameterDescriptionSampleMap(
          ds.id(),
          ds.label(),
          ds.documentation()
        );
      }
    };
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
      description.identifier(),
      description.version(),
      description.metadata(),
      fromJSONParameters(description.parameters()),
      fromJSONPorts(description.ports())
    );
  }

  private static Map<ARI1PortId, ARI1PortDescriptionType> fromJSONPorts(
    final List<ARI1JPortDescriptionType> ports)
  {
    return ports.stream()
      .map(p -> Map.entry(p.id(), p))
      .map(ARI1JInstrumentDescriptions::mapPortEntry)
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static Map.Entry<ARI1PortId, ARI1PortDescriptionType>
  mapPortEntry(
    final Map.Entry<ARI1PortId, ARI1JPortDescriptionType> e)
  {
    return Map.entry(e.getKey(), fromJSONPort(e.getValue()));
  }

  private static ARI1PortDescriptionType fromJSONPort(
    final ARI1JPortDescriptionType value)
  {
    return switch (value) {
      case final ARI1JPortDescriptionInputType i -> {
        yield switch (i) {
          case final ARI1JPortDescriptionInputAudio ia -> {
            yield new ARI1PortDescriptionInputAudio(
              ia.id(),
              ia.label(),
              ia.semantics(),
              ia.documentation()
            );
          }
          case final ARI1JPortDescriptionInputNote in -> {
            yield new ARI1PortDescriptionInputNote(
              in.id(),
              in.label(),
              in.semantics(),
              in.documentation()
            );
          }
        };
      }
      case final ARI1JPortDescriptionOutputType o -> {
        yield switch (o) {
          case final ARI1JPortDescriptionOutputAudio oa -> {
            yield new ARI1PortDescriptionOutputAudio(
              oa.id(),
              oa.label(),
              oa.semantics(),
              oa.documentation()
            );
          }
        };
      }
    };
  }

  private static Map<ARI1ParameterId, ARI1ParameterDescriptionType>
  fromJSONParameters(
    final List<ARI1JParameterDescriptionType> parameters)
  {
    return parameters.stream()
      .map(p -> Map.entry(p.id(), p))
      .map(ARI1JInstrumentDescriptions::fromJSONParameterEntry)
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static Map.Entry<ARI1ParameterId, ARI1ParameterDescriptionType>
  fromJSONParameterEntry(
    final Map.Entry<ARI1ParameterId, ARI1JParameterDescriptionType> e)
  {
    return Map.entry(e.getKey(), fromJSONParameter(e.getValue()));
  }

  private static ARI1ParameterDescriptionType
  fromJSONParameter(
    final ARI1JParameterDescriptionType value)
  {
    return switch (value) {
      case final ARI1JParameterDescriptionInteger pi -> {
        yield new ARI1ParameterDescriptionInteger(
          pi.id(),
          pi.label(),
          pi.documentation(),
          pi.unitOfMeasurement(),
          pi.valueMinimum(),
          pi.valueMaximum(),
          pi.valueDefault()
        );
      }
      case final ARI1JParameterDescriptionReal pr -> {
        yield new ARI1ParameterDescriptionReal(
          pr.id(),
          pr.label(),
          pr.documentation(),
          pr.unitOfMeasurement(),
          pr.valueMinimum(),
          pr.valueMaximum(),
          pr.valueDefault()
        );
      }
      case final ARI1JParameterDescriptionSampleMap psm -> {
        yield new ARI1ParameterDescriptionSampleMap(
          psm.id(),
          psm.label(),
          psm.documentation()
        );
      }
    };
  }
}
