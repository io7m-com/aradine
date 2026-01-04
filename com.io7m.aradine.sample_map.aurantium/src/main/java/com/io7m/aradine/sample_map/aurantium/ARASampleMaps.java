/*
 * Copyright © 2025 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

package com.io7m.aradine.sample_map.aurantium;

import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.progress.ARProgress;
import com.io7m.aradine.api.sample_map.ARSampleMapDataSummary;
import com.io7m.aradine.api.sample_map.ARSampleMapFileFactoryType;
import com.io7m.aradine.api.sample_map.ARSampleMapFileType;
import com.io7m.aradine.api.sample_map.ARSampleMapProbeType;
import com.io7m.aradine.sample_map.aurantium.internal.ARASampleMapFile;
import com.io7m.aradine.sample_map.aurantium.internal.ARASampleMapProbe;
import com.io7m.aurantium.parser.api.AUParserFactoryType;
import com.io7m.aurantium.parser.api.AUProbeFactoryType;
import com.io7m.aurantium.parser.api.AUProbes;
import com.io7m.aurantium.vanilla.AU1Parsers;
import com.io7m.jsamplebuffer.api.SampleBufferRateConverterFactoryType;
import com.io7m.jsamplebuffer.xmedia.SXMSampleBufferRateConverters;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Aurantium sample maps.
 */

public final class ARASampleMaps
  implements ARSampleMapFileFactoryType, ARSampleMapProbeType
{
  private final List<AUParserFactoryType> parsers;
  private final AUProbeFactoryType probes;
  private final SampleBufferRateConverterFactoryType converters;

  /**
   * Aurantium sample maps.
   */

  public ARASampleMaps()
  {
    this(
      new SXMSampleBufferRateConverters(),
      List.of(new AU1Parsers()),
      new AUProbes()
    );
  }

  /**
   * Aurantium sample maps.
   *
   * @param inParsers    The parsers
   * @param inProbes     The probes
   * @param inConverters Tbe sample rate converters
   */

  public ARASampleMaps(
    final SampleBufferRateConverterFactoryType inConverters,
    final List<AUParserFactoryType> inParsers,
    final AUProbeFactoryType inProbes)
  {
    this.converters =
      Objects.requireNonNull(inConverters, "Converters");
    this.parsers =
      Objects.requireNonNull(inParsers, "Parsers");
    this.probes =
      Objects.requireNonNull(inProbes, "Probes");
  }

  @Override
  public ARSampleMapFileType open(
    final Path file,
    final Consumer<ARProgress> progressConsumer)
    throws ARException
  {
    Objects.requireNonNull(file, "File");
    Objects.requireNonNull(progressConsumer, "ProgressConsumer");

    return ARASampleMapFile.open(
      this.converters,
      this.parsers,
      this.probes,
      file,
      progressConsumer
    );
  }

  @Override
  public Optional<ARSampleMapDataSummary> probe(
    final Path file)
    throws ARException
  {
    return ARASampleMapProbe.probe(
      this.parsers,
      this.probes,
      file
    );
  }
}
