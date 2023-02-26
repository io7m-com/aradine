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

import com.io7m.aradine.filter.biquad1.ARBQ1BiquadBPFO2;
import com.io7m.aradine.filter.biquad1.ARBQ1BiquadBPFBWO4;
import com.io7m.aradine.filter.biquad1.ARBQ1BiquadBRFBWO8;
import com.io7m.aradine.filter.biquad1.ARBQ1BiquadBRFO2;
import com.io7m.aradine.filter.biquad1.ARBQ1BiquadType;
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

public final class ARBQ1BiquadMultiDemo
{
  private ARBQ1BiquadMultiDemo()
  {

  }

  public static void main(
    final String[] args)
    throws Exception
  {
    final var tag =
      "brf8bw";
    final var title =
      "Biquad BRF (White noise input)";
    final Supplier<ARBQ1BiquadType> filter =
      ARBQ1BiquadBRFBWO8::new;

    final SampleBufferType sampleBuffer;
    try (var stream = ARBQ1BiquadMultiDemo.class.getResourceAsStream(
      "/com/io7m/aradine/tests/white_noise_1.wav")) {
      try (var audio = AudioSystem.getAudioInputStream(stream)) {
        sampleBuffer = SXMSampleBuffers.readSampleBufferFromStream(
          audio, SampleBufferDouble::createWithHeapBuffer
        );
      }
    }

    final var cutoffs =
      List.of(
        0.0,
        0.00390625,
        0.0078125,
        0.015625,
        0.03125,
        0.125,
        0.25,
        0.5
      );

    final var width = 1280;
    final var height = 800;

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

    styler.setXAxisMin(100.0);
    styler.setXAxisMax(24000.0);
    styler.setXAxisLogarithmic(true);

    styler.setYAxisMax(1.0);
    styler.setYAxisMin(0.0);
    styler.setYAxisLogarithmic(false);

    for (final var c : cutoffs) {
      final var f = filter.get();
      f.setCutoff(c.doubleValue());

      final var output =
        DoubleBuffer.allocate((int) sampleBuffer.frames());

      for (int index = 0; index < sampleBuffer.frames(); ++index) {
        final var x = f.processOneFrame(sampleBuffer.frameGetExact(index));
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
        xData.add(stat.frequencyBand());
        yMean.add(stat.meanAmplitude());
        yStdd.add(stat.standardDeviation());
      }

      final var s = chart.addSeries("Cutoff (%f)".formatted(c), xData, yMean);
      // s.setMarker(SeriesMarkers.NONE);

      final var color =
        new Color(c.floatValue() * 2.0f, 0.0f, 0.0f);
      s.setMarkerColor(color);
      s.setLineColor(color);

      freqStats.save("/tmp/%s_cutoff_%s.properties".formatted(tag, c));
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
