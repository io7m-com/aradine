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

package com.io7m.aradine.instrument.spi1.json_data.internal;

import com.io7m.anethum.api.ParseSeverity;
import com.io7m.anethum.api.ParseStatus;
import com.io7m.anethum.api.ParsingException;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentDescription;
import com.io7m.aradine.instrument.spi1.json_data.ARI1InstrumentParserType;
import com.io7m.jlexing.core.LexicalPosition;
import tools.jackson.core.JacksonException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * The instrument parser.
 */

public final class ARI1JInstrumentParser
  implements ARI1InstrumentParserType
{
  private final URI source;
  private final InputStream stream;
  private final Consumer<ParseStatus> statusConsumer;

  /**
   * The instrument parser.
   *
   * @param inSource         The source
   * @param inStatusConsumer The status consumer
   * @param inStream         The stream
   */

  public ARI1JInstrumentParser(
    final URI inSource,
    final InputStream inStream,
    final Consumer<ParseStatus> inStatusConsumer)
  {
    this.source =
      Objects.requireNonNull(inSource, "Source");
    this.stream =
      Objects.requireNonNull(inStream, "Stream");
    this.statusConsumer =
      Objects.requireNonNull(inStatusConsumer, "StatusConsumer");
  }

  @Override
  public ARI1InstrumentDescription execute()
    throws ParsingException
  {
    try {
      final var mapper =
        ARI1JMappers.mapper();
      final var description =
        mapper.readValue(this.stream, ARI1JInstrumentDescription.class);

      return ARI1JInstrumentDescriptions.fromJSON(description);
    } catch (final JacksonException e) {
      throw this.parseExceptionOf(e);
    }
  }

  private ParsingException parseExceptionOf(
    final JacksonException e)
  {
    final var location =
      e.getLocation();

    final var position =
      LexicalPosition.<URI>builder()
        .setFile(this.source)
        .setLine(location.getLineNr())
        .setColumn(location.getColumnNr())
        .build();

    final var status =
      ParseStatus.builder("error-parse", e.getMessage())
        .withLexical(position)
        .withException(e)
        .withSeverity(ParseSeverity.PARSE_ERROR)
        .build();

    this.statusConsumer.accept(status);
    return new ParsingException(e.getMessage(), List.of(status));
  }

  @Override
  public void close()
    throws IOException
  {
    this.stream.close();
  }
}
