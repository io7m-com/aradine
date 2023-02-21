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


package com.io7m.aradine.tests.arbitraries;

import com.io7m.aradine.instrument.spi1.ARI1EventConfigurationParameterChanged;
import com.io7m.aradine.instrument.spi1.ARI1EventNoteOff;
import com.io7m.aradine.instrument.spi1.ARI1EventNoteOn;
import com.io7m.aradine.instrument.spi1.ARI1EventNotePitchBend;
import com.io7m.aradine.instrument.spi1.ARI1EventType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterId;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;

import java.util.Set;

/**
 * A provider of values.
 */

public final class ARI1ProviderEvent
  implements ArbitraryProvider
{
  /**
   * A provider of values.
   */

  public ARI1ProviderEvent()
  {

  }

  @Override
  public boolean canProvideFor(
    final TypeUsage targetType)
  {
    return targetType.isOfType(ARI1EventType.class);
  }

  @Override
  public Set<Arbitrary<?>> provideFor(
    final TypeUsage targetType,
    final SubtypeProvider subtypeProvider)
  {
    return Set.of(
      eventNoteOff(),
      eventNoteOn(),
      eventNotePitchBend(),
      eventConfigurationParameterChanged()
    );
  }

  private static Arbitrary<ARI1EventNoteOff> eventNoteOff()
  {
    final var time =
      Arbitraries.integers()
        .between(0, 1_000_000);
    final var note =
      Arbitraries.integers()
        .between(0, 127);
    final var velocity =
      Arbitraries.doubles()
        .between(0.0, 1.0);

    return Combinators.combine(time, note, velocity)
      .as(ARI1EventNoteOff::new);
  }

  private static Arbitrary<ARI1EventNoteOn> eventNoteOn()
  {
    final var time =
      Arbitraries.integers()
        .between(0, 1_000_000);
    final var note =
      Arbitraries.integers()
        .between(0, 127);
    final var velocity =
      Arbitraries.doubles()
        .between(0.0, 1.0);

    return Combinators.combine(time, note, velocity)
      .as(ARI1EventNoteOn::new);
  }

  private static Arbitrary<ARI1EventNotePitchBend> eventNotePitchBend()
  {
    final var time =
      Arbitraries.integers()
        .between(0, 1_000_000);
    final var bend =
      Arbitraries.doubles()
        .between(0.0, 1.0);

    return Combinators.combine(time, bend)
      .as(ARI1EventNotePitchBend::new);
  }

  private static Arbitrary<ARI1EventConfigurationParameterChanged> eventConfigurationParameterChanged()
  {
    final var time =
      Arbitraries.integers()
        .between(0, 1_000_000);
    final var bend =
      Arbitraries.defaultFor(ARI1ParameterId.class);

    return Combinators.combine(time, bend)
      .as(ARI1EventConfigurationParameterChanged::new);
  }
}
