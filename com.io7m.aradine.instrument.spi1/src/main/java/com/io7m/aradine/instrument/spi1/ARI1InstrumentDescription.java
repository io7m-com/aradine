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

package com.io7m.aradine.instrument.spi1;

import java.util.Map;
import java.util.Objects;

/**
 * Create an instrument description.
 *
 * @param group      The group of the instrument
 * @param identifier The unique identifier of the instrument
 * @param version    The version of the instrument
 * @param metadata   The metadata strings declared in the instrument description
 * @param parameters The parameters declared by the instrument
 * @param ports      The ports declared by the instrument
 */

public record ARI1InstrumentDescription(
  ARI1DottedName group,
  ARI1DottedName identifier,
  ARI1Version version,
  Map<String, String> metadata,
  Map<ARI1ParameterId, ARI1ParameterDescriptionType> parameters,
  Map<ARI1PortId, ARI1PortDescriptionType> ports)
{
  /**
   * Create an instrument description.
   *
   * @param group      The group of the instrument
   * @param identifier The unique identifier of the instrument
   * @param version    The version of the instrument
   * @param metadata   The metadata strings declared in the instrument description
   * @param parameters The parameters declared by the instrument
   * @param ports      The ports declared by the instrument
   */

  public ARI1InstrumentDescription
  {
    Objects.requireNonNull(group, "group");
    Objects.requireNonNull(identifier, "identifier");
    Objects.requireNonNull(version, "version");
    Objects.requireNonNull(metadata, "metadata");
    Objects.requireNonNull(parameters, "parameters");
    Objects.requireNonNull(ports, "ports");

    metadata = Map.copyOf(metadata);
    parameters = Map.copyOf(parameters);
    ports = Map.copyOf(ports);
  }
}
