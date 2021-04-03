/*
 * Copyright © 2021 Mark Raynsford <code@io7m.com> http://io7m.com
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

import com.io7m.aradine.instrument.metadata.ARInstrumentVersion;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class ARVersionTest
{
  @Test
  public void testFormat0()
  {
    final var version =
      ARInstrumentVersion.builder()
        .setMajor(BigInteger.valueOf(1L))
        .setMinor(BigInteger.valueOf(2L))
        .setPatch(BigInteger.valueOf(3L))
        .setQualifier("SNAPSHOT")
        .build();

    final var formatted =
      String.format("%s", version);

    assertEquals("1.2.3-SNAPSHOT", formatted);
  }

  @Test
  public void testFormat1()
  {
    final var version =
      ARInstrumentVersion.builder()
        .setMajor(BigInteger.valueOf(1L))
        .setMinor(BigInteger.valueOf(2L))
        .setPatch(BigInteger.valueOf(3L))
        .build();

    final var formatted =
      String.format("%s", version);

    assertEquals("1.2.3", formatted);
  }
}
