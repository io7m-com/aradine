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

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.nio.DoubleBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class ARSV1StatevarMultiDemo
{
  private ARSV1StatevarMultiDemo()
  {

  }

  public static void main(
    final String[] args)
    throws Exception
  {
    final var tag =
      "svhpf";
    final var title =
      "State Variable High Pass (White noise input, Q = 1, S = 1)";
    final Supplier<ARSV1Filter> filter =
      ARSV1Filter::new;
    final var q = 1.0;
    final var sampleCount = 1;

    final SampleBufferType sampleBuffer;
    try (var stream = ARSV1StatevarMultiDemo.class.getResourceAsStream(
      "/com/io7m/aradine/tests/white_noise_1.wav")) {
      try (var audio = AudioSystem.getAudioInputStream(stream)) {
        sampleBuffer = SXMSampleBuffers.readSampleBufferFromStream(
          audio, SampleBufferDouble::createWithHeapBuffer
        );
      }
    }

    final var cutoffs =
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

    final var width = 1200;
    final var height = 1000;

    final var chart =
      new XYChartBuilder()
        .title(title)
        .xAxisTitle("Frequency (hz)")
        .yAxisTitle("Mean Amplitude")
        .width(width)
        .height(height)
        .build();

    final var font =
      Font.decode("Terminus (TTF) Bold 13");

    final var styler = chart.getStyler();
    styler.setAntiAlias(false);
    styler.setBaseFont(font);
    styler.setCursorFont(font);
    styler.setAnnotationTextFont(font);
    styler.setAxisTitleFont(font);
    styler.setChartTitleFont(font);
    styler.setAxisTickLabelsFont(font);
    styler.setToolTipFont(font);
    styler.setLegendFont(font);
    styler.setXAxisDecimalPattern(".0");

    styler.setChartBackgroundColor(Color.WHITE);

    styler.setXAxisMin(Double.valueOf(100.0));
    styler.setXAxisMax(Double.valueOf(24000.0));
    styler.setXAxisLogarithmic(true);

    styler.setYAxisMax(Double.valueOf(1.0));
    styler.setYAxisMin(Double.valueOf(0.0));
    styler.setYAxisLogarithmic(false);

    for (final var c : cutoffs) {
      final var f = filter.get();
      f.setCutoff(c.doubleValue());
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

      final var freqStats =
        ARTestFrequencyAnalysis.calculateFrequencyContent(
          output,
          sampleBuffer.sampleRate()
        );

      final var xData = new ArrayList<Double>();
      final var yMean = new ArrayList<Double>();
      final var yStdd = new ArrayList<Double>();

      for (final var entry : freqStats.stats().entrySet()) {
        final var stat = entry.getValue();
        xData.add(Double.valueOf(stat.frequencyBand()));
        yMean.add(Double.valueOf(stat.meanAmplitude()));
        yStdd.add(Double.valueOf(stat.standardDeviation()));
      }

      final var s = chart.addSeries("Cutoff (%f)".formatted(c), xData, yMean);
      // s.setMarker(SeriesMarkers.NONE);

      final var r = Math.min(
        1.0,
        Math.max(0.0, Math.sqrt(c.floatValue() * 2.0))
      );

      final var color = new Color((float) r, 0.0f, 0.0f);
      s.setMarkerColor(color);
      s.setLineColor(color);

      freqStats.save(
        "/tmp/%s_q%.1fs%d_cutoff_%s.properties"
          .formatted(
            tag,
            Double.valueOf(q),
            Integer.valueOf(sampleCount),
            c)
      );
    }

    final var image =
      new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    final var graphics =
      image.createGraphics();

    final var file =
      Paths.get("/tmp/chart.png");

    try {
      chart.paint(graphics, width, height);
      ImageIO.write(image, "PNG", file.toFile());
    } finally {
      graphics.dispose();
    }
  }
}
