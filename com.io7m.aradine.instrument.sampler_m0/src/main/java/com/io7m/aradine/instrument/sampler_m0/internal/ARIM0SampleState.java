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

package com.io7m.aradine.instrument.sampler_m0.internal;

import com.io7m.aradine.instrument.spi1.ARI1PitchBend;
import com.io7m.aradine.instrument.spi1.ARI1SampleMapEntryType;

import java.util.Arrays;
import java.util.Objects;

/**
 * The playback state of a sample.
 */

public final class ARIM0SampleState
{
  private final ARI1SampleMapEntryType sample;
  private final double velocity;
  private boolean done;
  private double positionReal;
  private long position;

  /**
   * The playback state of a sample.
   *
   * @param inSample The sample map entry
   */

  ARIM0SampleState(
    final ARI1SampleMapEntryType inSample,
    final double inVelocity)
  {
    this.sample = Objects.requireNonNull(inSample, "sample");
    this.position = 0;
    this.positionReal = 0.0;
    this.velocity = inVelocity;
    this.done = false;
  }

  /**
   * Evaluate the sample.
   *
   * @param pitchBend The current pitch bend value
   * @param frame     The output frame
   */

  public void evaluate(
    final double pitchBend,
    final double[] frame)
  {
    if (this.done) {
      Arrays.fill(frame, 0.0);
      return;
    }

    this.sample.evaluate(this.position, this.velocity, frame);

    final double rateScale =
      ARI1PitchBend.pitchBendToPlaybackRate(pitchBend, 24);

    final var newPositionReal =
      this.positionReal + (this.sample.playbackRate() * rateScale);
    final var newPosition =
      Math.round(newPositionReal);

    if (newPosition >= this.sample.frames()) {
      this.done = true;
    }

    this.positionReal = newPositionReal;
    this.position = newPosition;
  }
}
