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

package com.io7m.aradine.instrument.grain_sampler_m0.internal;

import com.io7m.aradine.annotations.ARTimeFrames;
import com.io7m.aradine.instrument.spi1.ARI1PitchBend;
import com.io7m.aradine.instrument.spi1.ARI1RNGDeterministicType;
import com.io7m.aradine.instrument.spi1.ARI1SampleMapEntryType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * The playback state of a sample.
 */

public final class ARIGM0SampleState
{
  private boolean done;
  private double positionReal;
  private double speed;
  private final ARI1RNGDeterministicType sampleJitterRNG;
  private final ARI1SampleMapEntryType sample;
  private final ArrayList<Grain> grains;
  private final double velocity;
  private @ARTimeFrames final long grainLength;
  private @ARTimeFrames int grainPositionJitter;
  private @ARTimeFrames long grainTimer;
  private @ARTimeFrames long grainTimerMax;
  private long position;
  private int pitchBendRange;

  private static double hannWindow(
    final long length,
    final long position)
  {
    final var lengthR =
      (double) length;
    final var positionR =
      (double) position;

    return 0.5 * (1.0 - StrictMath.cos((2.0 * StrictMath.PI * positionR) / lengthR));
  }

  private static double hannPositiveWindow(
    final long length,
    final long position)
  {
    final var lengthR =
      (double) length;
    final var positionR =
      (double) position;

    return StrictMath.cos((StrictMath.PI * positionR) / lengthR);
  }

  private final class Grain
  {
    private final long localLength;
    private final long sampleStartPosition;
    private long localPosition;
    private double localPositionReal;
    private boolean grainDone;

    Grain(
      final long inSampleStartPosition,
      final long inGrainLength)
    {
      this.sampleStartPosition =
        Math.max(0L, inSampleStartPosition);
      final var clampedGrainLength =
        Math.max(0L, inGrainLength);

      final var sampleRef =
        ARIGM0SampleState.this.sample;
      final var sampleEnd =
        this.sampleStartPosition + clampedGrainLength;

      final var sampleMaxFrames = sampleRef.frames();
      if (sampleEnd >= sampleMaxFrames) {
        this.localLength = Math.max(
          0L,
          clampedGrainLength - (sampleEnd - sampleMaxFrames));
      } else {
        this.localLength = clampedGrainLength;
      }

      this.localPosition = 0L;
      this.localPositionReal = 0.0;
    }

    public void evaluate(
      final double pitchBend,
      final double[] frame)
    {
      if (this.grainDone || this.localLength == 0L) {
        Arrays.fill(frame, 0.0);
        return;
      }

      final var samplePosition =
        this.sampleStartPosition + this.localPosition;

      final var sampleRef =
        ARIGM0SampleState.this.sample;

      sampleRef.evaluate(
        samplePosition,
        ARIGM0SampleState.this.velocity,
        frame
      );

      /*
       * If this is the first grain, use a positive Hann window to preserve
       * the initial attack of the note.
       */

      if (this.sampleStartPosition == 0L) {
        for (var index = 0; index < frame.length; ++index) {
          frame[index] *= hannPositiveWindow(
            this.localLength,
            this.localPosition
          );
        }
      } else {
        for (var index = 0; index < frame.length; ++index) {
          frame[index] *= hannWindow(
            this.localLength,
            this.localPosition
          );
        }
      }

      final var rateScale =
        ARI1PitchBend.pitchBendToPlaybackRate(
          pitchBend,
          ARIGM0SampleState.this.pitchBendRange
        );

      final var newLocalPositionReal =
        this.localPositionReal + (sampleRef.playbackRate() * rateScale);
      final var newLocalPosition =
        Math.round(newLocalPositionReal);

      if (newLocalPosition >= this.localLength) {
        this.grainDone = true;
      } else {
        this.localPositionReal = newLocalPositionReal;
        this.localPosition = newLocalPosition;
      }
    }
  }

  /**
   * @return The current maximum grain sample position jitter in frames
   */

  @ARTimeFrames
  public int grainPositionJitter()
  {
    return this.grainPositionJitter;
  }

  /**
   * Set the maximum grain sample position jitter in frames.
   *
   * @param inJitter The frames
   */

  public void setGrainPositionJitter(
    final @ARTimeFrames int inJitter)
  {
    this.grainPositionJitter = Math.max(0, inJitter);
  }

  /**
   * The playback state of a sample.
   *
   * @param inSample The sample map entry
   */

  ARIGM0SampleState(
    final ARI1SampleMapEntryType inSample,
    final ARI1RNGDeterministicType inRandom,
    final double inVelocity,
    final long inGrainLength)
  {
    this.sample =
      Objects.requireNonNull(inSample, "sample");
    this.sampleJitterRNG =
      Objects.requireNonNull(inRandom, "inRandom");

    this.velocity = inVelocity;
    this.grainLength = inGrainLength;
    this.grains = new ArrayList<>(4);

    this.grainTimerMax = inGrainLength / 2L;
    this.grainTimer = 0L;
    this.grainPositionJitter = 0;

    this.speed = 1.0;
    this.positionReal = 0.0;
    this.position = 0L;
    this.pitchBendRange = 24;
  }

  /**
   * @return The current pitch bend range
   */

  public int pitchBendRange()
  {
    return this.pitchBendRange;
  }

  /**
   * Set the new pitch bend range.
   *
   * @param newRange The new range
   */

  public void setPitchBendRange(
    final int newRange)
  {
    this.pitchBendRange = newRange;
  }

  /**
   * Set the playback speed.
   *
   * @param newSpeed The speed
   */

  public void setSpeed(
    final double newSpeed)
  {
    this.speed = newSpeed;
  }

  /**
   * @return The current playback speed
   */

  public double speed()
  {
    return this.speed;
  }

  private void setGrainTimerMax(
    final long timer)
  {
    this.grainTimerMax = Math.max(0L, timer);
  }

  private void setGrainTimer(
    final long newTime)
  {
    this.grainTimer = Math.max(0L, newTime);
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
    Arrays.fill(frame, 0.0);

    /*
     * The grain rate needs to be scaled along with the pitch bend. When
     * the pitch of grains is reduced, grains will effectively run for longer
     * and so will tend to stack up and increase the gain. When the pitch
     * of grains is increased, the grains will complete faster which can
     * lead to a kind of amplitude modulation effect. By scaling the grain
     * rate, the rate is kept consistent with the rate that grains are
     * completing.
     */

    final var rateScale =
      ARI1PitchBend.pitchBendToPlaybackRate(pitchBend, this.pitchBendRange);

    this.setGrainTimerMax((long) ((this.grainLength / 2.0) / rateScale));

    /*
     * If the sample isn't "done", then instantiate grains as necessary.
     */

    if (!this.done) {
      if (this.grainTimer == 0L) {

        /*
         * Apply jitter to the starting position within the sample of the
         * new grain. This helps to alleviate comb filtering issues caused
         * by having perfectly aligned grains.
         *
         * This jitter is _not_ applied to the very first grain, because
         * doing so might interfere with the attack of the note in the case
         * of percussive samples.
         */

        final long jitter;
        if (this.position > 0L) {
          final var r = (this.sampleJitterRNG.random() * 2.0) - 1.0;
          jitter = (long) (r * (double) this.grainPositionJitter);
        } else {
          jitter = 0L;
        }

        /*
         * Instantiate a new grain and restart the timer.
         */

        this.grains.add(new Grain(this.position + jitter, this.grainLength));
        this.setGrainTimer(this.grainTimerMax);
      } else {
        this.setGrainTimer(this.grainTimer - 1L);
      }

      /*
       * Increase the current sample playback position.
       */

      final var newPositionReal =
        this.positionReal + this.speed;
      final var newPosition =
        Math.round(newPositionReal);

      if (newPosition >= this.sample.frames()) {
        this.done = true;
      } else {
        this.positionReal = newPositionReal;
        this.position = newPosition;
      }
    }

    /*
     * If there are no grains remaining to process, then give up here.
     */

    if (this.grains.isEmpty()) {
      return;
    }

    /*
     * Evaluate each active grain, summing the outputs of all active grains,
     * and remove any grains that are "done".
     */

    final var frameTmp = new double[frame.length];
    final var iterator = this.grains.iterator();
    while (iterator.hasNext()) {
      final var grain = iterator.next();
      if (grain.grainDone) {
        iterator.remove();
        continue;
      }

      grain.evaluate(pitchBend, frameTmp);
      for (var index = 0; index < frame.length; ++index) {
        frame[index] += frameTmp[index];
      }
    }
  }
}
