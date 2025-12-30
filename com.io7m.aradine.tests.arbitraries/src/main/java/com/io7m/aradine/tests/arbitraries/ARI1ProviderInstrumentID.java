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

import com.io7m.aradine.api.instrument.ARInstrumentID;
import com.io7m.lanark.core.RDottedName;
import com.io7m.verona.core.Version;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;

import java.util.Set;

/**
 * A provider of values.
 */

public final class ARI1ProviderInstrumentID
  implements ArbitraryProvider
{
  /**
   * A provider of values.
   */

  public ARI1ProviderInstrumentID()
  {

  }

  @Override
  public boolean canProvideFor(
    final TypeUsage targetType)
  {
    return targetType.isOfType(ARInstrumentID.class);
  }

  @Override
  public Set<Arbitrary<?>> provideFor(
    final TypeUsage targetType,
    final SubtypeProvider subtypeProvider)
  {
    return Set.of(
      Combinators.combine(
        Arbitraries.integers().greaterOrEqual(0),
        Arbitraries.integers().greaterOrEqual(0),
        Arbitraries.defaultFor(RDottedName.class),
        Arbitraries.defaultFor(RDottedName.class)
      ).as((major, minor, group, name) -> {
        return new ARInstrumentID(
          group,
          name,
          Version.of(major, minor, 0)
        );
      })
    );
  }
}
