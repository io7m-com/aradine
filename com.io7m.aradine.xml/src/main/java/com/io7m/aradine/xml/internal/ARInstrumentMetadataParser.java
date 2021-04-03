/*
 * Copyright © 2021 Mark Raynsford <code@io7m.com> http://io7m.com
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

package com.io7m.aradine.xml.internal;

import com.io7m.anethum.common.ParseException;
import com.io7m.anethum.common.ParseSeverity;
import com.io7m.anethum.common.ParseStatus;
import com.io7m.aradine.instrument.metadata.ARInstrumentMetadata;
import com.io7m.aradine.instrument.metadata.parser.api.ARInstrumentMetadataParserType;
import com.io7m.aradine.xml.ARInstrumentMetadataSchemas;
import com.io7m.blackthorne.api.BTElementHandlerConstructorType;
import com.io7m.blackthorne.api.BTException;
import com.io7m.blackthorne.api.BTParseError;
import com.io7m.blackthorne.api.BTParseErrorType;
import com.io7m.blackthorne.api.BTQualifiedName;
import com.io7m.blackthorne.jxe.BlackthorneJXE;
import com.io7m.jxe.core.JXEXInclude;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.io7m.aradine.xml.ARInstrumentMetadataSchemas.instrument1Element;

public final class ARInstrumentMetadataParser implements
  ARInstrumentMetadataParserType
{
  private final URI source;
  private final InputStream stream;
  private final Consumer<ParseStatus> statusConsumer;

  public ARInstrumentMetadataParser(
    final URI inSource,
    final InputStream inStream,
    final Consumer<ParseStatus> inStatusConsumer)
  {
    this.source =
      Objects.requireNonNull(inSource, "source");
    this.stream =
      Objects.requireNonNull(inStream, "stream");
    this.statusConsumer =
      Objects.requireNonNull(inStatusConsumer, "statusConsumer");
  }

  private static ParseException mapException(
    final BTException e)
  {
    return new ParseException(
      e.getMessage(),
      e.errors()
        .stream()
        .map(ARInstrumentMetadataParser::mapError)
        .collect(Collectors.toUnmodifiableList())
    );
  }

  private static ParseStatus mapError(
    final BTParseError error)
  {
    return ParseStatus.builder()
      .setLexical(error.lexical())
      .setMessage(error.message())
      .setSeverity(mapSeverity(error.severity()))
      .build();
  }

  private static ParseSeverity mapSeverity(
    final BTParseErrorType.Severity severity)
  {
    switch (severity) {
      case WARNING:
        return ParseSeverity.PARSE_WARNING;
      case ERROR:
        return ParseSeverity.PARSE_ERROR;
    }
    throw new IllegalStateException(
      String.format("Unrecognized severity: %s", severity)
    );
  }

  @Override
  public ARInstrumentMetadata execute()
    throws ParseException
  {
    final Map<BTQualifiedName, BTElementHandlerConstructorType<?, ARInstrumentMetadata>> rootElements =
      Map.ofEntries(
        Map.entry(
          instrument1Element("Instrument"),
          AR1InstrumentMetadataParser::new
        )
      );

    try {
      return BlackthorneJXE.parse(
        this.source,
        this.stream,
        rootElements,
        Optional.empty(),
        JXEXInclude.XINCLUDE_DISABLED,
        ARInstrumentMetadataSchemas.instrumentSchemas()
      );
    } catch (final BTException e) {
      throw mapException(e);
    }
  }

  @Override
  public void close()
    throws IOException
  {
    this.stream.close();
  }
}
