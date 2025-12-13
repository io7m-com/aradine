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

import com.io7m.aradine.instrument.spi1.ARI1InstrumentDescription;
import com.io7m.aradine.instrument.spi1.json_data.ARI1InstrumentSerializerType;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * The instrument serializer.
 */

public final class ARI1JInstrumentSerializer
  implements ARI1InstrumentSerializerType
{
  private final OutputStream stream;

  /**
   * @param inStream The output stream
   */

  public ARI1JInstrumentSerializer(
    final OutputStream inStream)
  {
    this.stream =
      Objects.requireNonNull(inStream, "stream");
  }

  @Override
  public void execute(
    final ARI1InstrumentDescription description)
  {
    final var mapper = ARI1JMappers.mapper();
    mapper.writeValue(
      this.stream,
      ARI1JInstrumentDescriptions.toJson(description)
    );
  }

  @Override
  public void close()
    throws IOException
  {
    this.stream.close();
  }
}
