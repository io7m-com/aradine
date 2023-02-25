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


package com.io7m.aradine.filter.chebyshev1;

import com.io7m.aradine.annotations.ARNormalizedUnsigned;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Functions to calculate Chebyshev filter coefficients.</p>
 *
 * @see "https://www.dspguide.com/ch20.htm"
 * @see "https://www.dspguide.com/ch33.htm"
 */

public final class ARF1ChebyshevCoefficients
{
  private final ARF1ChebyshevType type;
  private final double cutoff;
  private final double ripple;
  private final int poles;
  private final double[] a;
  private final double[] b;
  private final ArrayList<ARF1PoleCoefficients> poleCoefficients;

  private ARF1ChebyshevCoefficients(
    final ARF1ChebyshevType inType,
    final double inCutoff,
    final double inRipple,
    final int inPoles,
    final double[] inA,
    final double[] inB,
    final ArrayList<ARF1PoleCoefficients> inPoleCoefficients)
  {
    this.type = inType;
    this.cutoff = inCutoff;
    this.ripple = inRipple;
    this.poles = inPoles;
    this.a = inA;
    this.b = inB;
    this.poleCoefficients = inPoleCoefficients;
  }

  /**
   * <p>Calculate Chebyshev coefficients for a cascade of two-pole filters.</p>
   *
   * <p>This is a direct transcription of the algorithm given on page 340.</p>
   *
   * @param type   The filter type
   * @param cutoff The cutoff value
   * @param ripple The desired ripple value
   * @param poles  The number of poles
   *
   * @return The calculated coefficients
   */

  public static ARF1ChebyshevCoefficients calculateCoefficients(
    final ARF1ChebyshevType type,
    @ARNormalizedUnsigned final double cutoff,
    @ARNormalizedUnsigned final double ripple,
    final int poles)
  {
    checkPoleCount(poles);

    final var ta = new double[22];
    final var tb = new double[22];
    final var a = new double[22];
    final var b = new double[22];

    a[2] = 1.0;
    b[2] = 1.0;

    final var poleCoefficients = new ArrayList<ARF1PoleCoefficients>();
    for (int poleNumber = 1; poleNumber <= (poles / 2); ++poleNumber) {
      final var poleCo =
        calculatePoleCoefficients(
          type,
          poles,
          poleNumber,
          cutoff,
          ripple
        );

      poleCoefficients.add(poleCo);

      /*
       * "Add coefficients to the cascade."
       */

      for (int index = 0; index < 22; ++index) {
        ta[index] = a[index];
        tb[index] = b[index];
      }

      for (int index = 2; index < 22; ++index) {
        final var at0 = poleCo.a0 * ta[index];
        final var at1 = poleCo.a1 * ta[index - 1];
        final var at2 = poleCo.a2 * ta[index - 2];
        a[index] = at0 + at1 + at2;

        final var bt0 = tb[index];
        final var bt1 = poleCo.b1 * tb[index - 1];
        final var bt2 = poleCo.b2 * tb[index - 2];
        b[index] = bt0 - bt1 - bt2;
      }
    }

    /*
     * "Finish combining coefficients."
     */

    b[2] = 0.0;
    for (int index = 0; index < 20; ++index) {
      a[index] = a[index + 2];
      b[index] = -b[index + 2];
    }

    /*
     * "Normalize the gain."
     */

    var sa = 0.0;
    var sb = 0.0;

    switch (type) {
      case LOW_PASS -> {
        for (int index = 0; index < 20; ++index) {
          sa = sa + a[index];
          sb = sb + b[index];
        }
      }
      case HIGH_PASS -> {
        for (int index = 0; index < 20; ++index) {
          sa = sa + (a[index] * StrictMath.pow(-1.0, (double) index));
          sb = sb + (b[index] * StrictMath.pow(-1.0, (double) index));
        }
      }
    }

    final var gain = sa / (1.0 - sb);
    for (int index = 0; index < 20; ++index) {
      a[index] = a[index] / gain;
    }

    return new ARF1ChebyshevCoefficients(
      type,
      cutoff,
      ripple,
      poles,
      a,
      b,
      poleCoefficients
    );
  }

  private static void checkPoleCount(
    final int poles)
  {
    if ((poles == 0) || ((poles % 2) != 0) || (poles > 22)) {
      throw new IllegalArgumentException(
        "Number of poles %d must be even, > 0, and <= 22"
          .formatted(Integer.valueOf(poles))
      );
    }
  }

  /**
   * Calculate filter coefficients for the given pole.
   *
   * @param type       The type of filter
   * @param poleCount  The total number of filter poles
   * @param poleNumber The number of this filter pole
   * @param cutoff     The frequency cutoff value
   * @param ripple     The ripple percentage
   *
   * @return The pole coefficients
   */

  public static ARF1PoleCoefficients calculatePoleCoefficients(
    final ARF1ChebyshevType type,
    final int poleCount,
    final int poleNumber,
    final @ARNormalizedUnsigned double cutoff,
    final @ARNormalizedUnsigned double ripple)
  {
    /*
     * See "The Scientist And Engineer's Guide to DSP", chapter 20, page 341.
     */

    final var piOver2NP =
      Math.PI / ((double) poleCount * 2.0);
    final var piOverNP =
      Math.PI / (double) poleCount;
    final var pnM1 =
      (double) poleNumber - 1.0;

    /*
     * "Calculate the pole location on the unit circle".
     *
     * rpUnscaled = The real part of the pole position.
     * ipUnscaled = The imaginary part of the pole position.
     */

    final var rpUnscaled =
      -StrictMath.cos(piOver2NP + (pnM1 * piOverNP));
    final var ipUnscaled =
      StrictMath.sin(piOver2NP + (pnM1 * piOverNP));

    /*
     * "Warp from a circle to an ellipse."
     *
     * This refers to the fact that Butterworth filters (in other words,
     * Chebyshev filters with a ripple value of 0) have poles that lie
     * on a circle in the complex plane, whilst Chebyshev filters (all other
     * ripple values) lie on an ellipse.
     *
     * See: Equation 33-9, Page 624.
     */

    final double rp;
    final double ip;

    if (ripple != 0.0) {

      /*
       * This differs from the book's equation because we use percentage
       * values in the range [0, 1]
       */

      final var eta = StrictMath.sqrt(
        StrictMath.pow(1.0 / (1.0 - ripple), 2.0) - 1.0
      );

      final var k =
        StrictMath.cosh((1.0 / (double) poleCount) * areaCosineHyp(1.0 / eta));

      final var v =
        areaSineHyp(1.0 / eta) / (double) poleCount;

      rp = (rpUnscaled * StrictMath.sinh(v)) / k;
      ip = (ipUnscaled * StrictMath.cosh(v)) / k;
    } else {
      rp = rpUnscaled;
      ip = ipUnscaled;
    }

    /*
     * "S-Domain to Z-Domain conversion"
     */

    final var t = 2.0 * StrictMath.tan(0.5);
    final var w = 2.0 * Math.PI * cutoff;
    final var m = (rp * rp) + (ip * ip);
    final var tSquared = t * t;
    final var d0 = (4.0 - (4.0 * rp * t)) + (m * tSquared);
    final var x0 = tSquared / d0;
    final var x1 = (2.0 * tSquared) / d0;
    final var x2 = tSquared / d0;
    final var y1 = (8.0 - (2.0 * m * tSquared)) / d0;
    final var y2 = (-4.0 - (4.0 * rp * t) - (m * tSquared)) / d0;

    /*
     * "Low-pass to low-pass, or low-pass to high-pass".
     */

    final var k =
      switch (type) {
        case HIGH_PASS -> {
          yield -StrictMath.cos((w / 2.0) + 0.5) / StrictMath.cos((w / 2.0) - 0.5);
        }
        case LOW_PASS -> {
          yield StrictMath.sin(0.5 - (w / 2.0)) / StrictMath.sin(0.5 + (w / 2.0));
        }
      };

    final var ks = k * k;
    final var d1 = (1.0 + (y1 * k)) - (y2 * ks);

    final var a0 = ((x0 - (x1 * k)) + (x2 * ks)) / d1;
    final var a1 = (((-2.0 * x0 * k) + x1 + (x1 * ks)) - (2.0 * x2 * k)) / d1;
    final var a2 = (((x0 * ks) - (x1 * k)) + x2) / d1;
    final var b1 = (((2.0 * k) + y1 + (y1 * ks)) - (2 * y2 * k)) / d1;
    final var b2 = ((-ks - (y1 * k)) + y2) / d1;

    return switch (type) {
      case LOW_PASS -> new ARF1PoleCoefficients(a0, a1, a2, b1, b2);
      case HIGH_PASS -> new ARF1PoleCoefficients(a0, -a1, a2, -b1, b2);
    };
  }

  private static double areaCosineHyp(
    final double x)
  {
    return StrictMath.log(x + StrictMath.sqrt((x * x) - 1.0));
  }

  private static double areaSineHyp(
    final double x)
  {
    if (Double.isInfinite(x)) {
      return x;
    }
    if (x == 0.0) {
      return x;
    }
    return StrictMath.log(x + StrictMath.sqrt((x * x) + 1.0));
  }

  /**
   * @return The pole coefficients for each pair of filter poles
   */

  public List<ARF1PoleCoefficients> poleCoefficients()
  {
    return this.poleCoefficients;
  }

  /**
   * @param x The coefficient number
   *
   * @return Coefficient x (such as A0, A1, A2, etc)
   */

  public double a(
    final int x)
  {
    return this.a[x];
  }

  /**
   * @param x The coefficient number
   *
   * @return Coefficient x (such as B1, B2, B3, etc)
   */

  public double b(
    final int x)
  {
    return this.b[x];
  }

  /**
   * The type of filter for which to generate coefficients.
   */

  public enum ARF1ChebyshevType
  {
    /**
     * The filter is a low pass filter.
     */

    LOW_PASS,

    /**
     * The filter is a high pass filter.
     */

    HIGH_PASS
  }

  /**
   * The filter coefficients for a two pole filter.
   *
   * @param a0 The a0 coefficient
   * @param a1 The a1 coefficient
   * @param a2 The a2 coefficient
   * @param b1 The b1 coefficient
   * @param b2 The b2 coefficient
   */

  public record ARF1PoleCoefficients(
    double a0,
    double a1,
    double a2,
    double b1,
    double b2)
  {

  }
}
