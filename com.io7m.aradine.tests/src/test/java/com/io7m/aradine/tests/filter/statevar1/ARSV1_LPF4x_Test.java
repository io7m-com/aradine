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


package com.io7m.aradine.tests.filter.statevar1;

import com.io7m.aradine.filter.statevar1.ARSV1Filter4x;
import com.io7m.aradine.tests.ARNoiseSample;
import com.io7m.aradine.tests.ARTestFrequencyAnalysis;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class ARSV1_LPF4x_Test
{
  record ARFilterTestCase(
    ARNoiseSample sample,
    double q,
    double cutoff)
    implements Closeable
  {
    @Override
    public void close()
      throws IOException
    {
      this.sample.close();
    }
  }

  private static Stream<ARFilterTestCase> filterTestCases()
    throws Exception
  {
    final var cutoffValues =
      List.of(
        Double.valueOf(0.0),
        Double.valueOf(0.00390625),
        Double.valueOf(0.0078125),
        Double.valueOf(0.015625),
        Double.valueOf(0.03125),
        Double.valueOf(0.125),
        Double.valueOf(0.25),
        Double.valueOf(0.5),
        Double.valueOf(0.75),
        Double.valueOf(1.0)
      );

    final var qValues =
      List.of(
        Double.valueOf(0.7),
        Double.valueOf(0.8),
        Double.valueOf(0.9),
        Double.valueOf(1.0),
        Double.valueOf(1.1),
        Double.valueOf(1.2)
      );

    return cutoffValues.stream()
      .flatMap(c -> {
        return qValues.stream()
          .map(q -> {
            try {
              return new ARFilterTestCase(
                new ARNoiseSample(),
                q.doubleValue(),
                c.doubleValue()
              );
            } catch (final Exception e) {
              throw new IllegalStateException(e);
            }
          });
      });
  }

  /**
   * Evaluate all test cases.
   *
   * @throws Exception On errors
   */

  @TestFactory
  public Stream<DynamicTest> testFilterCases()
    throws Exception
  {
    return filterTestCases()
      .map(c -> {
        return DynamicTest.dynamicTest(
          String.format(
            "testFilterCase LPF4x Q %.2f C %f",
            Double.valueOf(c.q),
            Double.valueOf(c.cutoff)
          ),
          () -> {
            try {
              final var filter = new ARSV1Filter4x();
              filter.setCutoff(c.cutoff);
              filter.setQ(c.q);

              assertEquals(c.q, filter.q());
              assertEquals(c.cutoff, filter.cutoff());

              runFilter(c, filter);

              filter.reset();
            } finally {
              c.close();
            }
          });
      });
  }

  private static void runFilter(
    final ARFilterTestCase c,
    final ARSV1Filter4x filter)
    throws Exception
  {
    final var name =
      "%s_q%.1f_cutoff_%s.properties"
        .formatted(
          "svlpf4x",
          Double.valueOf(c.q),
          Double.valueOf(c.cutoff)
        );

    final var path =
      "filter/statevar1/" + name;

    final var inputSample = c.sample.noise();
    final var outputBuffer = c.sample.outputBuffer();

    for (int index = 0; index < inputSample.frames(); ++index) {
      final var x = inputSample.frameGetExact(index);
      filter.processOneFrame(x);
      outputBuffer.put(index, filter.lowPassOutput());
    }

    final var receivedStats =
      ARTestFrequencyAnalysis.calculateFrequencyContent(
        outputBuffer,
        inputSample.sampleRate()
      );

    // receivedStats.save("/tmp/" + name);
    // receivedStats.chart("/tmp/chart.png");

    final var expectedStats =
      ARTestFrequencyAnalysis.loadFrequencyAnalysis(
        path,
        inputSample.sampleRate()
      );

    expectedStats.checkReceivedFrequencyContentAgainstThis(receivedStats);
  }
}
