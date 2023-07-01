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

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.DoubleRange;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public final class AROrder
{
  @Property(tries = 10000)
  public void testDivision(
    @ForAll @DoubleRange(min = -100_000.0, max = 100_000.0) double x,
    @ForAll @DoubleRange(min = -100_000.0, max = 100_000.0) double y,
    @ForAll @DoubleRange(min = -100_000.0, max = 100_000.0) double z)
  {
    assumeTrue(x <= y);
    assumeTrue(y <= z);

    final var n = y - x;
    final var d = z - x;

    final double r;
    if (d == 0.0) {
      r = 0.0;
    } else {
      r = n / d;
    }

    System.out.printf("x %f y %f z %f : %f / %f = %f%n", x, y, z, n, d, r);
    System.out.printf("%f - %f = %f%n", d, n, d - n);
    System.out.printf("%n");

    // Theorems
    assertTrue(r >= 0.0);
    assertTrue(r <= 1.0);

    // Corollaries
    assertTrue(0.0 <= n);
    assertTrue(0.0 <= d);
    assertTrue(n <= d);
  }

  @Property(tries = 10000)
  public void testLinear0(
    @ForAll @DoubleRange(min = -100_000.0, max = 100_000.0) double x,
    @ForAll @DoubleRange(min = -100_000.0, max = 100_000.0) double y,
    @ForAll @DoubleRange(min = 0.0, max = 1.0) double f)
  {
    assumeTrue(x <= y);

    final var l = x * (1.0 - f);
    final var r = y * f;
    final var m = l + r;

    System.out.printf("x %f y %f f %f%n",x,y,f);
    System.out.printf("%f = l %f + r %f (x <= l %s) %n",m, l, r, x<=l);

    assertTrue(x <= l);
    assertTrue(m >= (x - 0.000_000_000_001));
    assertTrue(m <= (y + 0.000_000_000_001));
  }
}
