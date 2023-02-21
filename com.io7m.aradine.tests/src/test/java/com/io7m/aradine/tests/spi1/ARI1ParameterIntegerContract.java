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


package com.io7m.aradine.tests.spi1;

import com.io7m.aradine.instrument.spi1.ARI1ParameterId;
import com.io7m.aradine.instrument.spi1.ARI1ParameterIntegerType;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class ARI1ParameterIntegerContract<T extends ARI1ParameterIntegerType>
{
  protected abstract T createParameter(
    ARI1ParameterId id,
    long valueMinimum,
    long valueMaximum,
    long valueDefault
  );

  protected abstract void setValue(
    T parameter,
    int time,
    long value
  );

  protected abstract void clearChanges(
    T parameter
  );

  private record Update(int time, long value)
  {
  }

  @Provide(value = "updates")
  public static Arbitrary<Update> updates()
  {
    final var ai =
      Arbitraries.integers()
        .between(0, 100_000_000);

    final var al =
      Arbitraries.longs();

    return ai.flatMap(time -> al.map(v -> {
      return new Update(time.intValue(), v.longValue());
    }));
  }

  @Provide(value = "updateLists")
  public static Arbitrary<List<Update>> updateLists()
  {
    return updates().list();
  }

  @Provide(value = "updateMaps")
  public static Arbitrary<Map<Integer, Update>> updateMaps()
  {
    return updates()
      .list()
      .reduce(new HashMap<>(), (m, update) -> {
        m.put(Integer.valueOf(update.time), update);
        return m;
      });
  }

  /**
   * The value change with the latest time wins.
   *
   * @return The tests
   */

  @Property
  public void testEventLastWins(
    @ForAll final ARI1ParameterId id,
    @ForAll final long valueDefault,
    @ForAll(value = "updateMaps") final Map<Integer, Update> updates)
  {
    final var param =
      this.createParameter(id, Long.MIN_VALUE, Long.MAX_VALUE, valueDefault);

    final var sorted = new ArrayList<>(updates.values());
    sorted.sort((o1, o2) -> Integer.compareUnsigned(o1.time, o2.time));

    for (final var update : sorted) {
      this.setValue(param, update.time, update.value);
    }

    /*
     * If there were no changes, the value is the default.
     */

    if (sorted.isEmpty()) {
      assertEquals(valueDefault, param.value(0));
      assertEquals(valueDefault, param.value(1_000));
      assertEquals(valueDefault, param.value(1_000_000));
      return;
    }

    /*
     * Otherwise, the values are correct at each time.
     */

    for (final var update : sorted) {
      assertEquals(update.value, param.value(update.time));
    }

    final var last = sorted.get(sorted.size() - 1);
    assertEquals(last.value, param.value(last.time + 100));

    /*
     * After clearing, the value is still the last update value.
     */

    this.clearChanges(param);
    assertEquals(last.value, param.value(0));
    assertEquals(last.value, param.value(1_000));
    assertEquals(last.value, param.value(1_000_000));
  }

  /**
   * The last value change occurring on the same frame wins.
   *
   * @return The tests
   */

  @Property
  public void testEventsSameTime(
    @ForAll final ARI1ParameterId id,
    @ForAll final long valueDefault,
    @ForAll final long valueA,
    @ForAll final long valueB,
    @ForAll final long valueC)
  {
    final var param =
      this.createParameter(id, Long.MIN_VALUE, Long.MAX_VALUE, valueDefault);

    this.setValue(param, 1_000, valueA);
    this.setValue(param, 1_000, valueB);
    this.setValue(param, 1_000, valueC);

    for (int index = 0; index < 1000; ++index) {
      assertEquals(valueDefault, param.value(index));
    }

    assertEquals(valueC, param.value(1_000));

    this.clearChanges(param);

    for (int index = 0; index <= 2000; ++index) {
      assertEquals(valueC, param.value(index));
    }
  }
}
