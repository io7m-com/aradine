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

import com.io7m.anethum.common.SerializeException;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentDescriptionType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionIntegerType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionRealType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionSampleMapType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterId;
import com.io7m.aradine.instrument.spi1.ARI1PortDescriptionInputAudioType;
import com.io7m.aradine.instrument.spi1.ARI1PortDescriptionInputNoteType;
import com.io7m.aradine.instrument.spi1.ARI1PortDescriptionOutputAudioType;
import com.io7m.aradine.instrument.spi1.ARI1PortDescriptionType;
import com.io7m.aradine.instrument.spi1.ARI1PortId;
import com.io7m.aradine.instrument.spi1.ARI1Version;
import com.io7m.aradine.instrument.spi1.xml.ARI1InstrumentSerializerType;
import com.io7m.aradine.instrument.spi1.xml.jaxb.Instrument;
import com.io7m.aradine.instrument.spi1.xml.jaxb.Meta;
import com.io7m.aradine.instrument.spi1.xml.jaxb.Metadata;
import com.io7m.aradine.instrument.spi1.xml.jaxb.ParameterIntegerType;
import com.io7m.aradine.instrument.spi1.xml.jaxb.ParameterRealType;
import com.io7m.aradine.instrument.spi1.xml.jaxb.ParameterSampleMapType;
import com.io7m.aradine.instrument.spi1.xml.jaxb.Parameters;
import com.io7m.aradine.instrument.spi1.xml.jaxb.PortInputAudioType;
import com.io7m.aradine.instrument.spi1.xml.jaxb.PortInputNoteType;
import com.io7m.aradine.instrument.spi1.xml.jaxb.PortOutputAudioType;
import com.io7m.aradine.instrument.spi1.xml.jaxb.PortSemantic;
import com.io7m.aradine.instrument.spi1.xml.jaxb.Ports;
import com.io7m.aradine.instrument.spi1.xml.jaxb.Version;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static java.lang.Boolean.TRUE;

/**
 * An instrument serializer.
 */

public final class ARI1InstrumentSerializer
  implements ARI1InstrumentSerializerType
{
  private final OutputStream stream;

  /**
   * An instrument serializer.
   *
   * @param inStream The stream
   */

  public ARI1InstrumentSerializer(
    final OutputStream inStream)
  {
    this.stream =
      Objects.requireNonNull(inStream, "stream");
  }

  private static Instrument processInstrument(
    final ARI1InstrumentDescriptionType value)
  {
    final var instrument = new Instrument();
    instrument.setIdentifier(value.identifier());
    instrument.setVersion(processVersion(value.version()));
    instrument.setMetadata(processMetadata(value.metadata()));
    instrument.setParameters(processParameters(value.parameters()));
    instrument.setPorts(processPorts(value.ports()));
    return instrument;
  }

  private static Ports processPorts(
    final Map<ARI1PortId, ARI1PortDescriptionType> ports)
  {
    final var r = new Ports();
    final var p = r.getPortInputAudioOrPortInputNoteOrPortOutputAudio();

    final var ks = new ArrayList<>(ports.keySet());
    Collections.sort(ks);

    for (final var k : ks) {
      final var value = ports.get(k);
      if (value instanceof ARI1PortDescriptionOutputAudioType v) {
        p.add(processPortOutputAudio(v));
        continue;
      }
      if (value instanceof ARI1PortDescriptionInputAudioType v) {
        p.add(processPortInputAudio(v));
        continue;
      }
      if (value instanceof ARI1PortDescriptionInputNoteType v) {
        p.add(processPortInputNote(v));
        continue;
      }
    }

    return r;
  }

  private static PortInputNoteType processPortInputNote(
    final ARI1PortDescriptionInputNoteType value)
  {
    final var m = new PortInputNoteType();
    m.setID(Integer.toUnsignedLong(value.id().value()));
    m.setLabel(value.label());

    final var s = m.getPortSemantic();
    final var ss = new ArrayList<>(value.semantics());
    Collections.sort(ss);

    for (final var pp : ss) {
      final var q = new PortSemantic();
      q.setValue(pp);
      s.add(q);
    }
    return m;
  }

  private static PortInputAudioType processPortInputAudio(
    final ARI1PortDescriptionInputAudioType value)
  {
    final var m = new PortInputAudioType();
    m.setID(Integer.toUnsignedLong(value.id().value()));
    m.setLabel(value.label());

    final var s = m.getPortSemantic();
    final var ss = new ArrayList<>(value.semantics());
    Collections.sort(ss);

    for (final var pp : ss) {
      final var q = new PortSemantic();
      q.setValue(pp);
      s.add(q);
    }
    return m;
  }

  private static PortOutputAudioType processPortOutputAudio(
    final ARI1PortDescriptionOutputAudioType value)
  {
    final var m = new PortOutputAudioType();
    m.setID(Integer.toUnsignedLong(value.id().value()));
    m.setLabel(value.label());

    final var s = m.getPortSemantic();
    final var ss = new ArrayList<>(value.semantics());
    Collections.sort(ss);

    for (final var pp : ss) {
      final var q = new PortSemantic();
      q.setValue(pp);
      s.add(q);
    }
    return m;
  }

  private static Parameters processParameters(
    final Map<ARI1ParameterId, ARI1ParameterDescriptionType> parameters)
  {
    final var r = new Parameters();
    final var p = r.getParameterIntegerOrParameterRealOrParameterSampleMap();

    final var ks = new ArrayList<>(parameters.keySet());
    Collections.sort(ks);

    for (final var k : ks) {
      final var value = parameters.get(k);
      if (value instanceof ARI1ParameterDescriptionSampleMapType i) {
        p.add(processParameterSampleMap(i));
        continue;
      }
      if (value instanceof ARI1ParameterDescriptionIntegerType i) {
        p.add(processParameterInteger(i));
        continue;
      }
      if (value instanceof ARI1ParameterDescriptionRealType i) {
        p.add(processParameterReal(i));
        continue;
      }
    }
    return r;
  }

  private static ParameterRealType processParameterReal(
    final ARI1ParameterDescriptionRealType i)
  {
    final var m = new ParameterRealType();
    m.setID(Integer.toUnsignedLong(i.id().value()));
    m.setLabel(i.label());
    m.setUnitOfMeasurement(i.unitOfMeasurement());
    m.setValueDefault(i.valueDefault());
    m.setValueMinimumInclusive(i.valueMinimum());
    m.setValueMaximumInclusive(i.valueMaximum());
    return m;
  }

  private static ParameterIntegerType processParameterInteger(
    final ARI1ParameterDescriptionIntegerType i)
  {
    final var m = new ParameterIntegerType();
    m.setID(Integer.toUnsignedLong(i.id().value()));
    m.setLabel(i.label());
    m.setUnitOfMeasurement(i.unitOfMeasurement());
    m.setValueDefault(i.valueDefault());
    m.setValueMinimumInclusive(i.valueMinimum());
    m.setValueMaximumInclusive(i.valueMaximum());
    return m;
  }

  private static ParameterSampleMapType processParameterSampleMap(
    final ARI1ParameterDescriptionSampleMapType i)
  {
    final var m = new ParameterSampleMapType();
    m.setID(Integer.toUnsignedLong(i.id().value()));
    m.setLabel(i.label());
    return m;
  }

  private static Metadata processMetadata(
    final Map<String, String> metadata)
  {
    final var r = new Metadata();
    final var rm = r.getMeta();

    for (final var entry : metadata.entrySet()) {
      final var m = new Meta();
      m.setName(entry.getKey());
      m.setContent(entry.getValue());
      rm.add(m);
    }
    return r;
  }

  private static Version processVersion(
    final ARI1Version version)
  {
    final var r = new Version();
    r.setMajor(version.major());
    r.setMinor(version.minor());
    r.setPatch(version.patch());
    version.qualifier().ifPresent(q -> r.setQualifier(q.text()));
    return r;
  }

  @Override
  public void execute(
    final ARI1InstrumentDescriptionType value)
    throws SerializeException
  {
    try {
      final var context =
        JAXBContext.newInstance(
          "com.io7m.aradine.instrument.spi1.xml.jaxb");
      final var marshaller =
        context.createMarshaller();

      marshaller.setProperty("jaxb.formatted.output", TRUE);
      marshaller.marshal(processInstrument(value), this.stream);
    } catch (final JAXBException e) {
      throw new SerializeException(e.getMessage(), e);
    }
  }

  @Override
  public void close()
    throws IOException
  {
    this.stream.close();
  }
}
