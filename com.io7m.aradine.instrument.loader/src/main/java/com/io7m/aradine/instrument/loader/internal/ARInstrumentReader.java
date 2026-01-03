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

package com.io7m.aradine.instrument.loader.internal;

import com.io7m.anethum.api.ParsingException;
import com.io7m.aradine.api.ARBlob;
import com.io7m.aradine.api.ARBytes;
import com.io7m.aradine.api.ARCloseables;
import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.ARHash;
import com.io7m.aradine.api.ARHashAlgorithm;
import com.io7m.aradine.api.instrument.ARInstrumentData;
import com.io7m.aradine.api.instrument.ARInstrumentID;
import com.io7m.aradine.instrument.loader.api.ARInstrumentReadResultType;
import com.io7m.aradine.instrument.loader.api.ARInstrumentReadV1;
import com.io7m.aradine.instrument.loader.api.ARInstrumentReaderType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentDescription;
import com.io7m.aradine.instrument.spi1.ARI1VersionQualifier;
import com.io7m.aradine.instrument.spi1.json_data.ARI1InstrumentParserFactoryType;
import com.io7m.aradine.instrument.spi1.json_data.ARI1InstrumentParsers;
import com.io7m.aradine.instrument.spi1.json_data.ARI1Schemas;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import com.io7m.lanark.core.RDottedName;
import com.io7m.verona.core.Version;
import com.io7m.verona.core.VersionQualifier;
import org.apache.commons.io.input.BoundedInputStream;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.StringNode;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;

/**
 * An instrument reader.
 */

public final class ARInstrumentReader implements ARInstrumentReaderType
{
  private static final Pattern LEADING_SLASHES =
    Pattern.compile("^/+");

  private static final ARI1InstrumentParserFactoryType PARSERS1 =
    new ARI1InstrumentParsers();

  private final CloseableCollectionType<ARException> resources;
  private final Path file;
  private final HashMap<String, String> attributes;
  private Manifest manifest;
  private String instrumentFile;
  private byte[] entryData;
  private long size;

  private ARInstrumentReader(
    final Path inFile)
  {
    this.file =
      Objects.requireNonNull(inFile, "File");
    this.resources =
      ARCloseables.create();

    this.attributes = new HashMap<>(4);
    this.attributes.put("File", this.file.toAbsolutePath().toString());
  }

  /**
   * Create a new instrument reader.
   *
   * @param file The source file
   *
   * @return The reader
   */

  public static ARInstrumentReader create(
    final Path file)
  {
    return new ARInstrumentReader(file);
  }

  @Override
  public ARInstrumentData execute()
    throws ARException
  {
    return this.executeMain().data();
  }

  private ARReadResultType executeMain()
    throws ARException
  {
    try {
      this.size =
        Files.size(this.file);
      this.manifest =
        this.parseManifest();
      this.instrumentFile =
        this.findManifestInstrumentFile();

      final var matcher =
        LEADING_SLASHES.matcher(this.instrumentFile);
      this.instrumentFile =
        matcher.replaceAll("");

      return this.parseInstrument();
    } catch (final Exception e) {
      throw this.errorException(e);
    }
  }

  @Override
  public ARInstrumentReadResultType executeAndParse()
    throws ARException
  {
    return switch (this.executeMain()) {
      case final ARRead1 r1 -> {
        yield new ARInstrumentReadV1(r1.data, r1.description);
      }
    };
  }

  private ARException errorException(
    final Exception e)
  {
    final var errorCode =
      switch (e) {
        case IOException _ -> "error-io";
        case ParsingException _ -> "error-parsing";
        case JacksonException _ -> "error-json";
        case final ARException x -> x.errorCode();
        case Exception _ -> "error-exception";
      };

    return new ARException(
      Objects.requireNonNullElse(e.getMessage(), e.getClass().getSimpleName()),
      e,
      errorCode,
      this.attributes,
      Optional.empty()
    );
  }

  private ARReadResultType parseInstrument()
    throws Exception
  {
    final var fileStream =
      this.resources.add(Files.newInputStream(this.file));
    final var bufferedStream =
      this.resources.add(new BufferedInputStream(fileStream));
    final var zipStream =
      this.resources.add(new ZipInputStream(bufferedStream));

    while (true) {
      final var entry = zipStream.getNextEntry();
      if (entry == null) {
        throw this.errorUnableToOpenInstrument();
      }
      if (Objects.equals(entry.getName(), this.instrumentFile)) {
        return this.parseInstrumentFromEntry(zipStream);
      }
    }
  }

  private ARReadResultType parseInstrumentFromEntry(
    final ZipInputStream stream)
    throws Exception
  {
    try (var limitStream = new BoundedInputStream(stream, 16_000_000L)) {
      this.entryData = limitStream.readAllBytes();
    }

    final var baseMapper =
      JsonMapper.shared();
    final var document =
      baseMapper.readTree(this.entryData);
    final var schemaId1 =
      ARI1Schemas.schema1().toString();
    final var schemaId =
      document.get("%Schema");

    if (schemaId instanceof final StringNode schemaText) {
      this.attributes.put("Schema", schemaText.asString());
      if (Objects.equals(schemaText.asString(), schemaId1)) {
        return this.parseInstrument1();
      }
      throw this.errorUnsupportedSchema();
    }

    throw this.errorInvalidInstrument();
  }

  private ARReadResultType parseInstrument1()
    throws Exception
  {
    final var fileName =
      Paths.get(this.instrumentFile);

    final var description =
      PARSERS1.parse(
        fileName.toUri(),
        new ByteArrayInputStream(this.entryData)
      );
    final var hash =
      this.hashFile();

    final var metadata =
      description.metadata();
    final var textTitle =
      metadata.getOrDefault("dc.title", "");
    final var textDescription =
      metadata.getOrDefault("dc.description", "");
    final var descriptionVersion =
      description.version();

    final var rVersion =
      new Version(
        descriptionVersion.major(),
        descriptionVersion.minor(),
        descriptionVersion.patch(),
        descriptionVersion.qualifier()
          .map(ARI1VersionQualifier::text)
          .map(VersionQualifier::new)
      );

    final var identifier =
      new ARInstrumentID(
        new RDottedName(description.group().value()),
        new RDottedName(description.identifier().value()),
        rVersion
      );

    final ARInstrumentData data =
      new ARInstrumentData(
        identifier,
        new RDottedName(ARI1Schemas.formatName().value()),
        new ARBytes(this.entryData),
        textTitle,
        textDescription,
        new ARBlob(this.size, hash, ARI1Schemas.instrumentMimeType())
      );

    return new ARRead1(description, data);
  }

  private ARHash hashFile()
    throws IOException, NoSuchAlgorithmException
  {
    final var fileStream =
      this.resources.add(Files.newInputStream(this.file));
    final var digest =
      MessageDigest.getInstance("SHA-256");
    final var digestStream =
      this.resources.add(new DigestInputStream(fileStream, digest));
    final var outputStream =
      this.resources.add(OutputStream.nullOutputStream());

    digestStream.transferTo(outputStream);

    return new ARHash(
      ARHashAlgorithm.SHA_256,
      HexFormat.of().formatHex(digest.digest())
    );
  }

  private String findManifestInstrumentFile()
    throws ARException
  {
    final var manifestFileName =
      this.manifest.getMainAttributes()
        .getValue("Aradine-Instrument");

    if (manifestFileName == null) {
      throw this.errorMissingAradineInstrument();
    }

    this.attributes.put("Aradine-Instrument", manifestFileName);
    return manifestFileName;
  }

  private Manifest parseManifest()
    throws IOException, ARException
  {
    final var fileStream =
      this.resources.add(Files.newInputStream(this.file));
    final var bufferedStream =
      this.resources.add(new BufferedInputStream(fileStream));
    final var zipStream =
      this.resources.add(new ZipInputStream(bufferedStream));

    while (true) {
      final var entry = zipStream.getNextEntry();
      if (entry == null) {
        throw this.errorUnableToFindManifest();
      }
      if (Objects.equals(entry.getName(), "META-INF/MANIFEST.MF")) {
        return new Manifest(zipStream);
      }
    }
  }

  private ARException errorUnableToFindManifest()
  {
    return new ARException(
      "Unable to locate a META-INF/MANIFEST.MF file.",
      "error-instrument-manifest-missing",
      this.attributes,
      Optional.empty()
    );
  }

  private ARException errorMissingAradineInstrument()
  {
    return new ARException(
      "No Aradine-Instrument entry in the given manifest.",
      "error-manifest-missing-aradine-instrument",
      this.attributes,
      Optional.empty()
    );
  }

  private ARException errorUnableToOpenInstrument()
  {
    return new ARException(
      "The Aradine-Instrument entry in the manifest refers to a nonexistent file.",
      "error-manifest-nonexistent-instrument",
      this.attributes,
      Optional.empty()
    );
  }

  private ARException errorUnsupportedSchema()
  {
    return new ARException(
      "The instrument definition uses an unsupported schema version.",
      "error-unsupported-schema-version",
      this.attributes,
      Optional.empty()
    );
  }

  private ARException errorInvalidInstrument()
  {
    return new ARException(
      "The instrument definition is invalid.",
      "error-invalid-instrument",
      this.attributes,
      Optional.empty()
    );
  }

  @Override
  public void close()
    throws ARException
  {
    this.resources.close();
  }

  private sealed interface ARReadResultType
  {
    ARInstrumentData data();
  }

  private record ARRead1(
    ARI1InstrumentDescription description,
    ARInstrumentData data)
    implements ARReadResultType
  {

  }
}
