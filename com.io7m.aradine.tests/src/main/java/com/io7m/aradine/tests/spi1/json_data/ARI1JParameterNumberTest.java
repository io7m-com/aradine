/*
 * Copyright © 2022 Mark Raynsford <code@io7m.com> https://www.io7m.com
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


package com.io7m.aradine.tests.spi1.json_data;

import com.io7m.aradine.instrument.spi1.ARI1ParameterNumber;
import com.io7m.aradine.instrument.spi1.json_data.internal.ARI1JParameterNumber;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class ARI1JParameterNumberTest
{
  /**
   * Test that the toString method reflects the contents of the class.
   *
   * @param id0 Value A
   * @param id1 Value B
   */

  @Property
  public void testParameterIdToString(
    final @ForAll ARI1ParameterNumber id0,
    final @ForAll ARI1ParameterNumber id1)
  {
    final var jid0 = new ARI1JParameterNumber(id0.value());
    final var jid1 = new ARI1JParameterNumber(id1.value());

    if (id0.equals(id1)) {
      assertEquals(jid0.toString(), jid1.toString());
    } else {
      assertNotEquals(jid0.toString(), jid1.toString());
    }
  }

  @Test
  public void testOutOfRange0()
  {
    assertThrows(IllegalArgumentException.class, () -> {
      new ARI1JParameterNumber(42949672956L);
    });
  }

  @Test
  public void testOutOfRange1()
  {
    assertThrows(IllegalArgumentException.class, () -> {
      new ARI1JParameterNumber(-1L);
    });
  }
}
