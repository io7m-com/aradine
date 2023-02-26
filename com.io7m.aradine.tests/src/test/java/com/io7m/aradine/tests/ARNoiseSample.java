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

import com.io7m.aradine.tests.filter.recursive1.ARF1HPFOnePoleTest;
import com.io7m.jsamplebuffer.api.SampleBufferReadableType;
import com.io7m.jsamplebuffer.api.SampleBufferType;
import com.io7m.jsamplebuffer.vanilla.SampleBufferDouble;
import com.io7m.jsamplebuffer.xmedia.SXMSampleBuffers;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioSystem;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.nio.DoubleBuffer;
import java.nio.file.Path;

public final class ARNoiseSample
  implements Closeable, ExtensionContext.Store.CloseableResource
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ARNoiseSample.class);

  private final Path directory;
  private final SampleBufferReadableType noise;
  private final DoubleBuffer noiseBuffer;
  private final DoubleBuffer outputBuffer;

  public ARNoiseSample()
    throws Exception
  {
    this.directory =
      ARTestDirectories.createTempDirectory();

    this.noise =
      sampleResource("white_noise_1.wav");
    this.noiseBuffer =
      DoubleBuffer.allocate((int) this.noise.frames());

    for (int index = 0; index < this.noise.frames(); ++index) {
      this.noiseBuffer.put(index, this.noise.frameGetExact(index));
    }

    this.outputBuffer =
      DoubleBuffer.allocate((int) this.noise.frames());

    LOG.debug("created {}", this.directory);
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

  public Path directory()
  {
    return this.directory;
  }

  public SampleBufferReadableType noise()
  {
    return this.noise;
  }

  public DoubleBuffer noiseBuffer()
  {
    return this.noiseBuffer;
  }

  public DoubleBuffer outputBuffer()
  {
    return this.outputBuffer;
  }

  @Override
  public void close()
    throws IOException
  {
    ARTestDirectories.deleteDirectory(this.directory);
    LOG.debug("closed {}", this.directory);
  }
}
