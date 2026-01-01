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

import com.io7m.aradine.api.ports.ARPortID;
import com.io7m.aradine.api.ports.ARPort;

import java.util.Map;
import java.util.Objects;

/**
 * A description of a loaded instrument.
 *
 * @param instanceId The instance ID
 * @param identifier The instrument identifier
 * @param ports      The instrument ports
 */

public record ARInstrumentDescription(
  ARInstrumentInstanceID instanceId,
  ARInstrumentID identifier,
  Map<ARPortID, ARPort> ports)
{
  /**
   * A description of a loaded instrument.
   *
   * @param instanceId The instance ID
   * @param identifier The instrument identifier
   * @param ports      The instrument ports
   */

  public ARInstrumentDescription
  {
    Objects.requireNonNull(instanceId, "InstanceID");
    Objects.requireNonNull(identifier, "Identifier");
    ports = Map.copyOf(ports);

    for (final var entry : ports.entrySet()) {
      this.checkEntry(instanceId, entry);
    }
  }

  private void checkEntry(
    final ARInstrumentInstanceID expectedInstanceId,
    final Map.Entry<ARPortID, ARPort> entry)
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
}
