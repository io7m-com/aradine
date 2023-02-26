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

import com.io7m.aradine.tests.filter.recursive1.ARF1LPFOnePoleTest;
import org.apache.commons.math4.legacy.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math4.transform.FastFourierTransform;
import org.apache.commons.numbers.complex.Complex;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.XYStyler;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.DoubleBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeMap;

public final class ARTestFrequencyAnalysis
{
  private final TreeMap<Long, ARTestFrequencyStatistics> freqStats;
  private final Properties properties;

  private ARTestFrequencyAnalysis(
    final TreeMap<Long, ARTestFrequencyStatistics> inFreqStats,
    final Properties inProperties)
  {
    this.freqStats =
      Objects.requireNonNull(inFreqStats, "freqStats");
    this.properties =
      Objects.requireNonNull(inProperties, "inProperties");
  }


  private static double amplitude(
    final Complex c)
  {
    return StrictMath.sqrt((c.real() * c.real()) + (c.imag() * c.imag()));
  }

  private static int smallerPowerOfTwo(
    final int value)
  {
    return (int) StrictMath.pow(
      2.0,
      StrictMath.floor(StrictMath.log(value) / StrictMath.log(2))
    );
  }

  public void save(
    final Path file)
    throws IOException
  {
    try (var output = Files.newOutputStream(file)) {
      this.properties.store(output, "");
    }
  }

  public void save(
    final String file)
    throws IOException
  {
    this.save(Paths.get(file));
  }

  public void chart(
    final String file)
    throws IOException
  {
    this.chart(Paths.get(file));
  }

  public void chart(
    final Path file)
    throws IOException
  {
    final var width = 800;
    final var height = 400;

    final var chart =
      new XYChartBuilder()
        .xAxisTitle("Frequency (hz)")
        .yAxisTitle("Amplitude")
        .width(width)
        .height(height)
        .build();

    final var xData = new ArrayList<Double>();
    final var yMean = new ArrayList<Double>();
    final var yStdd = new ArrayList<Double>();

    for (final var entry : this.freqStats.entrySet()) {
      final var stat = entry.getValue();
      xData.add(stat.frequencyBand());
      yMean.add(stat.meanAmplitude());
      yStdd.add(stat.standardDeviation());
    }

    final var s0 =
      chart.addSeries("Mean", xData, yMean);
    final var s1 =
      chart.addSeries("Stddev", xData, yStdd);

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

  public static ARTestFrequencyAnalysis calculateFrequencyContent(
    final DoubleBuffer sample,
    final double sampleRate)
    throws Exception
  {
    final var points =
      smallerPowerOfTwo(sample.capacity());

    final var powerOfTwoSegment = new double[points];
    for (int index = 0; index < points; ++index) {
      powerOfTwoSegment[index] = sample.get(index);
    }

    final var t =
      new FastFourierTransform(FastFourierTransform.Norm.UNIT);
    final List<Double> fftPoints =
      Arrays.stream(t.apply(powerOfTwoSegment))
        .map(c -> Double.valueOf(amplitude(c)))
        .limit(points / 2)
        .toList();

    /*
     * The frequency band size of a single point from the FFT.
     */

    final var nyquist =
      sampleRate / 2.0;
    final var frequencyBandSize =
      nyquist / ((double) points / 2.0);

    /*
     * The total number of frequency bands desired.
     */

    final var frequencyStatsBandsWanted =
      128;
    final var frequencyStatsBandSize =
      nyquist / (double) frequencyStatsBandsWanted;

    final var stats =
      new DescriptiveStatistics(points);

    final var frequencyStats =
      new TreeMap<Long, ARTestFrequencyStatistics>();

    var frequencyNow = 0.0;
    var frequencyStatAccum = 0.0;
    for (int index = 0; index < fftPoints.size(); ++index) {
      final var amp = fftPoints.get(index);
      stats.addValue(amp);

      frequencyNow += frequencyBandSize;
      frequencyStatAccum += frequencyBandSize;
      if (frequencyStatAccum >= frequencyStatsBandSize) {
        frequencyStats.put(
          Long.valueOf((long) Math.floor(frequencyNow)),
          new ARTestFrequencyStatistics(
            frequencyNow,
            stats.getMean(),
            stats.getStandardDeviation())
        );
        frequencyStatAccum = 0.0;
      }
    }

    final var properties = new Properties();
    for (final var entry : frequencyStats.entrySet()) {
      final var stat = entry.getValue();
      properties.setProperty(
        String.format("freq_%d_mean", entry.getKey()),
        String.format("%.16f", Double.valueOf(stat.meanAmplitude()))
      );
      properties.setProperty(
        String.format("freq_%d_stddev", entry.getKey()),
        String.format("%.16f", Double.valueOf(stat.standardDeviation()))
      );
      properties.setProperty(
        String.format("freq_%d_freq", entry.getKey()),
        String.format("%.16f", Double.valueOf(stat.frequencyBand()))
      );
    }

    return new ARTestFrequencyAnalysis(
      frequencyStats,
      properties
    );
  }

  public static ARTestFrequencyAnalysis loadFrequencyAnalysis(
    final String name,
    final double sampleRate)
    throws IOException
  {
    final var properties = new Properties();
    try (var stream = ARF1LPFOnePoleTest.class.getResourceAsStream(
      "/com/io7m/aradine/tests/" + name)) {
      properties.load(stream);
    }

    final var results = new TreeMap<Long, ARTestFrequencyStatistics>();
    for (int freq = 0; freq <= sampleRate / 2; ++freq) {
      final var freqName =
        String.format("freq_%d_freq", Integer.valueOf(freq));
      final var meanName =
        String.format("freq_%d_mean", Integer.valueOf(freq));
      final var stddevName =
        String.format("freq_%d_stddev", Integer.valueOf(freq));

      if (!properties.containsKey(freqName)) {
        continue;
      }

      final var stat =
        new ARTestFrequencyStatistics(
          Double.parseDouble(properties.getProperty(freqName)),
          Double.parseDouble(properties.getProperty(meanName)),
          Double.parseDouble(properties.getProperty(stddevName))
        );

      results.put(Long.valueOf(freq), stat);
    }
    return new ARTestFrequencyAnalysis(results, properties);
  }

  public TreeMap<Long, ARTestFrequencyStatistics> stats()
  {
    return this.freqStats;
  }

  public void saveMean(
    final String file)
    throws IOException
  {
    this.saveMean(Paths.get(file));
  }

  private void saveMean(
    final Path path)
    throws IOException
  {
    try (var writer = Files.newBufferedWriter(path)) {
      for (final var entry : this.freqStats.entrySet()) {
        final var stat = entry.getValue();
        writer.append(Double.toString(stat.frequencyBand()));
        writer.append(" ");
        writer.append(Double.toString(stat.meanAmplitude()));
        writer.newLine();
      }
    }
  }
}
