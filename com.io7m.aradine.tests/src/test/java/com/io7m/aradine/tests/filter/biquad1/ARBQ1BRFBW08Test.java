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

package com.io7m.aradine.tests.filter.biquad1;

import com.io7m.aradine.filter.biquad1.ARBQ1BiquadBRFBWO8;
import com.io7m.aradine.tests.ARNoiseSample;
import com.io7m.aradine.tests.ARNoiseSampleFixture;
import com.io7m.aradine.tests.ARTestFrequencyAnalysis;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.DoubleRange;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.DoubleBuffer;
import java.util.Arrays;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ARNoiseSampleFixture.class)
public final class ARBQ1BRFBW08Test
{
  private static void runFilter(
    final ARNoiseSample sample,
    final ARBQ1BiquadBRFBWO8 filter,
    final String name)
    throws Exception
  {
    final var inputBuffer = sample.noiseBuffer();
    final var inputSample = sample.noise();
    final var outputBuffer = sample.outputBuffer();

    for (int index = 0; index < inputSample.frames(); ++index) {
      filter.processOneFrameBuffers(index, inputBuffer, outputBuffer);
    }

    final var receivedStats =
      ARTestFrequencyAnalysis.calculateFrequencyContent(
        outputBuffer,
        inputSample.sampleRate()
      );
    final var expectedStats =
      ARTestFrequencyAnalysis.loadFrequencyAnalysis(
        name,
        inputSample.sampleRate()
      );

    checkFrequencyContent(receivedStats, expectedStats);
  }

  private static void checkFrequencyContent(
    final ARTestFrequencyAnalysis receivedAnalysis,
    final ARTestFrequencyAnalysis expectedAnalysis)
  {
    final var receivedStats = receivedAnalysis.stats();
    final var expectedStats = expectedAnalysis.stats();

    for (final var expectedEntry : expectedStats.entrySet()) {
      final var expected = expectedEntry.getValue();
      final var received = receivedStats.get(expectedEntry.getKey());
      Objects.requireNonNull(expected, "expected");
      Objects.requireNonNull(received, "received");

      assertEquals(
        expected.meanAmplitude(),
        received.meanAmplitude(),
        0.00000000000001,
        () -> {
          return String.format(
            "Mean amplitude for frequency %d, expected %f received %f",
            expectedEntry.getKey(),
            Double.valueOf(expected.meanAmplitude()),
            Double.valueOf(received.meanAmplitude())
          );
        }
      );

      assertEquals(
        expected.standardDeviation(),
        received.standardDeviation(),
        0.00000000000001,
        () -> {
          return String.format(
            "Standard deviation for frequency %d, expected %f received %f",
            expectedEntry.getKey(),
            Double.valueOf(expected.standardDeviation()),
            Double.valueOf(received.standardDeviation())
          );
        }
      );
    }
  }

  /**
   * BRF at 0.0.
   *
   * @throws Exception On errors
   */

  @Test
  public void testFilter0_0(
    final ARNoiseSample sample)
    throws Exception
  {
    final var filter = new ARBQ1BiquadBRFBWO8();
    filter.setCutoff(0.0);

    runFilter(sample, filter, "filter/biquad1/brf8bw_cutoff_0.0.properties");
  }

  /**
   * BRF at 0.015625.
   *
   * @throws Exception On errors
   */

  @Test
  public void testFilter0_0015625(
    final ARNoiseSample sample)
    throws Exception
  {
    final var filter = new ARBQ1BiquadBRFBWO8();
    filter.setCutoff(0.015625);

    runFilter(sample, filter, "filter/biquad1/brf8bw_cutoff_0.015625.properties");
  }

  /**
   * BRF at 0.0078125.
   *
   * @throws Exception On errors
   */

  @Test
  public void testFilter0_0078125(
    final ARNoiseSample sample)
    throws Exception
  {
    final var filter = new ARBQ1BiquadBRFBWO8();
    filter.setCutoff(0.0078125);

    runFilter(sample, filter, "filter/biquad1/brf8bw_cutoff_0.0078125.properties");
  }

  /**
   * BRF at 0.00390625.
   *
   * @throws Exception On errors
   */

  @Test
  public void testFilter0_00390625(
    final ARNoiseSample sample)
    throws Exception
  {
    final var filter = new ARBQ1BiquadBRFBWO8();
    filter.setCutoff(0.00390625);

    runFilter(sample, filter, "filter/biquad1/brf8bw_cutoff_0.00390625.properties");
  }
  
  /**
   * BRF at 0.03125.
   *
   * @throws Exception On errors
   */

  @Test
  public void testFilter0_03125(
    final ARNoiseSample sample)
    throws Exception
  {
    final var filter = new ARBQ1BiquadBRFBWO8();
    filter.setCutoff(0.03125);

    runFilter(sample, filter, "filter/biquad1/brf8bw_cutoff_0.03125.properties");
  }

  /**
   * BRF at 0.125.
   *
   * @throws Exception On errors
   */

  @Test
  public void testFilter0_125(
    final ARNoiseSample sample)
    throws Exception
  {
    final var filter = new ARBQ1BiquadBRFBWO8();
    filter.setCutoff(0.125);

    runFilter(sample, filter, "filter/biquad1/brf8bw_cutoff_0.125.properties");
  }

  /**
   * BRF at 0.25.
   *
   * @throws Exception On errors
   */

  @Test
  public void testFilter0_25(
    final ARNoiseSample sample)
    throws Exception
  {
    final var filter = new ARBQ1BiquadBRFBWO8();
    filter.setCutoff(0.25);

    runFilter(sample, filter, "filter/biquad1/brf8bw_cutoff_0.25.properties");
  }

  /**
   * BRF at 0.5.
   *
   * @throws Exception On errors
   */

  @Test
  public void testFilter0_5(
    final ARNoiseSample sample)
    throws Exception
  {
    final var filter = new ARBQ1BiquadBRFBWO8();
    filter.setCutoff(0.5);

    runFilter(sample, filter, "filter/biquad1/brf8bw_cutoff_0.5.properties");
  }

  /**
   * A filter that processes in blocks works the same as one that processes
   * entire files.
   *
   * @throws Exception On errors
   */

  @Property(tries = 100)
  public void testFilterChunkedSame(
    final @ForAll @DoubleRange(min = 0.0, max = 1.0) double cutoff)
    throws Exception
  {
    final var noiseSample = new ARNoiseSample();

    try {
      final var inputSample =
        noiseSample.noise();
      final var pNoiseBuffer =
        DoubleBuffer.allocate((int) inputSample.frames());

      for (int index = 0; index < inputSample.frames(); ++index) {
        pNoiseBuffer.put(index, inputSample.frameGetExact(index));
      }

      final var filterForAll = new ARBQ1BiquadBRFBWO8();
      filterForAll.setCutoff(cutoff);

      final var filterForBlocks = new ARBQ1BiquadBRFBWO8();
      filterForBlocks.setCutoff(cutoff);

      final var outputBuffer1 =
        DoubleBuffer.allocate((int) inputSample.frames());
      final var outputBuffer2 =
        DoubleBuffer.allocate((int) inputSample.frames());

      for (int index = 0; index < inputSample.frames(); ++index) {
        filterForAll.processOneFrameBuffers(index, pNoiseBuffer, outputBuffer1);
      }

      final var chunkInputBuffer =
        DoubleBuffer.allocate(256);
      final var chunkOutputBuffer =
        DoubleBuffer.allocate(256);

      var noiseIndex = 0;
      while (true) {
        Arrays.fill(chunkInputBuffer.array(), 0.0);
        Arrays.fill(chunkOutputBuffer.array(), 0.0);

        final var remaining =
          Math.min(256, outputBuffer2.capacity() - noiseIndex);

        for (int index = 0; index < remaining; ++index) {
          chunkInputBuffer.put(index, pNoiseBuffer.get(noiseIndex));

          filterForBlocks.processOneFrameBuffers(
            index,
            chunkInputBuffer,
            chunkOutputBuffer
          );

          outputBuffer2.put(noiseIndex, chunkOutputBuffer.get(index));
          ++noiseIndex;
        }

        if (remaining < 256) {
          break;
        }
      }

      for (int index = 0; index < inputSample.frames(); ++index) {
        final var out1 = outputBuffer1.get(index);
        final var out2 = outputBuffer2.get(index);
        final int finalIndex = index;
        assertEquals(
          out1,
          out2,
          0.0000000000001,
          () -> {
            final var text = new StringBuilder(128);
            text.append(String.format(
              "Output1[%d] (%f) must == Output2[%d] (%f)",
              Integer.valueOf(finalIndex),
              Double.valueOf(out1),
              Integer.valueOf(finalIndex),
              Double.valueOf(out2)
            ));
            text.append(System.lineSeparator());

            final var startContext = Math.max(0, finalIndex - 3);
            final var endContext = finalIndex + 3;
            for (int k = startContext; k < endContext; ++k) {
              text.append(
                String.format(
                  "Output1[%d] = %f   Output2[%d] = %f",
                  Integer.valueOf(k),
                  Double.valueOf(outputBuffer1.get(k)),
                  Integer.valueOf(k),
                  Double.valueOf(outputBuffer2.get(k))
                )
              );
              text.append(System.lineSeparator());
            }

            return text.toString();
          }
        );
      }
    } finally {
      noiseSample.close();
    }
  }
}
