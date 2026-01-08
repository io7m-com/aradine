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

package com.io7m.aradine.tests;

import com.io7m.aradine.api.instrument.ARInstrumentInstanceID;
import com.io7m.aradine.api.ports.ARInstrumentConnection;
import com.io7m.aradine.api.ports.ARPortConnection;
import com.io7m.aradine.api.ports.ARPortID;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class ARInstrumentConnectionTest
{
  @Test
  public void testConnectSelf0()
  {
    final var port0 =
      new ARPortID(UUID.randomUUID());
    final var port1 =
      new ARPortID(UUID.randomUUID());
    final var instrument =
      new ARInstrumentInstanceID(UUID.randomUUID());

    assertThrows(IllegalArgumentException.class, () -> {
      new ARInstrumentConnection(
        instrument,
        port0,
        instrument,
        port1
      );
    });
  }

  @Test
  public void testConnectSelf1()
  {
    final var port0 =
      new ARPortID(UUID.randomUUID());
    final var instrument0 =
      new ARInstrumentInstanceID(UUID.randomUUID());
    final var instrument1 =
      new ARInstrumentInstanceID(UUID.randomUUID());

    assertThrows(IllegalArgumentException.class, () -> {
      new ARInstrumentConnection(
        instrument0,
        port0,
        instrument1,
        port0
      );
    });
  }
}
