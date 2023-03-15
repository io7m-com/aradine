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

import com.io7m.aradine.envelope.table1.AREnvelopeADR;
import com.io7m.aradine.envelope.table1.AREnvelopeADREvaluator;
import com.io7m.aradine.envelope.table1.AREnvelopeTable;
import org.junit.jupiter.api.Test;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.markers.None;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.TreeMap;

import static com.io7m.aradine.envelope.table1.AREnvelopeADRState.STATE_ATTACK;
import static com.io7m.aradine.envelope.table1.AREnvelopeADRState.STATE_RELEASE;
import static com.io7m.aradine.envelope.table1.AREnvelopeADRState.STATE_SUSTAIN;
import static com.io7m.aradine.envelope.table1.AREnvelopeInterpolation.LINEAR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class AREnvelopeADREvaluatorTest
{
  /**
   * The default empty envelope evaluates as a constant 1.0 until the release.
   */

  @Test
  public void testEmpty()
  {
    final var env =
      new AREnvelopeADR(
        AREnvelopeTable.create(48000L),
        AREnvelopeTable.create(48000L),
        AREnvelopeTable.create(48000L)
      );

    final var eval =
      new AREnvelopeADREvaluator(env);

    assertEquals(STATE_ATTACK, eval.state());
    assertEquals(1.0, eval.evaluate(0L));
    assertEquals(STATE_SUSTAIN, eval.state());
    assertEquals(1.0, eval.evaluate(1L));
    assertEquals(STATE_SUSTAIN, eval.state());
    assertEquals(1.0, eval.evaluate(2L));
    assertEquals(STATE_SUSTAIN, eval.state());
    assertEquals(1.0, eval.evaluate(3L));

    eval.beginRelease(4L, false);
    assertEquals(STATE_RELEASE, eval.state());
    assertEquals(0.0, eval.evaluate(4L));
    assertEquals(0.0, eval.evaluate(5L));
    assertEquals(0.0, eval.evaluate(6L));
  }

  /**
   * Exercise a basic envelope.
   *
   * @throws Exception On errors
   */

  @Test
  public void testBasicEnvelope()
    throws Exception
  {
    final var env =
      new AREnvelopeADR(
        AREnvelopeTable.create(48000L),
        AREnvelopeTable.create(48000L),
        AREnvelopeTable.create(48000L)
      );

    {
      env.attack().setFirst(0.0, LINEAR);
      env.attack().setPoint(1000.0, 1.0, LINEAR);
      env.sustain().setFirst(1.0, LINEAR);
      env.sustain().setPoint(60.0, 0.8, LINEAR);
      env.sustain().setPoint(120.0, 1.0, LINEAR);
      env.release().setFirst(1.0, LINEAR);
      env.release().setPoint(500.0, 0.0, LINEAR);
    }

    final var data = new TreeMap<Long, Double>();
    final var eval = new AREnvelopeADREvaluator(env);
    for (var time = 0L; time <= 48000L * 4L; ++time) {
      if (time == 48000L * 2L) {
        eval.beginRelease(time, true);
      }

      final var amp = eval.evaluate(time);
      data.put(time, amp);

      if (time < 48000L) {
        assertEquals(STATE_ATTACK, eval.state());
        assertTrue(amp >= 0.0);
        assertTrue(amp <= 1.0);
      }

      if (time >= 48000L && time < 2L * 48000L) {
        assertEquals(STATE_SUSTAIN, eval.state());
        assertTrue(amp >= 0.8);
        assertTrue(amp <= 1.0);
      }

      if (time >= 48000L * 2L) {
        assertEquals(STATE_RELEASE, eval.state());
        assertTrue(amp >= 0.0);
        assertTrue(amp <= 1.0);
      }
    }

    // this.chart(data, Paths.get("/tmp/chart.png"));
  }

  /**
   * Exercise a basic envelope.
   *
   * @throws Exception On errors
   */

  @Test
  public void testBasicEnvelopeRelative()
    throws Exception
  {
    final var env =
      new AREnvelopeADR(
        AREnvelopeTable.create(48000L),
        AREnvelopeTable.create(48000L),
        AREnvelopeTable.create(48000L)
      );

    {
      env.attack().setFirst(0.0, LINEAR);
      env.attack().setPoint(1000.0, 1.0, LINEAR);
      env.sustain().setFirst(1.0, LINEAR);
      env.sustain().setPoint(60.0, 0.8, LINEAR);
      env.sustain().setPoint(120.0, 1.0, LINEAR);
      env.release().setFirst(1.0, LINEAR);
      env.release().setPoint(500.0, 0.0, LINEAR);
    }

    final var data = new TreeMap<Long, Double>();

    {
      final var eval = new AREnvelopeADREvaluator(env);
      for (var time = 0L; time <= 48000L * 4L; ++time) {
        if (time == 48000L * 2L) {
          eval.beginRelease(time, true);
        }

        final var amp = eval.evaluate(time);
        data.put(time, amp);

        if (time < 48000L) {
          assertEquals(STATE_ATTACK, eval.state());
          assertTrue(amp >= 0.0);
          assertTrue(amp <= 1.0);
        }

        if (time >= 48000L && time < 48000L * 2L) {
          assertEquals(STATE_SUSTAIN, eval.state());
          assertTrue(amp >= 0.8);
          assertTrue(amp <= 1.0);
        }

        if (time >= 48000L * 2L) {
          assertEquals(STATE_RELEASE, eval.state());
          assertTrue(amp >= 0.0);
          assertTrue(amp <= 1.0);
        }
      }
    }

    // this.chart(data, Paths.get("/tmp/chart.png"));

    {
      env.setSampleRate(44100L);

      final var eval = new AREnvelopeADREvaluator(env);
      for (var time = 0L; time <= 44100L * 4L; ++time) {
        if (time == 44100L * 2L) {
          eval.beginRelease(time, true);
        }

        final var amp = eval.evaluate(time);
        data.put(time, amp);

        if (time < 44100L) {
          assertEquals(STATE_ATTACK, eval.state());
          assertTrue(amp >= 0.0);
          assertTrue(amp <= 1.0);
        }

        if (time >= 44100L && time < 44100L * 2L) {
          assertEquals(STATE_SUSTAIN, eval.state());
          assertTrue(amp >= 0.8);
          assertTrue(amp <= 1.0);
        }

        if (time >= 44100L * 2L) {
          assertEquals(STATE_RELEASE, eval.state());
          assertTrue(amp >= 0.0);
          assertTrue(amp <= 1.0);
        }
      }
    }
  }

  private void chart(
    final TreeMap<Long, Double> data,
    final Path file)
    throws IOException
  {
    final var width = 1200;
    final var height = 1000;

    final var chart =
      new XYChartBuilder()
        .xAxisTitle("Frames")
        .yAxisTitle("Amplitude")
        .width(width)
        .height(height)
        .build();

    final var xData = new ArrayList<Long>();
    final var yData = new ArrayList<Double>();

    for (final var entry : data.entrySet()) {
      xData.add(entry.getKey());
      yData.add(entry.getValue());
    }

    final var s0 = chart.addSeries("Amplitude", xData, yData);
    s0.setMarker(new None());

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

    styler.setXAxisMin(0.0);
    styler.setYAxisMax(1.0);
    styler.setYAxisMin(0.0);
    styler.setYAxisLogarithmic(false);

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
