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

import com.io7m.aradine.filter.statevar1.ARSV1Filter;
import com.io7m.aradine.tests.ARTestFrequencyAnalysis;
import com.io7m.jsamplebuffer.api.SampleBufferType;
import com.io7m.jsamplebuffer.vanilla.SampleBufferDouble;
import com.io7m.jsamplebuffer.xmedia.SXMSampleBuffers;
import org.knowm.xchart.XYChartBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.DoubleBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.awt.image.BufferedImage.TYPE_4BYTE_ABGR;

public final class ARSV1StatevarInstabilityDemo
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ARSV1StatevarInstabilityDemo.class);

  private ARSV1StatevarInstabilityDemo()
  {

  }

  public static void main(
    final String[] args)
    throws Exception
  {
    final SampleBufferType sampleBuffer;
    try (var stream = ARSV1StatevarInstabilityDemo.class.getResourceAsStream(
      "/com/io7m/aradine/tests/white_noise_1.wav")) {
      try (var audio = AudioSystem.getAudioInputStream(stream)) {
        sampleBuffer = SXMSampleBuffers.readSampleBufferFromStream(
          audio, SampleBufferDouble::createWithHeapBuffer
        );
      }
    }

    for (int samples = 1; samples <= 5; ++samples) {
      final var image =
        new BufferedImage(800, 800, TYPE_4BYTE_ABGR);
      final var graphics =
        image.createGraphics();

      for (var cutoff = 0.0; cutoff <= 1.0; cutoff += 1.0 / 100.0) {
        for (var q = 0.5; q <= 1.5; q += 1.0 / 100.0) {
          runForOne(sampleBuffer, samples, graphics, cutoff, q);
        }
      }

      ImageIO.write(image, "PNG", new File("/tmp/stable_%d.png".formatted(samples)));
    }
  }

  private static void runForOne(
    final SampleBufferType sampleBuffer,
    final int sampleCount,
    final Graphics2D graphics,
    final double cutoff,
    final double q)
    throws Exception
  {
    final var f = new ARSV1Filter();
    f.setCutoff(cutoff);
    f.setQ(q);

    final var output =
      DoubleBuffer.allocate((int) sampleBuffer.frames());

    for (int index = 0; index < sampleBuffer.frames(); ++index) {
      for (int oversample = 0; oversample < sampleCount; ++oversample) {
        f.processOneFrame(sampleBuffer.frameGetExact(index));
      }
      final var x = f.highPassOutput();
      output.put(index, x);
    }

    LOG.debug("{} {}", Double.valueOf(cutoff), Double.valueOf(q));

    final var freqStats =
      ARTestFrequencyAnalysis.calculateFrequencyContent(
        output,
        sampleBuffer.sampleRate()
      );

    final var xCell =
      Math.round(cutoff * 100.0);
    final var yCell =
      Math.round((q - 0.5) * 100.0);
    final var x =
      (int) (xCell * 8.0);
    final var y =
      (int) (yCell * 8.0);

    for (final var stat : freqStats.stats().values()) {
      if (stat.meanAmplitude() >= 0.6) {
        graphics.setColor(Color.RED);
        graphics.fillRect(x, y, 8, 8);

        graphics.setColor(Color.WHITE);
        graphics.setStroke(new BasicStroke());
        graphics.drawRect(x, y, 8, 8);
        return;
      }
    }

    graphics.setColor(Color.GREEN);
    graphics.fillRect(x, y, 8, 8);

    graphics.setColor(Color.WHITE);
    graphics.setStroke(new BasicStroke());
    graphics.drawRect(x, y, 8, 8);
  }
}
