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

package com.io7m.aradine.tests.envelope.table;

import com.io7m.aradine.envelope.table.AREnvelopeTable;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import org.junit.jupiter.api.Test;

import static com.io7m.aradine.envelope.table.AREnvelopeInterpolation.CONSTANT_CURRENT;
import static com.io7m.aradine.envelope.table.AREnvelopeInterpolation.CONSTANT_NEXT;
import static com.io7m.aradine.envelope.table.AREnvelopeInterpolation.LINEAR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class AREnvelopeTableTest
{
  /**
   * An empty envelope always returns 1.0.
   *
   * @param time The time
   */

  @Property
  public void testEmpty(
    final @ForAll double time)
  {
    final var env = AREnvelopeTable.create();
    assertEquals(1.0, env.evaluate(time));
  }

  /**
   * An envelope with two linear points is linear.
   */

  @Test
  public void testLinearSimple()
  {
    final var env = AREnvelopeTable.create();
    env.setFirst(0.0, LINEAR);
    env.setPoint(1000.0, 1.0, LINEAR);

    assertEquals(1000.0, env.end());

    for (double time = 0.0; time < 2000.0; time += 0.1) {
      final var amp = env.evaluate(time);
      if (time < 1000.0) {
        assertTrue(amp >= 0.0);
        assertTrue(amp <= 1.0);
      } else {
        assertEquals(1.0, amp);
      }
    }
  }

  /**
   * An envelope with constant points is constant.
   */

  @Test
  public void testConstantSimple()
  {
    final var env = AREnvelopeTable.create();
    env.setFirst(0.0, CONSTANT_CURRENT);
    env.setPoint(100.0, 0.1, CONSTANT_CURRENT);
    env.setPoint(200.0, 0.2, CONSTANT_NEXT);
    env.setPoint(300.0, 0.3, CONSTANT_CURRENT);

    assertEquals(300.0, env.end());

    for (double time = 0.0; time <= 300.0; time += 0.1) {
      final var amp = env.evaluate(time);
      if (time >= 0.0 && time < 100.0) {
        assertEquals(0.0, env.evaluate(time));
      }
      if (time >= 100.0 && time < 200.0) {
        assertEquals(0.1, env.evaluate(time));
      }
      if (time >= 200.0 && time < 300.0) {
        assertEquals(0.3, env.evaluate(time));
      }
      if (time == 300.0) {
        assertEquals(0.3, env.evaluate(time));
      }
    }
  }
}
