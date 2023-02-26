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


package com.io7m.aradine.filter.biquad1;

/**
 * Functions to calculate Q values for biquad filters.
 */

public final class ARBQ1BiquadQs
{
  private ARBQ1BiquadQs()
  {

  }

  /**
   * Calculate a set of Q values for a series of cascaded filters to yield
   * Butterworth-style responses.
   *
   * @param order The filter order (must be even, and nonzero)
   *
   * @return The Q values
   */

  public static double[] butterworthStyleCascadedQValues(
    final int order)
  {
    if ((order <= 0) || ((order % 2) != 0)) {
      throw new IllegalArgumentException(
        "Must use a positive, even number of poles.");
    }

    final var pairs =
      order / 2;
    final var poleInc =
      Math.PI / (double) order;
    final var firstAngle =
      poleInc / 2.0;

    final var qValues = new double[pairs];
    for (int index = 0; index < pairs; ++index) {
      final var q =
        1.0 / (2.0 * StrictMath.cos(firstAngle + ((double) index * poleInc)));
      qValues[index] = q;
    }
    return qValues;
  }
}
