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

package com.io7m.aradine.tests.filter.recursive1;

import com.io7m.aradine.filter.recursive1.ARF1HPFOnePole;
import com.io7m.aradine.tests.ARTestDirectories;
import com.io7m.aradine.tests.ARTestFrequencyAnalysis;
import com.io7m.jsamplebuffer.api.SampleBufferType;
import com.io7m.jsamplebuffer.vanilla.SampleBufferDouble;
import com.io7m.jsamplebuffer.xmedia.SXMSampleBuffers;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.DoubleRange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioSystem;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.DoubleBuffer;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class ARF1HPFOnePoleTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ARF1HPFOnePoleTest.class);

  private SampleBufferType noise;
  private SampleBufferType output;
  private DoubleBuffer noiseBuffer;
  private DoubleBuffer outputBuffer;
  private Path directory;
  private Path outputFile;

  @BeforeEach
  public void setup()
    throws Exception
  {
    this.directory =
      ARTestDirectories.createTempDirectory();
    this.outputFile =
      this.directory.resolve("output.wav");

    this.noise =
      sampleResource("white_noise_1.wav");
    this.noiseBuffer =
      DoubleBuffer.allocate((int) this.noise.frames());

    for (int index = 0; index < this.noise.frames(); ++index) {
      this.noiseBuffer.put(index, this.noise.frameGetExact(index));
    }

    /*
     * Filter introduces one frame of latency, so allocate one extra frame.
     */

    this.output =
      SampleBufferDouble.createWithHeapBuffer(
        1,
        this.noise.frames() + 1L,
        this.noise.sampleRate()
      );

    this.outputBuffer =
      DoubleBuffer.allocate((int) this.output.frames());
  }

  @AfterEach
  public void tearDown()
    throws IOException
  {
    ARTestDirectories.deleteDirectory(this.directory);
  }

  /**
   * HPF at 0.0.
   *
   * @throws Exception On errors
   */

  @Test
  public void testFilter0_0()
    throws Exception
  {
    final var filter = new ARF1HPFOnePole();
    filter.setCutoff(0.0);

    for (int index = 0; index < this.noise.frames(); ++index) {
      filter.processOneFrameBuffers(index, this.noiseBuffer, this.outputBuffer);
    }

    final var receivedStats =
      ARTestFrequencyAnalysis.calculateFrequencyContent(
        this.outputBuffer,
        this.noise.sampleRate()
      );
    final var expectedStats =
      ARTestFrequencyAnalysis.loadFrequencyAnalysis(
        "filter/recursive1/hpf1_cutoff_0.properties",
        this.noise.sampleRate()
      );

    checkFrequencyContent(receivedStats, expectedStats);
  }

  /**
   * HPF at 0.03125.
   *
   * @throws Exception On errors
   */

  @Test
  public void testFilter0_03125()
    throws Exception
  {
    final var filter = new ARF1HPFOnePole();
    filter.setCutoff(0.03125);

    for (int index = 0; index < this.noise.frames(); ++index) {
      filter.processOneFrameBuffers(index, this.noiseBuffer, this.outputBuffer);
    }

    final var receivedStats =
      ARTestFrequencyAnalysis.calculateFrequencyContent(
        this.outputBuffer,
        this.noise.sampleRate()
      );
    final var expectedStats =
      ARTestFrequencyAnalysis.loadFrequencyAnalysis(
        "filter/recursive1/hpf1_cutoff_03125.properties",
        this.noise.sampleRate()
      );

    checkFrequencyContent(receivedStats, expectedStats);
  }

  /**
   * HPF at 0.125.
   *
   * @throws Exception On errors
   */

  @Test
  public void testFilter0_125()
    throws Exception
  {
    final var filter = new ARF1HPFOnePole();
    filter.setCutoff(0.125);

    for (int index = 0; index < this.noise.frames(); ++index) {
      filter.processOneFrameBuffers(index, this.noiseBuffer, this.outputBuffer);
    }

    final var receivedStats =
      ARTestFrequencyAnalysis.calculateFrequencyContent(
        this.outputBuffer,
        this.noise.sampleRate()
      );
    final var expectedStats =
      ARTestFrequencyAnalysis.loadFrequencyAnalysis(
        "filter/recursive1/hpf1_cutoff_0125.properties",
        this.noise.sampleRate()
      );

    checkFrequencyContent(receivedStats, expectedStats);
  }

  /**
   * HPF at 0.25.
   *
   * @throws Exception On errors
   */

  @Test
  public void testFilter0_25()
    throws Exception
  {
    final var filter = new ARF1HPFOnePole();
    filter.setCutoff(0.25);

    for (int index = 0; index < this.noise.frames(); ++index) {
      filter.processOneFrameBuffers(index, this.noiseBuffer, this.outputBuffer);
    }

    final var receivedStats =
      ARTestFrequencyAnalysis.calculateFrequencyContent(
        this.outputBuffer,
        this.noise.sampleRate()
      );
    final var expectedStats =
      ARTestFrequencyAnalysis.loadFrequencyAnalysis(
        "filter/recursive1/hpf1_cutoff_025.properties",
        this.noise.sampleRate()
      );

    checkFrequencyContent(receivedStats, expectedStats);
  }

  /**
   * HPF at 0.5.
   *
   * @throws Exception On errors
   */

  @Test
  public void testFilter0_5()
    throws Exception
  {
    final var filter = new ARF1HPFOnePole();
    filter.setCutoff(0.5);

    for (int index = 0; index < this.noise.frames(); ++index) {
      filter.processOneFrameBuffers(index, this.noiseBuffer, this.outputBuffer);
    }

    final var receivedStats =
      ARTestFrequencyAnalysis.calculateFrequencyContent(
        this.outputBuffer,
        this.noise.sampleRate()
      );
    final var expectedStats =
      ARTestFrequencyAnalysis.loadFrequencyAnalysis(
        "filter/recursive1/hpf1_cutoff_05.properties",
        this.noise.sampleRate()
      );

    checkFrequencyContent(receivedStats, expectedStats);
  }

  /**
   * HPF at 0.75.
   *
   * @throws Exception On errors
   */

  @Test
  public void testFilter0_75()
    throws Exception
  {
    final var filter = new ARF1HPFOnePole();
    filter.setCutoff(0.75);

    for (int index = 0; index < this.noise.frames(); ++index) {
      filter.processOneFrameBuffers(index, this.noiseBuffer, this.outputBuffer);
    }

    final var receivedStats =
      ARTestFrequencyAnalysis.calculateFrequencyContent(
        this.outputBuffer,
        this.noise.sampleRate()
      );
    final var expectedStats =
      ARTestFrequencyAnalysis.loadFrequencyAnalysis(
        "filter/recursive1/hpf1_cutoff_075.properties",
        this.noise.sampleRate()
      );

    checkFrequencyContent(receivedStats, expectedStats);
  }

  /**
   * HPF at 1.0.
   *
   * @throws Exception On errors
   */

  @Test
  public void testFilter1()
    throws Exception
  {
    final var filter = new ARF1HPFOnePole();
    filter.setCutoff(1.0);

    for (int index = 0; index < this.noise.frames(); ++index) {
      filter.processOneFrameBuffers(index, this.noiseBuffer, this.outputBuffer);
    }

    final var receivedStats =
      ARTestFrequencyAnalysis.calculateFrequencyContent(
        this.outputBuffer,
        this.noise.sampleRate()
      );
    final var expectedStats =
      ARTestFrequencyAnalysis.loadFrequencyAnalysis(
        "filter/recursive1/hpf1_cutoff_1.properties",
        this.noise.sampleRate()
      );

    checkFrequencyContent(receivedStats, expectedStats);
  }

  /**
   * The HPF at zero cutoff is an identity operation.
   *
   * @throws Exception On errors
   */

  @Test
  public void testFilterIdentity()
    throws Exception
  {
    final var filter = new ARF1HPFOnePole();
    filter.setCutoff(0.0);

    for (int index = 0; index < this.noise.frames(); ++index) {
      filter.processOneFrameBuffers(index, this.noiseBuffer, this.outputBuffer);
    }

    for (int index = 1; index < this.noiseBuffer.capacity(); ++index) {
      assertEquals(
        this.noiseBuffer.get(index),
        this.outputBuffer.get(index)
      );
    }

    final var receivedStats =
      ARTestFrequencyAnalysis.calculateFrequencyContent(
        this.outputBuffer,
        this.noise.sampleRate()
      );
    final var expectedStats =
      ARTestFrequencyAnalysis.loadFrequencyAnalysis(
        "filter/recursive1/hpf1_cutoff_0.properties",
        this.noise.sampleRate()
      );

    checkFrequencyContent(receivedStats, expectedStats);
  }

  /**
   * A filter that processes in blocks works the same as one that processes
   * entire files.
   *
   * @throws Exception On errors
   */

  @Property
  public void testFilterChunkedSame(
    final @ForAll @DoubleRange(min = 0.0, max = 1.0) double cutoff)
    throws Exception
  {
    final var pNoise =
      sampleResource("white_noise_1.wav");
    final var pNoiseBuffer =
      DoubleBuffer.allocate((int) pNoise.frames());

    for (int index = 0; index < pNoise.frames(); ++index) {
      pNoiseBuffer.put(index, pNoise.frameGetExact(index));
    }

    final var filterForAll = new ARF1HPFOnePole();
    filterForAll.setCutoff(cutoff);

    final var filterForBlocks = new ARF1HPFOnePole();
    filterForBlocks.setCutoff(cutoff);

    final var outputBuffer1 =
      DoubleBuffer.allocate((int) pNoise.frames());
    final var outputBuffer2 =
      DoubleBuffer.allocate((int) pNoise.frames());

    for (int index = 0; index < pNoise.frames(); ++index) {
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

    for (int index = 0; index < pNoise.frames(); ++index) {
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

  private static SampleBufferType sampleResource(
    final String name)
    throws Exception
  {
    final SampleBufferType sampleBuffer;
    try (var stream = ARF1HPFOnePoleTest.class.getResourceAsStream(
      "/com/io7m/aradine/tests/" + name)) {
      try (var buffered = new BufferedInputStream(stream)) {
        try (var audio = AudioSystem.getAudioInputStream(buffered)) {
          sampleBuffer = SXMSampleBuffers.readSampleBufferFromStream(
            audio, SampleBufferDouble::createWithHeapBuffer
          );
        }
      }
    }
    return sampleBuffer;
  }
}
