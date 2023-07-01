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


package com.io7m.aradine.tests.envelope.table;

import com.io7m.aradine.envelope.table1.AREnvelopeInterpolation;
import com.io7m.aradine.envelope.table1.AREnvelopeTable;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.markers.None;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.DoubleBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public final class AREnvelopeCharts
{
  private AREnvelopeCharts()
  {

  }

  public static void main(
    final String[] args)
    throws IOException
  {
    final var data = DoubleBuffer.allocate(1000);

    {
      for (final var value : AREnvelopeInterpolation.values()) {
        final var env0 = AREnvelopeTable.create(1000L);
        env0.setFirst(0.0, value);
        env0.setPoint(1000.0, 1.0, value);

        for (long f = 0L; f < 1000L; ++f) {
          data.put((int) f, env0.evaluate(f));
        }

        chart(data, value, Paths.get("/tmp/chart_%s.png".formatted(value).toLowerCase()));
      }
    }
  }

  private static void chart(
    final DoubleBuffer data,
    final AREnvelopeInterpolation interpolation,
    final Path file)
    throws IOException
  {
    final var width = 640;
    final var height = 480;

    final var chart =
      new XYChartBuilder()
        .title(interpolation.name())
        .xAxisTitle("Frames")
        .yAxisTitle("Amplitude")
        .width(width)
        .height(height)
        .build();

    final var xData = new ArrayList<Long>();
    final var yData = new ArrayList<Double>();

    for (int index = 0; index < data.capacity(); ++index) {
      xData.add(Long.valueOf(index));
      yData.add(Double.valueOf(data.get(index)));
    }

    final var s0 = chart.addSeries("Amplitude", xData, yData);
    s0.setMarker(new None());

    final var font =
      Font.decode("Terminus (TTF) Bold 16");

    final var styler = chart.getStyler();
    styler.setChartTitleVisible(true);
    styler.setAnnotationTextFont(font);
    styler.setAntiAlias(false);
    styler.setAxisTickLabelsFont(font);
    styler.setAxisTitleFont(font);
    styler.setBaseFont(font);
    styler.setChartBackgroundColor(Color.WHITE);
    styler.setChartTitleFont(font);
    styler.setCursorFont(font);
    styler.setLegendFont(font);
    styler.setToolTipFont(font);
    styler.setXAxisMin(0.0);
    styler.setXAxisMax(1000.0);
    styler.setXAxisTickLabelsColor(Color.WHITE);
    styler.setYAxisLogarithmic(false);
    styler.setYAxisMax(1.0);
    styler.setYAxisMin(0.0);
    styler.setLegendVisible(false);

    final var image =
      new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    final var graphics =
      image.createGraphics();

    try {
      chart.paint(graphics, width, height);
      ImageIO.write(image, "PNG", file.toFile());
    } finally {
      graphics.dispose();
    }
  }
}
