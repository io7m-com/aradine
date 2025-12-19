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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.io7m.aradine.instrument.spi1.ARI1DottedName;
import com.io7m.aradine.instrument.spi1.ARI1Version;
import com.io7m.aradine.instrument.spi1.json_data.ARI1Schemas;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Create an instrument description.
 *
 * @param schema     The schema identifier
 * @param group      The instrument group
 * @param identifier The unique identifier of the instrument
 * @param version    The version of the instrument
 * @param metadata   The metadata strings declared in the instrument description
 * @param parameters The parameters declared by the instrument
 * @param ports      The ports declared by the instrument
 */

public record ARI1JInstrumentDescription(
  @JsonPropertyDescription("The schema identifier.")
  @JsonProperty(value = "%Schema", required = true)
  String schema,
  @JsonPropertyDescription("The instrument group.")
  @JsonProperty(value = "Group", required = true)
  ARI1DottedName group,
  @JsonPropertyDescription("The instrument ID.")
  @JsonProperty(value = "ID", required = true)
  ARI1DottedName identifier,
  @JsonPropertyDescription("The instrument version.")
  @JsonProperty(value = "Version", required = true)
  ARI1Version version,
  @JsonPropertyDescription("The instrument metadata.")
  @JsonProperty("Metadata")
  Map<String, String> metadata,
  @JsonPropertyDescription("The instrument parameters.")
  @JsonProperty("Parameters")
  List<ARI1JParameterDescriptionType> parameters,
  @JsonPropertyDescription("The instrument ports.")
  @JsonProperty("Ports")
  List<ARI1JPortDescriptionType> ports)
  implements ARI1JElementType
{
  /**
   * Create an instrument description.
   *
   * @param schema     The schema identifier
   * @param group      The instrument group
   * @param identifier The unique identifier of the instrument
   * @param version    The version of the instrument
   * @param metadata   The metadata strings declared in the instrument description
   * @param parameters The parameters declared by the instrument
   * @param ports      The ports declared by the instrument
   */

  public ARI1JInstrumentDescription
  {
    Objects.requireNonNull(schema, "schema");
    Objects.requireNonNull(group, "group");
    Objects.requireNonNull(identifier, "identifier");
    Objects.requireNonNull(version, "version");
    Objects.requireNonNull(metadata, "metadata");
    Objects.requireNonNull(parameters, "parameters");
    Objects.requireNonNull(ports, "ports");

    metadata = Map.copyOf(metadata);
    parameters = List.copyOf(parameters);
    ports = List.copyOf(ports);

    checkUniqueness(parameters, ARI1JParameterDescriptionType::id);
    checkUniqueness(ports, ARI1JPortDescriptionType::id);
    checkSchemaIdentifier(schema);
  }

  private static void checkSchemaIdentifier(
    final String schema)
  {
    if (!schema.equals(ARI1Schemas.schema1().toString())) {
      throw new IllegalArgumentException(
        "Schema identifier must be %s".formatted(ARI1Schemas.schema1())
      );
    }
  }

  private static <T, I> void checkUniqueness(
    final List<T> parameters,
    final Function<T, I> extractUnique)
  {
    final var set = new HashSet<I>(parameters.size());
    for (final var p : parameters) {
      final var i = extractUnique.apply(p);
      if (set.contains(i)) {
        throw new IllegalArgumentException(
          "Duplicate ID: %s".formatted(i)
        );
      }
      set.add(i);
    }
  }
}
