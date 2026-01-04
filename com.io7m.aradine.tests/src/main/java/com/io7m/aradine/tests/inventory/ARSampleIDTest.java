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

package com.io7m.aradine.tests.inventory;

import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.sample_map.ARSampleMapID;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class ARSampleIDTest
{
  @Property
  public void testCompare(
    final @ForAll ARSampleMapID id)
  {
    assertEquals(
      0,
      id.compareTo(id)
    );
  }

  @Property
  public void testParseToString(
    final @ForAll ARSampleMapID id)
    throws ARException
  {
    assertEquals(
      id,
      ARSampleMapID.parse(id.toString())
    );
  }

  @TestFactory
  public Stream<DynamicTest> testParseBad()
  {
    return Stream.of(
      "",
      "com.io7m:com.io7m:x",
      "com.io7m:com.io7m:1.0.0-%"
    ).map(ARSampleIDTest::testParseBadOf);
  }

  private static DynamicTest testParseBadOf(
    final String text)
  {
    return DynamicTest.dynamicTest(
      "testParseBadOf_%s".formatted(text),
      () -> {
        final var ex =
          assertThrows(ARException.class, () -> ARSampleMapID.parse(text));
        assertEquals("error-parse", ex.errorCode());
      });
  }
}
