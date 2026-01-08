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
import com.io7m.aradine.instrument.spi1.ARI1ParameterRealType;
import com.io7m.aradine.tests.arbitraries.ARI1ValueChangedReal;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.DoubleRange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class ARI1ParameterRealContract<T extends ARI1ParameterRealType>
{
  protected abstract T createParameter(
    ARI1ParameterNumber id,
    double valueMinimum,
    double valueMaximum,
    double valueDefault
  );

  protected abstract void setValue(
    T parameter,
    int time,
    double value
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
    @ForAll final double valueDefault,
    @ForAll final Map<Integer, ARI1ValueChangedReal> updates)
  {
    final var param =
      this.createParameter(
        id,
        -Double.MAX_VALUE,
        Double.MAX_VALUE,
        valueDefault);

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
    @ForAll final double valueDefault,
    @ForAll final double valueA,
    @ForAll final double valueB,
    @ForAll final double valueC)
  {
    final var param =
      this.createParameter(
        id,
        -Double.MAX_VALUE,
        Double.MAX_VALUE,
        valueDefault);

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
    @ForAll @DoubleRange(min = 0L, max = 10L) final double valueMin,
    @ForAll @DoubleRange(min = 100L, max = 200L) final double valueMax,
    @ForAll @DoubleRange(min = 20L, max = 80L) final double valueDefault)
  {
    final var param =
      this.createParameter(
        id,
        valueMin,
        valueMax,
        valueDefault
      );
    assertEquals(id, param.id());
    assertNotNull(param.label());
    assertEquals(valueMin, param.valueMinimum());
    assertEquals(valueMax, param.valueMaximum());
  }

  @Test
  public void testRange0()
  {
    Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> {
        this.createParameter(
          new ARI1ParameterNumber(0),
          100,
          99,
          100
        );
      }
    );
  }

  @Test
  public void testRange1()
  {
    Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> {
        this.createParameter(
          new ARI1ParameterNumber(0),
          5,
          100,
          3
        );
      }
    );
  }

  @Test
  public void testRange2()
  {
    Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> {
        this.createParameter(
          new ARI1ParameterNumber(0),
          5,
          100,
          101
        );
      }
    );
  }
}
