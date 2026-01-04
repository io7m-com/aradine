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

import com.io7m.aradine.api.ARBlob;
import com.io7m.aradine.api.ARCloseables;
import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.ARHash;
import com.io7m.aradine.api.ARHashAlgorithm;
import com.io7m.aradine.api.sample_map.ARSampleMapDataSummary;
import com.io7m.aradine.api.sample_map.ARSampleMapID;
import com.io7m.aurantium.api.AUFileReadableType;
import com.io7m.aurantium.api.AUMetadataValue;
import com.io7m.aurantium.api.AUSectionReadableClipDataType;
import com.io7m.aurantium.api.AUSectionReadableClipDefinitionsType;
import com.io7m.aurantium.api.AUSectionReadableIdentifierType;
import com.io7m.aurantium.api.AUSectionReadableKeyAssignmentsType;
import com.io7m.aurantium.api.AUVersion;
import com.io7m.aurantium.parser.api.AUParseRequest;
import com.io7m.aurantium.parser.api.AUParserFactoryType;
import com.io7m.aurantium.parser.api.AUProbeFactoryType;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import com.io7m.jsamplebuffer.api.SampleBufferRateConverterFactoryType;
import com.io7m.mime2045.core.MimeType;
import com.io7m.seltzer.api.SStructuredErrorExceptionType;
import com.io7m.seltzer.io.SIOException;
import com.io7m.verona.core.Version;
import com.io7m.wendover.core.CloseShieldSeekableByteChannel;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A sample map file probe.
 */

public final class ARASampleMapProbe
{
  private final AtomicBoolean closed;
  private final CloseableCollectionType<ARException> resources;
  private final SampleBufferRateConverterFactoryType converters;
  private final AUFileReadableType fileParsed;
  private final AUSectionReadableClipDefinitionsType clipDefinitions;
  private final AUSectionReadableClipDataType clipData;
  private final AUSectionReadableKeyAssignmentsType keyAssignments;
  private final AUSectionReadableIdentifierType identifier;

  private ARASampleMapProbe(
    final CloseableCollectionType<ARException> inResources,
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
   * Probe a sample map.
   *
   * @param parsers The parsers
   * @param probes  The probes
   * @param file    The file
   *
   * @return The map
   *
   * @throws ARException On errors
   */

  public static Optional<ARSampleMapDataSummary> probe(
    final List<AUParserFactoryType> parsers,
    final AUProbeFactoryType probes,
    final Path file)
    throws ARException
  {
    final var probeOp =
      new OpProbe(
        parsers,
        probes,
        file
      );

    try {
      return probeOp.execute();
    } catch (final Throwable e) {
      probeOp.close();
      throw e;
    }
  }

  private static ARException wrap(
    final Exception e)
    throws ARException
  {
    return switch (e) {
      case final ARException ex -> {
        throw ex;
      }
      case final SStructuredErrorExceptionType<?> es -> {
        throw new ARException(
          es.message(),
          e,
          es.errorCode().toString(),
          es.attributes(),
          es.remediatingAction()
        );
      }
      case final Throwable et -> {
        yield new ARException(
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

  private static final class OpProbe
  {
    private final List<AUParserFactoryType> parsers;
    private final AUProbeFactoryType probes;
    private final Path file;
    private final CloseableCollectionType<ARException> resources;
    private FileChannel fileChannel;
    private AUVersion formatVersion;
    private List<AUParserFactoryType> parsersOrdered;
    private AUParserFactoryType parser;
    private AUFileReadableType fileParsed;
    private CloseShieldSeekableByteChannel safeChannel;

    OpProbe(
      final List<AUParserFactoryType> inParsers,
      final AUProbeFactoryType inProbes,
      final Path inFile)
    {
      this.parsers = inParsers;
      this.probes = inProbes;
      this.file = inFile;
      this.resources = ARCloseables.create();
    }

    public Optional<ARSampleMapDataSummary> execute()
    {
      try {
        this.openFile();
        this.probeVersion();
        this.findParser();
        this.runParser();
        return this.processParsed();
      } catch (final Exception e) {
        return Optional.empty();
      }
    }

    private void openFile()
      throws ARException
    {
      try {
        this.fileChannel =
          this.resources.add(FileChannel.open(this.file));
        this.safeChannel =
          new CloseShieldSeekableByteChannel(this.fileChannel);
      } catch (final IOException e) {
        throw this.errorIO(e);
      }
    }

    private void probeVersion()
      throws ARException
    {
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
    }

    private void findParser()
      throws ARException
    {
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
    }

    private void runParser()
      throws ARException
    {
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
    }

    private Optional<ARSampleMapDataSummary> processParsed()
      throws IOException, ARException
    {
      final var identifierSection =
        this.fileParsed.openIdentifier()
          .orElseThrow(() -> this.errorMissingData("Identifier"));

      final var metadataSectionOpt =
        this.fileParsed.openMetadata();

      final String title;
      final String description;
      if (metadataSectionOpt.isPresent()) {
        final var metadataSection =
          metadataSectionOpt.get();
        final var metadata =
          metadataSection.metadata();

        title = metadata.stream()
          .filter(m -> "dc:title".equalsIgnoreCase(m.name()))
          .map(AUMetadataValue::value)
          .findFirst()
          .orElse("");
        description = metadata.stream()
          .filter(m -> "dc:description".equalsIgnoreCase(m.name()))
          .map(AUMetadataValue::value)
          .findFirst()
          .orElse("");
      } else {
        title = "";
        description = "";
      }

      final var id =
        identifierSection.identifier();
      final var version =
        Version.of(
          id.version().major(),
          id.version().minor(),
          0
        );

      return Optional.of(
        new ARSampleMapDataSummary(
          new ARSampleMapID(
            id.group(),
            id.name(),
            version
          ),
          title,
          description,
          this.hash()
        )
      );
    }

    private ARBlob hash()
      throws ARException
    {
      try (var stream = Files.newInputStream(this.file)) {
        final var digest = MessageDigest.getInstance("SHA-256");
        try (var digestStream = new DigestInputStream(stream, digest)) {
          digestStream.transferTo(OutputStream.nullOutputStream());
        }
        return new ARBlob(
          Files.size(this.file),
          new ARHash(
            ARHashAlgorithm.SHA_256,
            HexFormat.of().formatHex(digest.digest())),
          MimeType.of(
            "application",
            "vnd.com.io7m.aurantium.sample_map"
          )
        );
      } catch (final Exception e) {
        throw wrap(e);
      }
    }

    private ARException errorMissingData(
      final String data)
    {
      return new ARException(
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

    private ARException errorNoParserSupporting()
    {
      return new ARException(
        "No parser available supporting the given version.",
        "error-sample-map-format-version",
        Map.ofEntries(
          Map.entry("File", this.file.toAbsolutePath().toString()),
          Map.entry("Version", this.formatVersion.toString())
        ),
        Optional.empty()
      );
    }

    private ARException errorIO(
      final IOException e)
    {
      return new ARException(
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
      throws ARException
    {
      this.resources.close();
    }
  }
}
