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


package com.io7m.aradine.envelope.table;

import com.io7m.aradine.annotations.ARNormalizedUnsigned;
import com.io7m.aradine.annotations.ARTimeFrames;

import java.util.Objects;

import static com.io7m.aradine.envelope.table.AREnvelopeADRState.STATE_ATTACK;
import static com.io7m.aradine.envelope.table.AREnvelopeADRState.STATE_RELEASE;
import static com.io7m.aradine.envelope.table.AREnvelopeADRState.STATE_SUSTAIN;

/**
 * A stateful ADR envelope evaluator.
 */

public final class AREnvelopeADREvaluator
{
  private final AREnvelopeADR envelope;
  private @ARNormalizedUnsigned double releaseScale;
  private @ARNormalizedUnsigned double ampMostRecent;
  private AREnvelopeADRState state;
  private @ARTimeFrames long stateStarted;

  /**
   * Construct an evaluator.
   *
   * @param inEnvelope The underlying envelope
   */

  public AREnvelopeADREvaluator(
    final AREnvelopeADR inEnvelope)
  {
    this.envelope =
      Objects.requireNonNull(inEnvelope, "inEnvelope");
    this.state =
      STATE_ATTACK;
    this.stateStarted =
      0L;
    this.ampMostRecent =
      0.0;
    this.releaseScale =
      1.0;
  }

  /**
   * @return The current envelope state
   */

  public AREnvelopeADRState state()
  {
    return this.state;
  }

  /**
   * Begin the release state of the envelope.
   *
   * @param time     The current time
   * @param relative {@code true} if the amplitude of the release should be
   *                 relative to the most recent amplitude
   */

  public void beginRelease(
    final @ARTimeFrames long time,
    final boolean relative)
  {
    this.state = STATE_RELEASE;
    this.stateStarted = time;

    if (relative) {
      this.releaseScale = this.ampMostRecent;
    }
  }

  /**
   * Evaluate the envelope.
   *
   * @param time The current time
   *
   * @return The amplitude
   */

  public @ARNormalizedUnsigned double evaluate(
    final @ARTimeFrames long time)
  {
    return switch (this.state) {
      case STATE_ATTACK -> {
        final var attack = this.envelope.attack();
        if (time >= attack.endFrames()) {
          this.state = STATE_SUSTAIN;
          this.stateStarted = time;
          yield this.evaluate(this.timeClamped(time));
        }

        this.ampMostRecent =
          attack.evaluate(this.timeClamped(time));

        yield this.ampMostRecent;
      }

      case STATE_SUSTAIN -> {
        final var sustain =
          this.envelope.sustain();
        final var timeClamped =
          this.timeClamped(time);
        final var timeEnd =
          sustain.endFrames();

        final long timeCyclic;
        if (timeEnd == 0L) {
          timeCyclic = 0L;
        } else {
          timeCyclic = timeClamped % sustain.endFrames();
        }

        this.ampMostRecent =
          sustain.evaluate(timeCyclic);

        yield this.ampMostRecent;
      }

      case STATE_RELEASE -> {
        final var timeClamped =
          this.timeClamped(time);

        this.ampMostRecent =
          this.releaseScale * this.envelope.release().evaluate(timeClamped);

        yield this.ampMostRecent;
      }
    };
  }

  private @ARTimeFrames long timeClamped(
    final @ARTimeFrames long time)
  {
    return Math.max(0L, time - this.stateStarted);
  }
}
