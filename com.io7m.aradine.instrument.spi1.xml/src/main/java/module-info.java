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

import com.io7m.aradine.instrument.spi1.xml.ARI1InstrumentParserFactoryType;
import com.io7m.aradine.instrument.spi1.xml.ARI1InstrumentParsers;

/**
 * Modular programmable synthesis (SPI 1 XML code)
 */

module com.io7m.aradine.instrument.spi1.xml
{
  requires static org.osgi.annotation.bundle;
  requires static org.osgi.annotation.versioning;
  requires static org.osgi.service.component.annotations;

  requires com.io7m.anethum.api;
  requires com.io7m.anethum.common;
  requires com.io7m.aradine.instrument.spi1;
  requires com.io7m.jlexing.core;
  requires com.io7m.lanark.core;
  requires jakarta.xml.bind;

  provides ARI1InstrumentParserFactoryType
    with ARI1InstrumentParsers;

  exports com.io7m.aradine.instrument.spi1.xml;
}
