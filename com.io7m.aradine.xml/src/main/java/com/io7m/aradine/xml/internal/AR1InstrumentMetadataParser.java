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

import com.io7m.aradine.instrument.metadata.ARInstrumentID;
import com.io7m.aradine.instrument.metadata.ARInstrumentLicenseID;
import com.io7m.aradine.instrument.metadata.ARInstrumentMetadata;
import com.io7m.aradine.instrument.metadata.ARInstrumentVersion;
import com.io7m.aradine.xml.ARInstrumentMetadataSchemas;
import com.io7m.blackthorne.api.BTElementHandlerConstructorType;
import com.io7m.blackthorne.api.BTElementHandlerType;
import com.io7m.blackthorne.api.BTElementParsingContextType;
import com.io7m.blackthorne.api.BTQualifiedName;
import org.xml.sax.Attributes;

import java.net.URI;
import java.util.Map;

public final class AR1InstrumentMetadataParser
  implements BTElementHandlerType<Object, ARInstrumentMetadata>
{
  private final ARInstrumentMetadata.Builder metadata;

  public AR1InstrumentMetadataParser(
    final BTElementParsingContextType context)
  {
    this.metadata = ARInstrumentMetadata.builder();
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ?>>
  onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return Map.ofEntries(
      Map.entry(
        ARInstrumentMetadataSchemas.instrument1Element("Version"),
        AR1VersionParser::new
      ),
      Map.entry(
        ARInstrumentMetadataSchemas.instrument1Element("Authors"),
        AR1AuthorsParser::new
      ),
      Map.entry(
        ARInstrumentMetadataSchemas.instrument1Element("InputPorts"),
        AR1InstrumentInputPortsParser::new
      ),
      Map.entry(
        ARInstrumentMetadataSchemas.instrument1Element("OutputPorts"),
        AR1InstrumentOutputPortsParser::new
      )
    );
  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
  {
    this.metadata.setId(
      ARInstrumentID.of(attributes.getValue("id")));
    this.metadata.setReadableName(
      attributes.getValue("readableName"));
    this.metadata.setLicenseId(
      ARInstrumentLicenseID.of(attributes.getValue("licenseId")));

    final var siteText = attributes.getValue("site");
    if (siteText != null) {
      this.metadata.setSite(URI.create(siteText));
    }
    final var iconText = attributes.getValue("icon");
    if (iconText != null) {
      this.metadata.setIcon(URI.create(iconText));
    }
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final Object result)
  {
    if (result instanceof ARInstrumentVersion) {
      this.metadata.setVersion((ARInstrumentVersion) result);
      return;
    }
    if (result instanceof AR1InstrumentAuthors) {
      this.metadata.addAllAuthors(((AR1InstrumentAuthors) result).authors());
      return;
    }
    if (result instanceof AR1InstrumentOutputPorts) {
      this.metadata.addAllOutputs(((AR1InstrumentOutputPorts) result).ports());
      return;
    }
    if (result instanceof AR1InstrumentInputPorts) {
      this.metadata.addAllInputs(((AR1InstrumentInputPorts) result).ports());
      return;
    }

    throw new IllegalStateException(
      String.format("Unexpected element: %s", result)
    );
  }

  @Override
  public ARInstrumentMetadata onElementFinished(
    final BTElementParsingContextType context)
  {
    return this.metadata.build();
  }
}
