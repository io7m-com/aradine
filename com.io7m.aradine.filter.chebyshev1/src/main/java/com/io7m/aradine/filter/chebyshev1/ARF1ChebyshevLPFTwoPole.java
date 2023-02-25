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

import static com.io7m.aradine.filter.chebyshev1.ARF1ChebyshevCoefficients.ARF1ChebyshevType.LOW_PASS;

/**
 * <p>A recursive two-pole low pass Chebyshev filter. Filter instances are
 * stateful (they store the most recently processed output frames) and therefore
 * each instance should only be used on a single audio stream.</p>
 *
 * @see "https://www.w3.org/TR/audio-eq-cookbook/"
 */

public final class ARF1ChebyshevLPFTwoPole
{
  private ARF1ChebyshevCoefficients coefficients;
  private double cutoff;
  private double input_m1;
  private double input_m2;
  private double output_m1;
  private double output_m2;

  /**
   * Create a new filter.
   */

  public ARF1ChebyshevLPFTwoPole()
  {
    this.cutoff = 1.0;
    this.reset();
  }

  /**
   * Reset the internal state of the filter, and set the cutoff to {@code 1.0}.
   */

  public void reset()
  {
    this.input_m1 = 0.0;
    this.input_m2 = 0.0;
    this.output_m1 = 0.0;
    this.output_m2 = 0.0;
    this.setCutoff(this.cutoff);
  }

  /**
   * <p>Specify a new filter cutoff.</p>
   *
   * <p>The cutoff is a value in the range
   * {@code [0, 1]} where {@code 0} effectively blocks all frequencies,
   * {@code 0.5} blocks roughly half of the higher frequency spectrum, and
   * {@code 1.0} blocks nothing.</p>
   *
   * @param newCutoff The cutoff
   */

  public void setCutoff(
    final @ARNormalizedUnsigned double newCutoff)
  {
    this.cutoff =
      Math.min(1.0, Math.max(0.0, newCutoff));
    this.coefficients =
      ARF1ChebyshevCoefficients.calculateCoefficients(
        LOW_PASS,
        this.cutoff,
        0.005,
        2
      );
  }

  /**
   * Process one frame of input.
   *
   * @param input The input value
   *
   * @return The filtered output value
   */

  public double processOneFrame(
    final double input)
  {
    final var t0 = this.coefficients.a(0) * input;
    final var t1 = this.coefficients.a(1) * this.input_m1;
    final var t2 = this.coefficients.a(2) * this.input_m2;
    final var t3 = this.coefficients.b(1) * this.output_m1;
    final var t4 = this.coefficients.b(2) * this.output_m2;
    final var out = t0 + t1 + t2 + t3 + t4;

    /*
     * Record the most recent input and output frames.
     */

    this.input_m2 = this.input_m1;
    this.input_m1 = input;
    this.output_m2 = this.output_m1;
    this.output_m1 = out;
    return out;
  }
}
