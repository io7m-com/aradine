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
import com.io7m.aradine.api.ports.ARPortDirection;
import com.io7m.aradine.api.ports.ARPortID;
import com.io7m.aradine.api.ports.ARPortInstrument;
import com.io7m.aradine.api.ports.ARPortKind;
import com.io7m.aradine.api.ports.ARPortNumber;
import com.io7m.aradine.instrument.loader.api.ARInstrumentPortAssignerType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentDescription;
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
    final ARInstrumentPortAssignerType portAssigner,
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
      fromV1Ports(portAssigner, instanceID, description.ports())
    );
  }

  private static Map<ARPortID, ARPortInstrument>
  fromV1Ports(
    final ARInstrumentPortAssignerType portAssigner,
    final ARInstrumentInstanceID instanceID,
    final Map<ARI1PortNumber, ARI1PortDescription> ports)
  {
    return ports.entrySet()
      .stream()
      .map(e -> fromV1PortEntry(portAssigner, instanceID, e))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static Map.Entry<ARPortID, ARPortInstrument>
  fromV1PortEntry(
    final ARInstrumentPortAssignerType portAssigner,
    final ARInstrumentInstanceID instanceID,
    final Map.Entry<ARI1PortNumber, ARI1PortDescription> entry)
  {
    return Map.entry(
      fromV1PortID(portAssigner, instanceID, entry.getKey()),
      fromV1Port(portAssigner, instanceID, entry.getValue())
    );
  }

  private static ARPortInstrument fromV1Port(
    final ARInstrumentPortAssignerType portAssigner,
    final ARInstrumentInstanceID instanceID,
    final ARI1PortDescription port)
  {
    final var number =
      fromV1PortNumber(port.number());

    return new ARPortInstrument(
      instanceID,
      portAssigner.assign(instanceID, number),
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
      case AR_SOURCE -> ARPortDirection.AR_SOURCE;
      case AR_TARGET -> ARPortDirection.AR_TARGET;
    };
  }

  private static ARPortKind fromV1PortKind(
    final ARI1PortKind kind)
  {
    return switch (kind) {
      case AR_AUDIO -> ARPortKind.AR_AUDIO;
      case AR_NOTE -> ARPortKind.AR_NOTE;
    };
  }

  private static ARPortID fromV1PortID(
    final ARInstrumentPortAssignerType portAssigner,
    final ARInstrumentInstanceID instanceID,
    final ARI1PortNumber id)
  {
    final var number = fromV1PortNumber(id);
    return portAssigner.assign(instanceID, number);
  }
}
