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


package com.io7m.aradine.tests;

import com.io7m.aradine.api.ports.ARPortNumber;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class ARPortNumberTest
{
  /**
   * Test that the toString method reflects the contents of the class.
   *
   * @param id0 Value A
   * @param id1 Value B
   */

  @Property
  public void testPortIdToString(
    final @ForAll ARPortNumber id0,
    final @ForAll ARPortNumber id1)
  {
    if (id0.equals(id1)) {
      assertEquals(id0.toString(), id1.toString());
    } else {
      assertNotEquals(id0.toString(), id1.toString());
    }
  }

  @Property
  public void testOrder0(
    final @ForAll @IntRange(min = 0, max = Integer.MAX_VALUE) int x,
    final @ForAll @IntRange(min = 0, max = Integer.MAX_VALUE) int y)
  {
    assertEquals(
      Integer.compareUnsigned(x, y),
      new ARPortNumber(x).compareTo(new ARPortNumber(y))
    );
  }

  @Test
  public void testOutOfRange0()
  {
    assertThrows(IllegalArgumentException.class, () -> {
      new ARPortNumber(42949672956L);
    });
  }

  @Test
  public void testOutOfRange1()
  {
    assertThrows(IllegalArgumentException.class, () -> {
      new ARPortNumber(-1L);
    });
  }
}
