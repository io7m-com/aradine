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

import com.io7m.aradine.instrument.metadata.ARInstrumentDocumentationParagraph;
import com.io7m.blackthorne.api.BTElementHandlerType;
import com.io7m.blackthorne.api.BTElementParsingContextType;
import org.xml.sax.Attributes;

public final class AR1DocumentationParagraphParser
  implements BTElementHandlerType<Object, ARInstrumentDocumentationParagraph>
{
  private String language;
  private String text;

  public AR1DocumentationParagraphParser(
    final BTElementParsingContextType context)
  {
    this.text = "";
    this.language = "en";
  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
  {
    this.language = attributes.getValue("language");
  }

  @Override
  public void onCharacters(
    final BTElementParsingContextType context,
    final char[] data,
    final int offset,
    final int length)
  {
    // CHECKSTYLE:OFF
    this.text = new String(data, offset, length);
    // CHECKSTYLE:ON
  }

  @Override
  public ARInstrumentDocumentationParagraph onElementFinished(
    final BTElementParsingContextType context)
  {
    return ARInstrumentDocumentationParagraph.builder()
      .setLanguage(this.language)
      .setText(this.text)
      .build();
  }
}
