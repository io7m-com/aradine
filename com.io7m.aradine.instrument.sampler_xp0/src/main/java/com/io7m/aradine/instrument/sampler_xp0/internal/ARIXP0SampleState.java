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

package com.io7m.aradine.instrument.sampler_xp0.internal;

import com.io7m.aradine.instrument.spi1.ARI1PitchBend;
import com.io7m.aradine.instrument.spi1.ARI1SampleMapEntryType;

import java.util.Objects;

/**
 * The playback state of a sample.
 */

public final class ARIXP0SampleState
{
  private final ARI1SampleMapEntryType sample;
  private final double velocity;
  private final double frameLast;
  private double loopPoint;
  private State state;
  private double frameLoop;
  private double positionReal;
  private long position;
  private int pitchBendRange;

  /**
   * The playback state of a sample.
   *
   * @param inSample   The sample map entry
   * @param inVelocity The velocity
   */

  public ARIXP0SampleState(
    final ARI1SampleMapEntryType inSample,
    final double inVelocity)
  {
    this.sample = Objects.requireNonNull(inSample, "sample");
    this.position = 0L;
    this.positionReal = 0.0;
    this.velocity = inVelocity;
    this.frameLast = (double) this.sample.frames() - 1.0;
    this.state = State.PRE_LOOP;
    this.setPitchBendRange(24);
    this.setLoopPoint(0.9);
  }

  /**
   * Set the pitch bend range.
   *
   * @param newSemitones The semitones range
   */

  public void setPitchBendRange(
    final int newSemitones)
  {
    this.pitchBendRange = Math.min(Math.max(1, newSemitones), 120);
  }

  /**
   * @return The current pitch bend range
   */

  public int pitchBendRange()
  {
    return this.pitchBendRange;
  }

  /**
   * Set the loop point.
   *
   * @param newLoopPoint The loop point
   */

  public void setLoopPoint(
    final double newLoopPoint)
  {
    this.loopPoint = Math.max(Math.min(1.0, newLoopPoint), 0.0);
    this.frameLoop = this.frameLast * this.loopPoint;
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
    this.sample.evaluate(this.position, this.velocity, frame);

    final double rateScale =
      ARI1PitchBend.pitchBendToPlaybackRate(pitchBend, this.pitchBendRange());

    final var delta =
      this.sample.playbackRate() * rateScale;

    this.positionReal =
      switch (this.state) {
        case PRE_LOOP -> {
          final double x = this.positionReal + delta;
          if (x >= this.frameLoop) {
            this.state = State.LOOP_FORWARD;
          }
          yield x;
        }
        case LOOP_FORWARD -> {
          final double x = this.positionReal + delta;
          if (x > this.frameLast) {
            this.state = State.LOOP_BACKWARD;
            yield this.frameLast;
          }
          yield x;
        }
        case LOOP_BACKWARD -> {
          final double x = this.positionReal - delta;
          if (x < this.frameLoop) {
            this.state = State.LOOP_FORWARD;
            yield this.frameLoop;
          }
          yield x;
        }
      };

    this.position = Math.round(this.positionReal);
  }

  /**
   * @return The velocity
   */

  public double velocity()
  {
    return this.velocity;
  }

  /**
   * @return The most recent playback position
   */

  public long position()
  {
    return this.position;
  }

  /**
   * @return The most recent playback position
   */

  public double positionReal()
  {
    return this.positionReal;
  }

  private enum State
  {
    PRE_LOOP,
    LOOP_FORWARD,
    LOOP_BACKWARD
  }
}
