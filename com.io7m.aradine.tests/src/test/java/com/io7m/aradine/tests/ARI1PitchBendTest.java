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


package com.io7m.aradine.tests;

import com.io7m.aradine.instrument.spi1.ARI1PitchBend;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class ARI1PitchBendTest
{
  /**
   * Test that the expected bend values are calculated for a range of semitone
   * values.
   *
   * @param semitones The semitones
   *
   * @throws Exception On errors
   */

  @ParameterizedTest
  @ValueSource(ints = {12, 24, 48, 120})
  public void testPitchBendForSemitones(
    final int semitones)
    throws Exception
  {
    final var path =
      String.format(
        "/com/io7m/aradine/tests/pitchBend%d.properties",
        Integer.valueOf(semitones)
      );

    final var properties = new Properties();
    try (var stream = ARI1PitchBendTest.class.getResourceAsStream(path)) {
      properties.load(stream);
    }

    for (final var key : properties.keySet()) {
      final var keyS =
        (String) key;
      final var valS =
        properties.getProperty(keyS);
      final var bend =
        Double.parseDouble(keyS);
      final var expectedRate =
        Double.parseDouble(valS);
      final var receivedRate =
        ARI1PitchBend.pitchBendToPlaybackRate(bend, semitones);

      assertEquals(expectedRate, receivedRate, 0.00000001);
    }
  }
}
