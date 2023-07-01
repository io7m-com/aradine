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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ARI1RNGDeterministicTest
{
  private Path directory;

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.directory = ARTestDirectories.createTempDirectory();
  }

  @AfterEach
  public void tearDown()
    throws IOException
  {
    ARTestDirectories.deleteDirectory(this.directory);
  }

  @Test
  public void testRNG()
  {
    final var rng =
      new ARI1RNGDeterministic(0x494F376D);

    final var values0 = new ArrayList<Double>();
    for (int index = 0; index < 1000; ++index) {
      values0.add(Double.valueOf(rng.random()));
    }

    rng.reset();

    final var values1 = new ArrayList<Double>();
    for (int index = 0; index < 1000; ++index) {
      values1.add(Double.valueOf(rng.random()));
    }

    assertEquals(values0, values1);
  }

  @Test
  public void testRNGAnalyze()
    throws Exception
  {
    final var rng =
      new ARI1RNGDeterministic(0x494F376D);

    final var buffer =
      DoubleBuffer.allocate(44100);

    try (var writer = Files.newBufferedWriter(this.directory.resolve("data.txt"))) {
      for (int index = 0; index < 44100; ++index) {
        // Convert to a signed [-1.0, 1.0] range for frequency analysis.
        final var x = (rng.random() * 2.0) - 1.0;
        writer.write(Double.toString(x));
        writer.newLine();
        buffer.put(index, x);
      }
      writer.flush();
    }

    final var analysis =
      ARTestFrequencyAnalysis.calculateFrequencyContent(buffer, 44100.0);

    for (final var entry : analysis.stats().entrySet()) {
      final var bandStats = entry.getValue();
      final var mean = bandStats.meanAmplitude();
      final var freq = bandStats.frequencyBand();
      assertTrue(
        mean < 0.52,
        String.format(
          "Mean %f for frequency band %f must be < 0.52",
          mean,
          freq)
      );
      assertTrue(
        mean > 0.45,
        String.format(
          "Mean %f for frequency band %f must be > 0.45",
          mean,
          freq)
      );
    }
  }
}
