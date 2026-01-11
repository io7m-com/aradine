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

import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.instrument.ARInstrumentDescription;
import com.io7m.aradine.api.instrument.ARInstrumentID;
import com.io7m.aradine.api.instrument.ARInstrumentIdentifiers;
import com.io7m.aradine.api.instrument.ARInstrumentInstanceID;
import com.io7m.aradine.api.instrument.ARInstrumentRole;
import com.io7m.aradine.api.parameters.ARParameterID;
import com.io7m.aradine.api.ports.ARPortID;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * An instrument that represents the system source.
 */

public final class AREnsInstrumentSystemSource
  implements AREnsInstrumentType
{
  private final ARInstrumentInstanceID instanceID;
  private final HashMap<ARPortID, AREnsPortInstanceType> ports;
  private final Map<ARPortID, AREnsPortInstanceType> portsRead;
  private final ARInstrumentDescription description;

  AREnsInstrumentSystemSource(
    final ARInstrumentInstanceID inInstanceID)
  {
    this.instanceID =
      Objects.requireNonNull(inInstanceID, "InstanceID");
    this.ports =
      new HashMap<>();
    this.portsRead =
      Collections.unmodifiableMap(this.ports);

    this.description =
      new ARInstrumentDescription(
        this.instanceID,
        ARInstrumentIdentifiers.systemTarget(),
        Map.of(),
        Map.of(),
        ARInstrumentRole.AR_SYSTEM_SOURCE
      );
  }

  @Override
  public Map<ARPortID, AREnsPortInstanceType> ports()
  {
    return this.portsRead;
  }

  @Override
  public Map<ARParameterID, AREnsParameterType> parameters()
  {
    return Map.of();
  }

  @Override
  public ARInstrumentDescription description()
  {
    return this.description;
  }

  @Override
  public ARInstrumentInstanceID instanceID()
  {
    return this.description().instanceId();
  }

  @Override
  public ARInstrumentID identifier()
  {
    return this.description().identifier();
  }

  @Override
  public ARInstrumentRole role()
  {
    return this.description().role();
  }

  @Override
  public void close()
    throws ARException
  {

  }
}
