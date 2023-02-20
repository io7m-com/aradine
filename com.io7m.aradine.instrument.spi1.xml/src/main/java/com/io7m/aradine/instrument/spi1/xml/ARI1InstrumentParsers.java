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


package com.io7m.aradine.instrument.spi1.xml;

import com.io7m.anethum.common.ParseStatus;
import com.io7m.aradine.instrument.spi1.xml.internal.ARI1InstrumentParser;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;
import java.net.URI;
import java.util.function.Consumer;

/**
 * The default implementation of the parser factory.
 */

@Component
public final class ARI1InstrumentParsers
  implements ARI1InstrumentParserFactoryType
{
  /**
   * The default implementation of the parser factory.
   */

  public ARI1InstrumentParsers()
  {

  }

  @Override
  public ARI1InstrumentParserType createParserWithContext(
    final Void context,
    final URI source,
    final InputStream stream,
    final Consumer<ParseStatus> statusConsumer)
  {
    return new ARI1InstrumentParser(source, stream, statusConsumer);
  }
}
