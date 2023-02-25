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

import com.io7m.aradine.filter.recursive1.ARF1LPFOnePole;
import com.io7m.aradine.tests.ARTestFrequencyAnalysis;
import com.io7m.jsamplebuffer.api.SampleBufferType;
import com.io7m.jsamplebuffer.vanilla.SampleBufferDouble;
import com.io7m.jsamplebuffer.xmedia.SXMSampleBuffers;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.markers.SeriesMarkers;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.nio.DoubleBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public final class ARF1LPFOnePoleIRDemo
{
  private ARF1LPFOnePoleIRDemo()
  {

  }

  public static void main(
    final String[] args)
    throws Exception
  {

    final var cutoffs =
      List.of(0.015625, 0.03125, 0.0625, 0.125, 0.25, 0.5, 0.75, 0.875, 1.0);

    final var width = 1280;
    final var height = 800;

    final var chart =
      new XYChartBuilder()
        .title("LPF 1 Pole (Impulse Response)")
        .xAxisTitle("Frame Index")
        .yAxisTitle("Amplitude")
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

    final var sampleCount =
      128;
    final var impulse =
      DoubleBuffer.allocate(sampleCount);

    styler.setXAxisMin(0.0);
    styler.setXAxisMax((double) sampleCount);
    styler.setXAxisLogarithmic(false);

    styler.setYAxisMax(1.0);
    styler.setYAxisMin(0.0);
    styler.setYAxisLogarithmic(false);

    impulse.put(0, 1.0);

    for (final var c : cutoffs) {
      final var f = new ARF1LPFOnePole();
      f.setCutoff(c.doubleValue());

      final var output =
        DoubleBuffer.allocate(impulse.capacity());

      for (int index = 0; index < impulse.capacity(); ++index) {
        final var x = f.processOneFrame(impulse.get(index));
        output.put(index, x);
      }

      final var xData = new ArrayList<Integer>();
      final var yData = new ArrayList<Double>();

      for (int index = 0; index < impulse.capacity(); ++index) {
        xData.add(index);
        yData.add(output.get(index));
      }

      final var s =
      chart.addSeries("Cutoff (%f)".formatted(c), xData, yData);

      s.setMarker(SeriesMarkers.NONE);
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
