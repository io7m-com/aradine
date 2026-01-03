/*
 * Copyright © 2026 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

package com.io7m.aradine.cmdline.internal;

import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.instrument.ARInstrumentID;
import com.io7m.lanark.core.RDottedName;
import com.io7m.quarrel.core.QException;
import com.io7m.quarrel.core.QValueConverterType;
import com.io7m.verona.core.Version;

import java.util.List;
import java.util.Optional;

/**
 * Value converter.
 */

public final class ARInstrumentIDConverter
  implements QValueConverterType<ARInstrumentID>
{
  /**
   * Value converter.
   */
  public ARInstrumentIDConverter()
  {

  }

  @Override
  public ARInstrumentID convertFromString(
    final String text)
    throws QException
  {
    try {
      return ARInstrumentID.parse(text);
    } catch (final ARException e) {
      throw new QException(
        e.getMessage(),
        e,
        e.errorCode(),
        e.attributes(),
        e.remediatingAction(),
        List.of()
      );
    }
  }

  @Override
  public String convertToString(
    final ARInstrumentID instrumentID)
  {
    return instrumentID.toString();
  }

  @Override
  public ARInstrumentID exampleValue()
  {
    return new ARInstrumentID(
      new RDottedName("com.io7m.aradine"),
      new RDottedName("com.io7m.aradine.example"),
      new Version(1, 2, 3, Optional.empty())
    );
  }

  @Override
  public String syntax()
  {
    return "<dotted-name>:<dotted-name>:<version>";
  }

  @Override
  public Class<ARInstrumentID> convertedClass()
  {
    return ARInstrumentID.class;
  }
}
