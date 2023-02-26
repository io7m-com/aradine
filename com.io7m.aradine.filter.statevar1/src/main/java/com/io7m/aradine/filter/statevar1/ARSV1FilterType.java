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


package com.io7m.aradine.filter.statevar1;

import com.io7m.aradine.annotations.ARNormalizedUnsigned;

/**
 * The type of state variable filters.
 */

public interface ARSV1FilterType
{
  /**
   * <p>Specify a new frequency cutoff value.</p>
   *
   * @param newCutoff The cutoff
   */

  void setCutoff(
    @ARNormalizedUnsigned double newCutoff);

  /**
   * <p>Specify a new Q value.</p>
   *
   * @param newQ The Q
   */

  void setQ(
    double newQ);

  /**
   * @return The current cutoff value
   */

  double cutoff();

  /**
   * @return The current Q value
   */

  double q();

  /**
   * Reset all internal state variables, leaving {@code cutoff} and {@code q}
   * untouched.
   */

  void reset();

  /**
   * @return The low pass output of the most recent filter evaluation
   */

  double lowPassOutput();

  /**
   * @return The high pass output of the most recent filter evaluation
   */

  double highPassOutput();

  /**
   * @return The band pass output of the most recent filter evaluation
   */

  double bandPassOutput();

  /**
   * @return The band reject output of the most recent filter evaluation
   */

  double bandRejectOutput();

  /**
   * Process a single frame of input.
   *
   * @param input The input
   */

  void processOneFrame(
    double input);
}
