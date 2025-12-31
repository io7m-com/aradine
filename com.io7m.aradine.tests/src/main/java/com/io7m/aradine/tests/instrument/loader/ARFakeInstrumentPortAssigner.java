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

package com.io7m.aradine.tests.instrument.loader;

import com.io7m.aradine.api.instrument.ARInstrumentInstanceID;
import com.io7m.aradine.api.ports.ARPortID;
import com.io7m.aradine.api.ports.ARPortNumber;
import com.io7m.aradine.instrument.loader.api.ARInstrumentPortAssignerType;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ARFakeInstrumentPortAssigner
  implements ARInstrumentPortAssignerType
{
  private final ConcurrentHashMap<InstanceAndNumber, ARPortID> assignments;

  public ARFakeInstrumentPortAssigner()
  {
    this.assignments =
      new ConcurrentHashMap<InstanceAndNumber, ARPortID>();
  }

  private record InstanceAndNumber(
    ARInstrumentInstanceID instance,
    ARPortNumber portNumber)
  {

  }

  @Override
  public ARPortID assign(
    final ARInstrumentInstanceID instance,
    final ARPortNumber portNumber)
  {
    return this.assignments.computeIfAbsent(
      new InstanceAndNumber(instance, portNumber),
      _ -> new ARPortID(UUID.randomUUID())
    );
  }
}
