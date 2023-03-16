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


package com.io7m.aradine.envelope.table1;

import com.io7m.aradine.annotations.ARNormalizedUnsigned;
import com.io7m.aradine.annotations.ARTimeFrames;
import com.io7m.aradine.annotations.ARTimeFramesPerSecond;
import com.io7m.aradine.annotations.ARTimeMilliseconds;

import java.util.TreeMap;

import static com.io7m.aradine.envelope.table1.AREnvelopeInterpolation.LINEAR;

/**
 * A table1 of nodes comprising an envelope.
 */

public final class AREnvelopeTable
{
  private static final Long ZERO =
    Long.valueOf(0L);

  private TreeMap<Long, AREnvelopeNode> points;
  private @ARTimeFramesPerSecond long sampleRate;

  private AREnvelopeTable(
    final @ARTimeFramesPerSecond long inSampleRate)
  {
    this.points = new TreeMap<>();
    this.setSampleRate(inSampleRate);
  }

  /**
   * Set the new sample rate.
   *
   * @param inSampleRate The new sample rate
   */

  public void setSampleRate(
    final long inSampleRate)
  {
    this.sampleRate = Math.max(0L, inSampleRate);

    final var newPoints = new TreeMap<Long, AREnvelopeNode>();
    for (final var entry : this.points.entrySet()) {
      final var node = entry.getValue();
      final var newNode =
        new AREnvelopeNode(
          node.time(),
          millisecondsToFrames(this.sampleRate, node.time()),
          node.amplitude(),
          node.interpolation()
        );
      newPoints.put(Long.valueOf(newNode.timeFrames()), newNode);
    }

    this.points = newPoints;
  }

  private static @ARTimeFrames long millisecondsToFrames(
    final @ARTimeFramesPerSecond double sampleRate,
    final @ARTimeMilliseconds double milliseconds)
  {
    return Math.round((sampleRate * (milliseconds / 1000.0)));
  }

  /**
   * Create a new envelope.
   *
   * @param sampleRate The current sample rate
   *
   * @return The envelope
   */

  public static AREnvelopeTable create(
    final @ARTimeFramesPerSecond long sampleRate)
  {
    final var table = new AREnvelopeTable(sampleRate);
    table.points.put(
      ZERO,
      new AREnvelopeNode(0.0, 0L, 1.0, LINEAR)
    );
    return table;
  }

  /**
   * Evaluate the envelope at the given time.
   *
   * @param time The time
   *
   * @return An amplitude value
   */

  public double evaluate(
    final @ARTimeFrames long time)
  {
    final var timeClamped =
      Math.max(0L, time);
    final var timeBoxed =
      Long.valueOf(timeClamped);
    final var nodePrevious =
      this.points.floorEntry(timeBoxed)
        .getValue();
    final var entryNext =
      this.points.ceilingEntry(timeBoxed);

    if (entryNext == null) {
      return nodePrevious.amplitude();
    }

    final var nodeNext =
      entryNext.getValue();
    final var timePrevious =
      nodePrevious.timeFrames();
    final var timeNext =
      nodeNext.timeFrames();
    final var timeUpper =
      timeNext - timePrevious;

    if (timeUpper == 0L) {
      return nodePrevious.amplitude();
    }

    final var timeCurrent =
      timeClamped - timePrevious;
    final var timeNormal =
      (double) timeCurrent / (double) timeUpper;

    return switch (nodePrevious.interpolation()) {
      case LINEAR -> {
        yield interpolateLinear(
          nodePrevious.amplitude(),
          nodeNext.amplitude(),
          timeNormal
        );
      }

      case CONSTANT_CURRENT -> {
        yield nodePrevious.amplitude();
      }

      case CONSTANT_NEXT -> {
        yield nodeNext.amplitude();
      }

      case EXPONENTIAL -> {
        yield interpolateLinear(
          nodePrevious.amplitude(),
          nodeNext.amplitude(),
          timeNormal * timeNormal
        );
      }

      case LOGARITHMIC -> {
        yield interpolateLinear(
          nodePrevious.amplitude(),
          nodeNext.amplitude(),
          StrictMath.sqrt(timeNormal)
        );
      }

      case COSINE -> {
        yield interpolateCosine(
          nodePrevious.amplitude(),
          nodeNext.amplitude(),
          timeNormal
        );
      }
    };
  }

  /**
   * Evaluate the envelope at the given time.
   *
   * @param time The time
   *
   * @return An amplitude value
   */

  public double evaluateAtMilliseconds(
    final @ARTimeMilliseconds double time)
  {
    return this.evaluate(millisecondsToFrames(this.sampleRate, time));
  }

  /**
   * @return The time of the last envelope node
   */

  public @ARTimeMilliseconds double endMilliseconds()
  {
    return this.points.lastEntry()
      .getValue()
      .time();
  }

  /**
   * @return The time of the last envelope node
   */

  public @ARTimeFrames long endFrames()
  {
    return this.points.lastEntry()
      .getValue()
      .timeFrames();
  }

  private static double interpolateLinear(
    final double x0,
    final double x1,
    final double factor)
  {
    return (x0 * (1.0 - factor)) + (x1 * factor);
  }

  private static double interpolateCosine(
    final double x0,
    final double x1,
    final double factor)
  {
    final var ft = factor * Math.PI;
    final var f = (1.0 - StrictMath.cos(ft)) * 0.5;
    return (x0 * (1.0 - f)) + (x1 * f);
  }

  /**
   * Set the amplitude and interpolation of the first node.
   *
   * @param amplitude     The amplitude
   * @param interpolation The interpolation
   */

  public void setFirst(
    final @ARNormalizedUnsigned double amplitude,
    final AREnvelopeInterpolation interpolation)
  {
    final var ampClamped =
      Math.min(1.0, Math.max(0.0, amplitude));

    this.points.put(
      ZERO,
      new AREnvelopeNode(0.0, 0L, ampClamped, interpolation)
    );
  }

  /**
   * Set a point in the envelope.
   *
   * @param time          The time
   * @param amplitude     The amplitude
   * @param interpolation The interpolation
   */

  public void setPoint(
    final @ARTimeMilliseconds double time,
    final @ARNormalizedUnsigned double amplitude,
    final AREnvelopeInterpolation interpolation)
  {
    final var timeClamped =
      Math.max(0.0, time);
    final var timeFrame =
      millisecondsToFrames(this.sampleRate, timeClamped);

    final var ampClamped =
      Math.min(1.0, Math.max(0.0, amplitude));

    this.points.put(
      Long.valueOf(timeFrame),
      new AREnvelopeNode(timeClamped, timeFrame, ampClamped, interpolation)
    );
  }
}
