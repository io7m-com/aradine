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
import com.io7m.aradine.api.parameters.ARParameterDescriptionInteger;
import com.io7m.aradine.api.parameters.ARParameterID;
import com.io7m.aradine.api.parameters.ARParameterNumber;
import com.io7m.aradine.api.system.ARAudioSystemAttributes;
import com.io7m.aradine.ensemble.internal.model.AREnsInstrumentContext;
import com.io7m.aradine.ensemble.internal.model.AREnsParameterInteger;
import com.io7m.aradine.ensemble.internal.v1.context.AREnsSPI1InstrumentContextAdapter;
import com.io7m.aradine.ensemble.internal.v1.context.AREnsSPI1ParameterIntegerAdapter;
import com.io7m.aradine.instrument.spi1.ARI1ParameterNumber;
import com.io7m.lanark.core.RDottedName;
import com.io7m.verona.core.Version;

import java.util.Map;

public final class ARI1ParameterIntegerTest
  extends ARI1ParameterIntegerContract<AREnsSPI1ParameterIntegerAdapter>
{
  @Override
  protected AREnsSPI1ParameterIntegerAdapter createParameter(
    final ARI1ParameterNumber id,
    final long valueMinimum,
    final long valueMaximum,
    final long valueDefault)
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

    return new AREnsSPI1ParameterIntegerAdapter(
      context,
      new AREnsParameterInteger(
        baseContext,
        new ARParameterDescriptionInteger(
          instanceId,
          number,
          ARParameterID.ofInstanceParameter(instanceId, number),
          "L",
          new RDottedName("com.io7m.example"),
          valueMinimum,
          valueMaximum,
          valueDefault
        )
      )
    );
  }

  @Override
  protected void setValue(
    final AREnsSPI1ParameterIntegerAdapter parameter,
    final int time,
    final long value)
  {
    parameter.valueChange(time, value);
  }

  @Override
  protected void clearChanges(
    final AREnsSPI1ParameterIntegerAdapter parameter)
  {
    parameter.valueChangesClear();
  }
}
