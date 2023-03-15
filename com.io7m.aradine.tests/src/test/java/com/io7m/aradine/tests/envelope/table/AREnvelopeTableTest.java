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

import com.io7m.aradine.envelope.table1.AREnvelopeTable;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import org.junit.jupiter.api.Test;

import static com.io7m.aradine.envelope.table1.AREnvelopeInterpolation.CONSTANT_CURRENT;
import static com.io7m.aradine.envelope.table1.AREnvelopeInterpolation.CONSTANT_NEXT;
import static com.io7m.aradine.envelope.table1.AREnvelopeInterpolation.LINEAR;
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
    final @ForAll long time)
  {
    final var env = AREnvelopeTable.create(48000L);
    assertEquals(1.0, env.evaluate(time));
  }

  /**
   * An envelope with two linear points is linear.
   */

  @Test
  public void testLinearSimple()
  {
    final var env = AREnvelopeTable.create(48000L);
    env.setFirst(0.0, LINEAR);
    env.setPoint(1000.0, 1.0, LINEAR);

    assertEquals(1000.0, env.endMilliseconds());

    for (long time = 0L; time < 48000L * 2L; ++time) {
      final var amp = env.evaluate(time);
      if (time < 48000L) {
        assertTrue(amp >= 0.0);
        assertTrue(amp <= 1.0);
      } else {
        assertEquals(1.0, amp);
      }
    }

    env.setSampleRate(44100L);

    for (long time = 0L; time < 44100L * 2L; ++time) {
      final var amp = env.evaluate(time);
      if (time < 44100L) {
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
    final var env = AREnvelopeTable.create(48000L);
    env.setFirst(0.0, CONSTANT_CURRENT);
    env.setPoint(100.0, 0.1, CONSTANT_CURRENT);
    env.setPoint(200.0, 0.2, CONSTANT_NEXT);
    env.setPoint(300.0, 0.3, CONSTANT_CURRENT);

    assertEquals(300.0, env.endMilliseconds());

    for (double time = 0.0; time <= 300.0; time += 0.1) {
      final var amp = env.evaluateAtMilliseconds(time);
      if (time <= 99.9) {
        assertEquals(0.0, amp);
      }
      if (time >= 100.0 && time <= 199.9) {
        assertEquals(0.1, amp);
      }
      if (time >= 200.0 && time < 300.0) {
        assertEquals(0.3, amp);
      }
      if (time == 300.0) {
        assertEquals(0.3, amp);
      }
    }

    env.setSampleRate(44100L);
    assertEquals(300.0, env.endMilliseconds());

    for (double time = 0.0; time <= 300.0; time += 0.1) {
      final var amp = env.evaluateAtMilliseconds(time);
      if (time <= 99.9) {
        assertEquals(0.0, amp);
      }
      if (time >= 100.0 && time <= 199.9) {
        assertEquals(0.1, amp);
      }
      if (time >= 200.0 && time < 300.0) {
        assertEquals(0.3, amp);
      }
      if (time == 300.0) {
        assertEquals(0.3, amp);
      }
    }
  }
}
