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


package com.io7m.aradine.tests.filter.recursive1;

import com.io7m.aradine.filter.chebyshev1.ARF1ChebyshevCoefficients;
import org.junit.jupiter.api.Test;

import static com.io7m.aradine.filter.chebyshev1.ARF1ChebyshevCoefficients.ARF1ChebyshevType.HIGH_PASS;
import static com.io7m.aradine.filter.chebyshev1.ARF1ChebyshevCoefficients.ARF1ChebyshevType.LOW_PASS;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Functions to test the generation of Chebyshev filter coefficients.
 */

public final class ARF1ChebyshevCoefficientsTest
{
  /**
   * <p>See "The Scientist And Engineer's Guide to DSP", chapter 20, page
   * 342.</p>
   *
   * <p>This is data set 1.</p>
   */

  @Test
  public void testDataSet1()
  {
    final var co = ARF1ChebyshevCoefficients.calculatePoleCoefficients(
      LOW_PASS,
      4,
      1,
      0.1,
      0.0
    );

    assertAll(
      () -> assertEquals(0.061885, co.a0(), 0.000001),
      () -> assertEquals(0.123770, co.a1(), 0.000001),
      () -> assertEquals(0.061885, co.a2(), 0.000001),
      () -> assertEquals(1.048600, co.b1(), 0.000001),
      () -> assertEquals(-0.296140, co.b2(), 0.000001)
    );
  }

  /**
   * <p>See "The Scientist And Engineer's Guide to DSP", chapter 20, page
   * 342.</p>
   *
   * <p>This is data set 2.</p>
   */

  @Test
  public void testDataSet2()
  {
    final var co = ARF1ChebyshevCoefficients.calculatePoleCoefficients(
      HIGH_PASS,
      4,
      2,
      0.1,
      0.1
    );

    assertAll(
      () -> assertEquals(0.922919, co.a0(), 0.000001),
      () -> assertEquals(-1.845840, co.a1(), 0.000001),
      () -> assertEquals(0.922919, co.a2(), 0.000001),
      () -> assertEquals(1.446913, co.b1(), 0.000001),
      () -> assertEquals(-0.836653, co.b2(), 0.000001)
    );
  }

  /**
   * <p>See "The Scientist And Engineer's Guide to DSP", chapter 20, page
   * 336.</p>
   */

  @Test
  public void testP336_001_2()
  {
    final var co = ARF1ChebyshevCoefficients.calculateCoefficients(
      LOW_PASS,
      0.01,
      0.005,
      2
    );

    assertAll(
      () -> assertEquals(8.663387E-04, co.a(0), 0.000001),
      () -> assertEquals(1.732678E-03, co.a(1), 0.000001),
      () -> assertEquals(8.663387E-04, co.a(2), 0.000001),
      () -> assertEquals(1.919129E+00, co.b(1), 0.000001),
      () -> assertEquals(-9.225943E-01, co.b(2), 0.000001)
    );
  }

  /**
   * <p>See "The Scientist And Engineer's Guide to DSP", chapter 20, page
   * 336.</p>
   */

  @Test
  public void testP336_045_2()
  {
    final var co = ARF1ChebyshevCoefficients.calculateCoefficients(
      LOW_PASS,
      0.45,
      0.005,
      2
    );

    assertAll(
      () -> assertEquals(8.001101E-01, co.a(0), 0.000001),
      () -> assertEquals(1.600220E+00, co.a(1), 0.000001),
      () -> assertEquals(8.001101E-01, co.a(2), 0.000001),
      () -> assertEquals(-1.556269E+00, co.b(1), 0.000001),
      () -> assertEquals(-6.441713E-01, co.b(2), 0.000001)
    );
  }
}
