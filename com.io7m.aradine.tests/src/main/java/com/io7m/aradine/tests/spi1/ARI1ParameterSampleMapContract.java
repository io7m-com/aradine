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

import com.io7m.aradine.instrument.spi1.ARI1ParameterNumber;
import com.io7m.aradine.instrument.spi1.ARI1ParameterSampleMapType;
import com.io7m.aradine.instrument.spi1.ARI1SampleMapInstanceID;
import com.io7m.aradine.tests.arbitraries.ARI1ValueChangedSampleMap;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class ARI1ParameterSampleMapContract<T extends ARI1ParameterSampleMapType>
{
  protected abstract T createParameter(
    ARI1ParameterNumber id,
    ARI1SampleMapInstanceID valueDefault
  );

  protected abstract void setValue(
    T parameter,
    int time,
    ARI1SampleMapInstanceID value
  );

  protected abstract void clearChanges(
    T parameter
  );

  /**
   * The value change with the latest time wins.
   */

  @Property
  public void testEventLastWins(
    @ForAll final ARI1ParameterNumber id,
    @ForAll final ARI1SampleMapInstanceID valueDefault,
    @ForAll final Map<Integer, ARI1ValueChangedSampleMap> updates)
  {
    final var param =
      this.createParameter(id, valueDefault);

    final var sorted = new ArrayList<>(updates.values());
    sorted.sort((o1, o2) -> Integer.compareUnsigned(o1.time(), o2.time()));

    for (final var update : sorted) {
      this.setValue(param, update.time(), update.value());
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
      assertEquals(update.value(), param.value(update.time()));
    }

    final var last = sorted.get(sorted.size() - 1);
    assertEquals(last.value(), param.value(last.time() + 100));

    /*
     * After clearing, the value is still the last update value.
     */

    this.clearChanges(param);
    assertEquals(last.value(), param.value(0));
    assertEquals(last.value(), param.value(1_000));
    assertEquals(last.value(), param.value(1_000_000));
  }

  /**
   * The last value change occurring on the same frame wins.
   */

  @Property
  public void testEventsSameTime(
    @ForAll final ARI1ParameterNumber id,
    @ForAll final ARI1SampleMapInstanceID valueDefault,
    @ForAll final ARI1SampleMapInstanceID valueA,
    @ForAll final ARI1SampleMapInstanceID valueB,
    @ForAll final ARI1SampleMapInstanceID valueC)
  {
    final var param =
      this.createParameter(id, valueDefault);

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

  /**
   * Property checks.
   */

  @Property
  public void testID(
    @ForAll final ARI1ParameterNumber id,
    @ForAll final ARI1SampleMapInstanceID valueDefault)
  {
    final var param = this.createParameter(id, valueDefault);
    assertEquals(id, param.id());
    assertNotNull(param.label());
  }
}
