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

import com.io7m.aradine.instrument.metadata.ARInstrumentDocumentation;
import com.io7m.aradine.instrument.metadata.ARInstrumentDocumentationParagraph;
import com.io7m.aradine.xml.ARInstrumentMetadataSchemas;
import com.io7m.blackthorne.api.BTElementHandlerConstructorType;
import com.io7m.blackthorne.api.BTElementHandlerType;
import com.io7m.blackthorne.api.BTElementParsingContextType;
import com.io7m.blackthorne.api.BTQualifiedName;

import java.util.Map;

public final class AR1DocumentationParser
  implements BTElementHandlerType<ARInstrumentDocumentationParagraph, ARInstrumentDocumentation>
{
  private final ARInstrumentDocumentation.Builder documentation;

  public AR1DocumentationParser(
    final BTElementParsingContextType context)
  {
    this.documentation = ARInstrumentDocumentation.builder();
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ? extends ARInstrumentDocumentationParagraph>>
  onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return Map.ofEntries(
      Map.entry(
        ARInstrumentMetadataSchemas.instrument1Element("Paragraph"),
        AR1DocumentationParagraphParser::new
      )
    );
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final ARInstrumentDocumentationParagraph result)
  {
    this.documentation.addParagraphs(result);
  }

  @Override
  public ARInstrumentDocumentation onElementFinished(
    final BTElementParsingContextType context)
  {
    return this.documentation.build();
  }
}
