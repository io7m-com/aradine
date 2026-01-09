/*
 * Copyright © 2026 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

import org.knowm.xchart.XYChartBuilder;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

public final class AROutputCharting
{
  private AROutputCharting()
  {

  }

  public static void chart(
    final double[] dataL,
    final double[] dataR,
    final Path output)
    throws IOException
  {
    final var width = 1200;
    final var height = 1000;

    final var chart =
      new XYChartBuilder()
        .xAxisTitle("Time")
        .yAxisTitle("Amplitude")
        .width(width)
        .height(height)
        .build();

    final var s0 =
      chart.addSeries("L", dataL);
    final var s1 =
      chart.addSeries("R", dataR);

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

    styler.setXAxisMin(Double.valueOf(0.0));
    styler.setXAxisMax(Double.valueOf((double) dataL.length));
    styler.setXAxisLogarithmic(false);

    styler.setYAxisMax(2.0);
    styler.setYAxisMin(-2.0);
    styler.setYAxisLogarithmic(false);

    final var image =
      new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    final var graphics =
      image.createGraphics();

    try {
      chart.paint(graphics, width, height);
      ImageIO.write(image, "PNG", output.toFile());
    } finally {
      graphics.dispose();
    }
  }
}
