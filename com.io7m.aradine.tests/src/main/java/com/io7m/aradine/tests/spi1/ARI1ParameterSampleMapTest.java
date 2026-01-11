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


package com.io7m.aradine.tests.spi1;

import com.io7m.aradine.api.instrument.ARInstrumentDescription;
import com.io7m.aradine.api.instrument.ARInstrumentID;
import com.io7m.aradine.api.instrument.ARInstrumentInstanceID;
import com.io7m.aradine.api.instrument.ARInstrumentRole;
import com.io7m.aradine.api.parameters.ARParameterDescriptionSampleMap;
import com.io7m.aradine.api.parameters.ARParameterID;
import com.io7m.aradine.api.parameters.ARParameterNumber;
import com.io7m.aradine.api.sample_map.ARSampleMapInstanceID;
import com.io7m.aradine.api.system.ARAudioSystemAttributes;
import com.io7m.aradine.ensemble.internal.model.AREnsInstrumentContext;
import com.io7m.aradine.ensemble.internal.model.AREnsParameterSampleMap;
import com.io7m.aradine.ensemble.internal.v1.context.AREnsSPI1InstrumentContextAdapter;
import com.io7m.aradine.ensemble.internal.v1.context.AREnsSPI1ParameterSampleMapAdapter;
import com.io7m.aradine.instrument.spi1.ARI1ParameterNumber;
import com.io7m.aradine.instrument.spi1.ARI1SampleMapInstanceID;
import com.io7m.lanark.core.RDottedName;
import com.io7m.verona.core.Version;

import java.util.Map;

public final class ARI1ParameterSampleMapTest
  extends ARI1ParameterSampleMapContract<AREnsSPI1ParameterSampleMapAdapter>
{
  @Override
  protected AREnsSPI1ParameterSampleMapAdapter createParameter(
    final ARI1ParameterNumber id,
    final ARI1SampleMapInstanceID valueDefault)
  {
    final var audioSystemAttributes =
      new ARAudioSystemAttributes();
    final var instanceId =
      ARInstrumentInstanceID.random();

    final var baseContext =
      AREnsInstrumentContext.create(
        audioSystemAttributes,
        new ARInstrumentDescription(
          instanceId,
          new ARInstrumentID(
            new RDottedName("com.io7m.aradine"),
            new RDottedName("com.io7m.aradine"),
            Version.of(1, 0, 0)
          ),
          Map.of(),
          Map.of(),
          ARInstrumentRole.AR_INSTRUMENT
        )
      );

    final var context =
      AREnsSPI1InstrumentContextAdapter.wrap(
        audioSystemAttributes,
        baseContext
      );

    final var number =
      new ARParameterNumber(id.value());

    return new AREnsSPI1ParameterSampleMapAdapter(
      context,
      new AREnsParameterSampleMap(
        baseContext,
        new ARParameterDescriptionSampleMap(
          instanceId,
          number,
          ARParameterID.ofInstanceParameter(instanceId, number),
          "L"
        ),
        new ARSampleMapInstanceID(valueDefault.value())
      )
    );
  }

  @Override
  protected void setValue(
    final AREnsSPI1ParameterSampleMapAdapter parameter,
    final int time,
    final ARI1SampleMapInstanceID value)
  {
    parameter.valueChange(time, new ARSampleMapInstanceID(value.value()));
  }

  @Override
  protected void clearChanges(
    final AREnsSPI1ParameterSampleMapAdapter parameter)
  {
    parameter.valueChangesClear();
  }
}
