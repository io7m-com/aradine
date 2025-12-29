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

package com.io7m.aradine.sample_map.aurantium.internal;

import com.io7m.aradine.api.progress.ARProgress;
import com.io7m.aradine.api.sample_map.ARSampleMapException;
import com.io7m.aradine.api.sample_map.ARSampleMapFileType;
import com.io7m.aradine.api.sample_map.ARSampleMapLoadConfiguration;
import com.io7m.aradine.api.sample_map.ARSampleMapType;
import com.io7m.aurantium.api.AUClipDescription;
import com.io7m.aurantium.api.AUClipID;
import com.io7m.aurantium.api.AUFileReadableType;
import com.io7m.aurantium.api.AUSectionReadableClipDataType;
import com.io7m.aurantium.api.AUSectionReadableClipDefinitionsType;
import com.io7m.aurantium.api.AUSectionReadableIdentifierType;
import com.io7m.aurantium.api.AUSectionReadableKeyAssignmentsType;
import com.io7m.aurantium.api.AUVersion;
import com.io7m.aurantium.parser.api.AUParseRequest;
import com.io7m.aurantium.parser.api.AUParserFactoryType;
import com.io7m.aurantium.parser.api.AUProbeFactoryType;
import com.io7m.aurantium.xmedia.AUXMediaConversion;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import com.io7m.jsamplebuffer.api.SampleBufferRateConverterFactoryType;
import com.io7m.jsamplebuffer.api.SampleBufferType;
import com.io7m.jsamplebuffer.vanilla.SampleBufferFloat;
import com.io7m.jsamplebuffer.xmedia.SXMSampleBuffers;
import com.io7m.seltzer.api.SStructuredErrorExceptionType;
import com.io7m.seltzer.io.SIOException;
import com.io7m.wendover.core.CloseShieldSeekableByteChannel;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * A sample map file.
 */

public final class ARASampleMapFile
  implements ARSampleMapFileType
{
  private final AtomicBoolean closed;
  private final CloseableCollectionType<ARSampleMapException> resources;
  private final SampleBufferRateConverterFactoryType converters;
  private final AUFileReadableType fileParsed;
  private final AUSectionReadableClipDefinitionsType clipDefinitions;
  private final AUSectionReadableClipDataType clipData;
  private final AUSectionReadableKeyAssignmentsType keyAssignments;
  private final AUSectionReadableIdentifierType identifier;

  private ARASampleMapFile(
    final CloseableCollectionType<ARSampleMapException> inResources,
    final SampleBufferRateConverterFactoryType inConverters,
    final AUFileReadableType inFileParsed,
    final AUSectionReadableClipDefinitionsType inClipDefinitions,
    final AUSectionReadableClipDataType inClipData,
    final AUSectionReadableKeyAssignmentsType inKeyAssignments,
    final AUSectionReadableIdentifierType inIdentifier)
  {
    this.resources = inResources;
    this.converters = inConverters;
    this.fileParsed = inFileParsed;
    this.clipDefinitions = inClipDefinitions;
    this.clipData = inClipData;
    this.keyAssignments = inKeyAssignments;
    this.identifier = inIdentifier;
    this.closed = new AtomicBoolean(false);
  }

  /**
   * Open a sample map.
   *
   * @param converters       The sample rate converters
   * @param parsers          The parsers
   * @param probes           The probes
   * @param file             The file
   * @param progressConsumer The progress consumer
   *
   * @return The map
   *
   * @throws ARSampleMapException On errors
   */

  public static ARSampleMapFileType open(
    final SampleBufferRateConverterFactoryType converters,
    final List<AUParserFactoryType> parsers,
    final AUProbeFactoryType probes,
    final Path file,
    final Consumer<ARProgress> progressConsumer)
    throws ARSampleMapException
  {
    final var open =
      new OpOpen(
        converters,
        parsers,
        probes,
        file,
        progressConsumer
      );

    try {
      return open.execute();
    } catch (final Throwable e) {
      open.close();
      throw e;
    }
  }

  @Override
  public ARSampleMapType load(
    final ARSampleMapLoadConfiguration configuration)
    throws ARSampleMapException
  {
    Objects.requireNonNull(configuration, "Configuration");
    this.checkNotClosed();

    final var open = new OpLoad(configuration, this.converters, this);
    try {
      return open.execute();
    } catch (final Throwable e) {
      open.close();
      throw e;
    }
  }

  private void checkNotClosed()
  {
    if (this.closed.get()) {
      throw new IllegalStateException("Sample map file is closed.");
    }
  }

  @Override
  public boolean isClosed()
  {
    return this.closed.get();
  }

  @Override
  public void close()
    throws ARSampleMapException
  {
    if (this.closed.compareAndSet(false, true)) {
      this.resources.close();
    }
  }

  private static final class OpOpen
  {
    private static final int SUBTASK_COUNT = 5;
    private final List<AUParserFactoryType> parsers;
    private final AUProbeFactoryType probes;
    private final Path file;
    private final Consumer<ARProgress> progressConsumer;
    private final String task;
    private final CloseableCollectionType<ARSampleMapException> resources;
    private final SampleBufferRateConverterFactoryType converters;
    private double taskProgress;
    private String subTask;
    private double subtaskProgress;
    private FileChannel fileChannel;
    private AUVersion formatVersion;
    private List<AUParserFactoryType> parsersOrdered;
    private AUParserFactoryType parser;
    private AUFileReadableType fileParsed;
    private AUSectionReadableClipDataType clipData;
    private AUSectionReadableClipDefinitionsType clipDefinitions;
    private AUSectionReadableKeyAssignmentsType keyAssignments;
    private AUSectionReadableIdentifierType identifier;
    private CloseShieldSeekableByteChannel safeChannel;

    OpOpen(
      final SampleBufferRateConverterFactoryType inConverters,
      final List<AUParserFactoryType> inParsers,
      final AUProbeFactoryType inProbes,
      final Path inFile,
      final Consumer<ARProgress> inProgressConsumer)
    {
      this.converters = inConverters;
      this.parsers = inParsers;
      this.probes = inProbes;
      this.file = inFile;
      this.progressConsumer = inProgressConsumer;

      this.resources = ARCloseables.create();
      this.task = "Opening sample map.";
      this.taskProgress = 0.0;
      this.subTask = "";
      this.subtaskProgress = 0.0;
    }

    private static double taskProgressOf(
      final int subTaskIndex)
    {
      return (double) (subTaskIndex + 1) / (double) SUBTASK_COUNT;
    }

    private ARProgress progressNow()
    {
      return new ARProgress(
        this.task,
        this.taskProgress,
        this.subTask,
        this.subtaskProgress
      );
    }

    private void publishProgressNow()
    {
      this.progressConsumer.accept(this.progressNow());
    }

    public ARSampleMapFileType execute()
      throws ARSampleMapException
    {
      try {
        this.openFile();
        this.probeVersion();
        this.findParser();
        this.runParser();
        return this.processParsed();
      } catch (final Exception e) {
        throw wrap(e);
      }
    }

    private void openFile()
      throws ARSampleMapException
    {
      this.taskProgress = taskProgressOf(0);
      this.subTask = "Opening file.";
      this.publishProgressNow();

      try {
        this.fileChannel =
          this.resources.add(FileChannel.open(this.file));
        this.safeChannel =
          new CloseShieldSeekableByteChannel(this.fileChannel);
      } catch (final IOException e) {
        throw this.errorIO(e);
      }

      this.subtaskProgress = 1.0;
      this.publishProgressNow();
    }

    private void probeVersion()
      throws ARSampleMapException
    {
      this.taskProgress = taskProgressOf(1);
      this.subTask = "Probing sample map version.";
      this.publishProgressNow();

      try {
        final var probe =
          this.probes.createProbe(
            this.file.toUri(),
            this.safeChannel
          );

        this.formatVersion = probe.execute();
      } catch (final SIOException e) {
        throw wrap(e);
      }

      this.subtaskProgress = 1.0;
      this.publishProgressNow();
    }

    private void findParser()
      throws ARSampleMapException
    {
      this.taskProgress = taskProgressOf(2);
      this.subTask = "Finding parser for file version.";
      this.publishProgressNow();

      this.parsersOrdered =
        this.parsers.stream()
          .sorted((x, y) -> {
            return Comparator.comparing(AUParserFactoryType::supportedMajorVersion)
              .thenComparing(AUParserFactoryType::highestMinorVersion)
              .reversed()
              .compare(x, y);
          })
          .toList();

      for (final var parserFactory : this.parsersOrdered) {
        if (parserFactory.supportedMajorVersion() == this.formatVersion.major()) {
          this.parser = parserFactory;
          break;
        }
      }

      if (this.parser == null) {
        throw this.errorNoParserSupporting();
      }

      this.subtaskProgress = 1.0;
      this.publishProgressNow();
    }

    private void runParser()
      throws ARSampleMapException
    {
      this.taskProgress = taskProgressOf(3);
      this.subTask = "Executing parser for file.";
      this.publishProgressNow();

      final var parseRequest =
        AUParseRequest.builder(this.safeChannel, this.file.toUri())
          .build();

      try (var parserNow = this.parser.createParser(parseRequest)) {
        this.fileParsed = this.resources.add(parserNow.execute());
      } catch (final SIOException e) {
        throw wrap(e);
      } catch (final IOException e) {
        throw this.errorIO(e);
      }

      this.subtaskProgress = 1.0;
      this.publishProgressNow();
    }

    private ARSampleMapFileType processParsed()
      throws IOException, ARSampleMapException
    {
      this.taskProgress = taskProgressOf(4);
      this.subTask = "Processing parsed file.";
      this.publishProgressNow();

      this.clipData =
        this.fileParsed.openClipData()
          .orElseThrow(() -> this.errorMissingData("Clip Data"));
      this.clipDefinitions =
        this.fileParsed.openClipDefinitions()
          .orElseThrow(() -> this.errorMissingData("Clip Definitions"));
      this.keyAssignments =
        this.fileParsed.openKeyAssignments()
          .orElseThrow(() -> this.errorMissingData("Key Assignments"));
      this.identifier =
        this.fileParsed.openIdentifier()
          .orElseThrow(() -> this.errorMissingData("Identifier"));

      this.subtaskProgress = 1.0;
      this.publishProgressNow();

      return new ARASampleMapFile(
        this.resources,
        this.converters,
        this.fileParsed,
        this.clipDefinitions,
        this.clipData,
        this.keyAssignments,
        this.identifier
      );
    }

    private ARSampleMapException errorMissingData(
      final String data)
    {
      return new ARSampleMapException(
        "Sample map is missing required data.",
        "error-missing-data",
        Map.ofEntries(
          Map.entry("File", this.file.toAbsolutePath().toString()),
          Map.entry("Version", this.formatVersion.toString()),
          Map.entry("Data", data)
        ),
        Optional.empty()
      );
    }

    private ARSampleMapException errorNoParserSupporting()
    {
      return new ARSampleMapException(
        "No parser available supporting the given version.",
        "error-sample-map-format-version",
        Map.ofEntries(
          Map.entry("File", this.file.toAbsolutePath().toString()),
          Map.entry("Version", this.formatVersion.toString())
        ),
        Optional.empty()
      );
    }

    private ARSampleMapException errorIO(
      final IOException e)
    {
      return new ARSampleMapException(
        Objects.requireNonNullElse(
          e.getMessage(),
          e.getClass().getSimpleName()
        ),
        e,
        "error-io",
        Map.ofEntries(
          Map.entry("File", this.file.toAbsolutePath().toString())
        ),
        Optional.empty()
      );
    }

    public void close()
      throws ARSampleMapException
    {
      this.resources.close();
    }
  }

  private static final class OpLoad
  {
    private final ARSampleMapLoadConfiguration configuration;
    private final SampleBufferRateConverterFactoryType converters;
    private final ARASampleMapFile file;
    private final String task;
    private double taskProgress;
    private String subTask;
    private double subtaskProgress;

    OpLoad(
      final ARSampleMapLoadConfiguration inConfiguration,
      final SampleBufferRateConverterFactoryType inConverters,
      final ARASampleMapFile inFile)
    {
      this.configuration = inConfiguration;
      this.converters = inConverters;
      this.file = inFile;
      this.task = "Loading sample map.";
      this.taskProgress = 0.0;
      this.subTask = "";
      this.subtaskProgress = 0.0;
    }

    public ARSampleMapType execute()
      throws ARSampleMapException
    {
      try {
        this.subTask = "Converting clip data.";
        this.subtaskProgress = 0.0;
        this.publishProgressNow();

        final var clipList =
          this.file.clipDefinitions.clips();

        if (clipList.isEmpty()) {
          return new ARASampleMap(Map.of());
        }

        final var clipDatas =
          this.file.clipData;

        final var sampleBuffers =
          new HashMap<AUClipID, SampleBufferType>(clipList.size());

        var index = 0;
        for (final var clipDefinition : clipList) {
          this.taskProgress = (double) index / (double) clipList.size();
          this.subTask = "Converting clip %s".formatted(clipDefinition.id());
          this.subtaskProgress = 0.0;
          this.publishProgressNow();

          try (var clipData = clipDatas.audioDataForClip(clipDefinition)) {
            sampleBuffers.put(
              clipDefinition.id(),
              this.convertClipData(clipDefinition, clipData)
            );
          }

          this.subTask = "Converted clip %s".formatted(clipDefinition.id());
          this.subtaskProgress = 1.0;
          this.publishProgressNow();
          ++index;
        }

        this.taskProgress = 1.0;
        this.subtaskProgress = 1.0;
        this.publishProgressNow();
        return new ARASampleMap(Map.copyOf(sampleBuffers));
      } catch (final IOException e) {
        throw wrap(e);
      }
    }

    ///
    /// Convert the clip data to the native representation that we want.
    /// For practical reasons, this is a series of chained conversions.
    ///
    /// 1. Get from clip data to a raw PCM stream with the same sample rate,
    ///    same bit depth, etc. This might entail decoding FLAC data, and
    ///    the audio system provider doesn't support changing properties such
    ///    as sample rate and bit depth while decoding.
    ///
    /// 2. Get from the PCM stream to floating point buffer at the same
    ///    sample rate.
    ///
    /// 3. Convert the buffer to our native sample rate.
    ///

    private SampleBufferType convertClipData(
      final AUClipDescription clipDefinition,
      final SeekableByteChannel clipData)
      throws ARSampleMapException
    {
      final var converter =
        this.converters.createConverter();

      try (var sourceStream =
             AUXMediaConversion.createAudioStreamOf(clipDefinition, clipData)) {

        try (var convertStream = convertToFloat(sourceStream)) {
          final var buffer =
            SXMSampleBuffers.readSampleBufferFromStream(
              convertStream,
              SampleBufferFloat::createWithHeapBuffer
            );

          return converter.convert(
            SampleBufferFloat::createWithHeapBuffer,
            buffer,
            this.configuration.sampleRate()
          );
        }
      } catch (final Exception e) {
        throw wrap(e);
      }
    }

    private static AudioInputStream convertToFloat(
      final AudioInputStream sourceStream)
    {
      final var sourceFormat =
        sourceStream.getFormat();

      final var targetFormat =
        new AudioFormat(
          AudioFormat.Encoding.PCM_FLOAT,
          sourceFormat.getSampleRate(),
          32,
          sourceFormat.getChannels(),
          sourceFormat.getChannels() * 4,
          sourceFormat.getFrameRate(),
          sourceFormat.isBigEndian()
        );

      return AudioSystem.getAudioInputStream(
        targetFormat,
        sourceStream
      );
    }

    private ARProgress progressNow()
    {
      return new ARProgress(
        this.task,
        this.taskProgress,
        this.subTask,
        this.subtaskProgress
      );
    }

    private void publishProgressNow()
    {
      this.configuration.progressConsumer()
        .accept(this.progressNow());
    }

    public void close()
    {
      // No extra resources.
    }
  }

  private static ARSampleMapException wrap(
    final Exception e)
    throws ARSampleMapException
  {
    return switch (e) {
      case final ARSampleMapException ex -> {
        throw ex;
      }
      case final SStructuredErrorExceptionType<?> es -> {
        throw new ARSampleMapException(
          es.message(),
          e,
          es.errorCode().toString(),
          es.attributes(),
          es.remediatingAction()
        );
      }
      case final Throwable et -> {
        yield new ARSampleMapException(
          Objects.requireNonNullElse(
            et.getMessage(),
            et.getClass().getSimpleName()),
          et,
          "error-exception",
          Map.of(),
          Optional.empty()
        );
      }
    };
  }
}
