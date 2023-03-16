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


package com.io7m.aradine.instrument.spi1.xml.internal;

import com.io7m.anethum.common.ParseException;
import com.io7m.anethum.common.ParseStatus;
import com.io7m.aradine.instrument.spi1.ARI1DocumentationType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentDescriptionType;
import com.io7m.aradine.instrument.spi1.ARI1ParagraphContentType;
import com.io7m.aradine.instrument.spi1.ARI1ParagraphType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterId;
import com.io7m.aradine.instrument.spi1.ARI1PortDescriptionType;
import com.io7m.aradine.instrument.spi1.ARI1PortId;
import com.io7m.aradine.instrument.spi1.ARI1Version;
import com.io7m.aradine.instrument.spi1.ARI1VersionQualifier;
import com.io7m.aradine.instrument.spi1.xml.ARI1InstrumentParserType;
import com.io7m.aradine.instrument.spi1.xml.ARI1Schema;
import com.io7m.aradine.instrument.spi1.xml.jaxb.Documentation;
import com.io7m.aradine.instrument.spi1.xml.jaxb.Instrument;
import com.io7m.aradine.instrument.spi1.xml.jaxb.LinkType;
import com.io7m.aradine.instrument.spi1.xml.jaxb.Metadata;
import com.io7m.aradine.instrument.spi1.xml.jaxb.Paragraph;
import com.io7m.aradine.instrument.spi1.xml.jaxb.ParameterIntegerType;
import com.io7m.aradine.instrument.spi1.xml.jaxb.ParameterRealType;
import com.io7m.aradine.instrument.spi1.xml.jaxb.ParameterSampleMapType;
import com.io7m.aradine.instrument.spi1.xml.jaxb.Parameters;
import com.io7m.aradine.instrument.spi1.xml.jaxb.PortInputAudioType;
import com.io7m.aradine.instrument.spi1.xml.jaxb.PortInputNoteType;
import com.io7m.aradine.instrument.spi1.xml.jaxb.PortOutputAudioType;
import com.io7m.aradine.instrument.spi1.xml.jaxb.PortType;
import com.io7m.aradine.instrument.spi1.xml.jaxb.Ports;
import com.io7m.aradine.instrument.spi1.xml.jaxb.Version;
import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.jlexing.core.LexicalPositions;
import com.io7m.lanark.core.RDottedName;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.ValidationEventLocator;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static com.io7m.anethum.common.ParseSeverity.PARSE_ERROR;
import static com.io7m.anethum.common.ParseSeverity.PARSE_WARNING;
import static jakarta.xml.bind.ValidationEvent.ERROR;
import static jakarta.xml.bind.ValidationEvent.FATAL_ERROR;
import static jakarta.xml.bind.ValidationEvent.WARNING;

/**
 * An instrument parser.
 */

public final class ARI1InstrumentParser implements ARI1InstrumentParserType
{
  private static final ARI1DocumentationType DOCUMENTATION =
    new ARI1Documentation(List.of());

  private final URI source;
  private final InputStream stream;
  private final Consumer<ParseStatus> statusConsumer;
  private final ArrayList<ParseStatus> statusValues;
  private boolean failed;

  /**
   * An instrument parser.
   *
   * @param inSource         The source
   * @param inStream         The stream
   * @param inStatusConsumer The status consumer
   */

  public ARI1InstrumentParser(
    final URI inSource,
    final InputStream inStream,
    final Consumer<ParseStatus> inStatusConsumer)
  {
    this.source =
      Objects.requireNonNull(inSource, "source");
    this.stream =
      Objects.requireNonNull(inStream, "stream");

    this.statusValues =
      new ArrayList<>();
    this.statusConsumer =
      Objects.requireNonNull(inStatusConsumer, "statusConsumer");
  }

  private static ParseStatus createParseError(
    final String errorCode,
    final LexicalPosition<URI> lexical,
    final String message)
  {
    return ParseStatus.builder()
      .setSeverity(PARSE_ERROR)
      .setErrorCode(errorCode)
      .setLexical(lexical)
      .setMessage(message)
      .build();
  }

  private static LexicalPosition<URI> locatorLexical(
    final ValidationEventLocator locator)
  {
    try {
      return LexicalPosition.of(
        locator.getLineNumber(),
        locator.getColumnNumber(),
        Optional.of(locator.getURL().toURI())
      );
    } catch (final URISyntaxException e) {
      throw new IllegalStateException(e);
    }
  }

  private static ARI1InstrumentDescriptionType processInstrument(
    final Instrument raw)
  {
    final Map<String, String> meta =
      processMetadata(raw.getMetadata());
    final var version =
      processVersion(raw.getVersion());
    final var identifier =
      new RDottedName(raw.getIdentifier());
    final var parameters =
      processParameters(raw.getParameters());
    final var ports =
      processPorts(raw.getPorts());

    return new ARI1InstrumentDescription(
      identifier.value(),
      version,
      meta,
      parameters,
      ports
    );
  }

  private static Map<ARI1PortId, ARI1PortDescriptionType> processPorts(
    final Ports ports)
  {
    final var declarations =
      ports.getPortInputAudioOrPortInputNoteOrPortOutputAudio();
    final Map<ARI1PortId, ARI1PortDescriptionType> results =
      new HashMap<>(declarations.size());

    for (final var declaration : declarations) {
      final var id = new ARI1PortId((int) declaration.getID());
      if (declaration instanceof PortOutputAudioType) {
        processPortOutputAudio(results, declaration, id);
        continue;
      }

      if (declaration instanceof PortInputAudioType) {
        processPortInputAudio(results, declaration, id);
        continue;
      }

      if (declaration instanceof PortInputNoteType) {
        processPortInputNote(results, declaration, id);
        continue;
      }
    }

    return results;
  }

  private static void processPortOutputAudio(
    final Map<ARI1PortId, ARI1PortDescriptionType> results,
    final PortType declaration,
    final ARI1PortId id)
  {
    final var semantics = new HashSet<String>();
    for (final var sem : declaration.getPortSemantic()) {
      semantics.add(new RDottedName(sem.getValue()).value());
    }

    results.put(
      id,
      new ARI1PortOutputAudio(
        id,
        Set.copyOf(semantics),
        declaration.getLabel(),
        processDocumentation(declaration.getDocumentation())
      )
    );
  }

  private static ARI1DocumentationType processDocumentation(
    final Documentation documentation)
  {
    if (documentation == null) {
      return DOCUMENTATION;
    }

    final var paragraphs = documentation.getParagraph();
    if (paragraphs.isEmpty()) {
      return DOCUMENTATION;
    }

    final var results = new ArrayList<ARI1ParagraphType>();
    for (final var para : paragraphs) {
      results.add(processParagraph(para));
    }
    return new ARI1Documentation(List.copyOf(results));
  }

  private static ARI1ParagraphType processParagraph(
    final Paragraph para)
  {
    final var results = new ArrayList<ARI1ParagraphContentType>();
    final var content = para.getContent();
    for (final var c : content) {
      if (c instanceof LinkType link) {
        results.add(new ARI1Link(URI.create(link.getTarget()), link.getContent()));
      }
      if (c instanceof String text) {
        results.add(new ARI1Text(text));
      }
    }
    return new ARI1Paragraph(List.copyOf(results));
  }

  private static void processPortInputNote(
    final Map<ARI1PortId, ARI1PortDescriptionType> results,
    final PortType declaration,
    final ARI1PortId id)
  {
    final var semantics = new HashSet<String>();
    for (final var sem : declaration.getPortSemantic()) {
      semantics.add(new RDottedName(sem.getValue()).value());
    }

    results.put(
      id,
      new ARI1PortInputNote(
        id,
        Set.copyOf(semantics),
        declaration.getLabel(),
        processDocumentation(declaration.getDocumentation())
      )
    );
  }

  private static void processPortInputAudio(
    final Map<ARI1PortId, ARI1PortDescriptionType> results,
    final PortType declaration,
    final ARI1PortId id)
  {
    final var semantics = new HashSet<String>();
    for (final var sem : declaration.getPortSemantic()) {
      semantics.add(new RDottedName(sem.getValue()).value());
    }

    results.put(
      id,
      new ARI1PortInputAudio(
        id,
        Set.copyOf(semantics),
        declaration.getLabel(),
        processDocumentation(declaration.getDocumentation())
      )
    );
  }

  private static Map<ARI1ParameterId, ARI1ParameterDescriptionType> processParameters(
    final Parameters parameters)
  {
    final var declarations =
      parameters.getParameterIntegerOrParameterRealOrParameterSampleMap();
    final Map<ARI1ParameterId, ARI1ParameterDescriptionType> results =
      new HashMap<>(declarations.size());

    for (final var declaration : declarations) {
      final var id = new ARI1ParameterId((int) declaration.getID());
      if (declaration instanceof ParameterIntegerType i) {
        results.put(
          id,
          new ARI1ParameterInteger(
            id,
            i.getLabel(),
            i.getUnitOfMeasurement(),
            i.getValueDefault(),
            i.getValueMinimumInclusive(),
            i.getValueMaximumInclusive(),
            processDocumentation(declaration.getDocumentation())
          )
        );
        continue;
      }

      if (declaration instanceof ParameterRealType r) {
        results.put(
          id,
          new ARI1ParameterReal(
            id,
            r.getLabel(),
            r.getUnitOfMeasurement(),
            r.getValueDefault(),
            r.getValueMinimumInclusive(),
            r.getValueMaximumInclusive(),
            processDocumentation(declaration.getDocumentation())
          )
        );
        continue;
      }

      if (declaration instanceof ParameterSampleMapType sm) {
        results.put(
          id,
          new ARI1ParameterSampleMap(
            id,
            sm.getLabel(),
            processDocumentation(declaration.getDocumentation())
          )
        );
        continue;
      }
    }

    return results;
  }

  private static ARI1Version processVersion(
    final Version version)
  {
    final var qualifier = version.getQualifier();
    if (qualifier != null) {
      final var pQualifier = new ARI1VersionQualifier(qualifier);
      return new ARI1Version(
        (int) version.getMajor(),
        (int) version.getMinor(),
        (int) version.getPatch(),
        Optional.of(pQualifier)
      );
    }

    return new ARI1Version(
      (int) version.getMajor(),
      (int) version.getMinor(),
      (int) version.getPatch(),
      Optional.empty()
    );
  }

  private static Map<String, String> processMetadata(
    final Metadata metadata)
  {
    final var values = metadata.getMeta();
    final var results = new HashMap<String, String>(values.size());
    for (final var value : values) {
      final var dottedName = new RDottedName(value.getName());
      results.put(dottedName.value(), value.getContent());
    }
    return results;
  }

  @Override
  public ARI1InstrumentDescriptionType execute()
    throws ParseException
  {
    this.failed = false;
    this.statusValues.clear();

    try {
      final var schemas =
        SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      final var schema =
        schemas.newSchema(new StreamSource(ARI1Schema.openSchema()));

      final var context =
        JAXBContext.newInstance(
          "com.io7m.aradine.instrument.spi1.xml.jaxb");
      final var unmarshaller =
        context.createUnmarshaller();

      unmarshaller.setEventHandler(event -> {
        final var locator = event.getLocator();
        switch (event.getSeverity()) {
          case WARNING -> {
            this.publishWarning(
              "warn-xml",
              locatorLexical(locator),
              event.getMessage()
            );
          }
          case ERROR, FATAL_ERROR -> {
            this.publishError(
              "error-xml-validation",
              locatorLexical(locator),
              event.getMessage()
            );
          }
        }
        return true;
      });

      unmarshaller.setSchema(schema);

      final var streamSource =
        new StreamSource(this.stream, this.source.toString());

      final var raw =
        (Instrument) unmarshaller.unmarshal(streamSource);

      if (this.failed) {
        throw this.parseException();
      }
      return processInstrument(raw);
    } catch (final SAXException e) {
      this.publishError(
        "sax-exception",
        LexicalPositions.zero(),
        Objects.requireNonNullElse(e.getMessage(), e.getClass().getName())
      );
      throw this.parseException();
    } catch (final JAXBException e) {
      this.publishError(
        "jaxb-exception",
        LexicalPositions.zero(),
        Objects.requireNonNullElse(e.getMessage(), e.getClass().getName())
      );
      throw this.parseException();
    }
  }

  private ParseException parseException()
  {
    return new ParseException(
      "Parse failed.",
      List.copyOf(this.statusValues)
    );
  }

  @Override
  public void close()
    throws IOException
  {
    this.stream.close();
  }

  private void publishStatus(
    final ParseStatus status)
  {
    if (status.severity() == PARSE_ERROR) {
      this.failed = true;
    }

    this.statusValues.add(status);
    this.statusConsumer.accept(status);
  }

  private void publishError(
    final String errorCode,
    final LexicalPosition<URI> lex,
    final String message)
  {
    this.publishStatus(createParseError(errorCode, lex, message));
  }

  private void publishWarning(
    final String errorCode,
    final LexicalPosition<URI> lex,
    final String message)
  {
    final var status =
      ParseStatus.builder()
        .setErrorCode(errorCode)
        .setLexical(lex)
        .setSeverity(PARSE_WARNING)
        .setMessage(message)
        .build();

    this.statusValues.add(status);
    this.statusConsumer.accept(status);
  }
}
