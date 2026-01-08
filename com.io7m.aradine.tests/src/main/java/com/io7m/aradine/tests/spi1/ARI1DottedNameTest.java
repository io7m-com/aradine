/*
 * Copyright © 2026 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

import com.io7m.aradine.instrument.spi1.ARI1DottedName;
import com.io7m.lanark.core.RDottedName;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class ARI1DottedNameTest
{
  @Property
  public void testCompare(
    final @ForAll RDottedName name0,
    final @ForAll RDottedName name1)
  {
    assertEquals(
      name0.compareTo(name1),
      new ARI1DottedName(name0.value())
        .compareTo(new ARI1DottedName(name1.value()))
    );
  }

  @Property
  public void testDottedNameCompatible(
    final @ForAll RDottedName name)
  {
    Assertions.assertEquals(
      name,
      new RDottedName(new ARI1DottedName(name.toString()).toString())
    );
  }

  @TestFactory
  public Stream<DynamicTest> testInvalid()
  {
    return Stream.of(
        " ",
        "a.b.c.d.e.f.g.h.i.a.b.c.d.e.f.g.h.i")
      .map(s -> {
        return DynamicTest.dynamicTest("testInvalid_" + s, () -> {
          assertThrows(IllegalArgumentException.class, () -> {
            new ARI1DottedName(s);
          });
        });
      });
  }
}
