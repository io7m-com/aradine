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

package com.io7m.aradine.api.instrument;

import com.io7m.aradine.api.parameters.ARParameterDescriptionType;
import com.io7m.aradine.api.parameters.ARParameterID;
import com.io7m.aradine.api.ports.ARPortDescription;
import com.io7m.aradine.api.ports.ARPortID;

import java.util.Map;
import java.util.Objects;

/**
 * A description of a loaded instrument.
 *
 * @param instanceId The instance ID
 * @param identifier The instrument identifier
 * @param ports      The instrument ports
 * @param parameters The instrument parameters
 * @param role       The instrument role
 */

public record ARInstrumentDescription(
  ARInstrumentInstanceID instanceId,
  ARInstrumentID identifier,
  Map<ARPortID, ARPortDescription> ports,
  Map<ARParameterID, ARParameterDescriptionType> parameters,
  ARInstrumentRole role)
{
  /**
   * A description of a loaded instrument.
   *
   * @param instanceId The instance ID
   * @param identifier The instrument identifier
   * @param ports      The instrument ports
   * @param parameters The instrument parameters
   * @param role       The instrument role
   */

  public ARInstrumentDescription
  {
    Objects.requireNonNull(instanceId, "InstanceID");
    Objects.requireNonNull(identifier, "Identifier");
    ports = Map.copyOf(ports);
    parameters = Map.copyOf(parameters);
    Objects.requireNonNull(role, "Role");

    for (final var entry : ports.entrySet()) {
      this.checkEntryPort(instanceId, entry);
    }
    for (final var entry : parameters.entrySet()) {
      this.checkEntryParameter(instanceId, entry);
    }
  }

  private void checkEntryPort(
    final ARInstrumentInstanceID expectedInstanceId,
    final Map.Entry<ARPortID, ARPortDescription> entry)
  {
    final var portId = entry.getKey();
    final var port = entry.getValue();

    if (!Objects.equals(portId, port.id())) {
      throw new IllegalArgumentException(
        "Port map key %s must match port ID %s".formatted(portId, port.id())
      );
    }
    if (!Objects.equals(port.instrumentInstance(), expectedInstanceId)) {
      throw new IllegalArgumentException(
        "Port instrument instance %s must match %s"
          .formatted(port.instrumentInstance(), expectedInstanceId)
      );
    }
  }

  private void checkEntryParameter(
    final ARInstrumentInstanceID expectedInstanceId,
    final Map.Entry<ARParameterID, ARParameterDescriptionType> entry)
  {
    final var parameterID = entry.getKey();
    final var parameter = entry.getValue();

    if (!Objects.equals(parameterID, parameter.id())) {
      throw new IllegalArgumentException(
        "Parameter map key %s must match parameter ID %s"
          .formatted(parameterID, parameter.id())
      );
    }
    if (!Objects.equals(parameter.instrumentInstance(), expectedInstanceId)) {
      throw new IllegalArgumentException(
        "Parameter instrument instance %s must match %s"
          .formatted(parameter.instrumentInstance(), expectedInstanceId)
      );
    }
  }
}
