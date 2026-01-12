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

import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.audiosystem.ARAudioSystemUsableType;
import com.io7m.aradine.api.instrument.ARInstrumentDescription;
import com.io7m.aradine.api.instrument.ARInstrumentID;
import com.io7m.aradine.api.instrument.ARInstrumentInstanceID;
import com.io7m.aradine.api.instrument.ARInstrumentRole;
import com.io7m.aradine.api.parameters.ARParameterDescriptionReal;
import com.io7m.aradine.api.parameters.ARParameterID;
import com.io7m.aradine.api.parameters.ARParameterNumber;
import com.io7m.aradine.audiosystem.main.ARAudioSystem;
import com.io7m.aradine.audiosystem.zero.ARAudioBackendZeroProvider;
import com.io7m.aradine.ensemble.internal.model.AREnsInstrumentContext;
import com.io7m.aradine.ensemble.internal.model.AREnsParameterReal;
import com.io7m.aradine.ensemble.internal.v1.context.AREnsSPI1InstrumentContextAdapter;
import com.io7m.aradine.ensemble.internal.v1.context.AREnsSPI1ParameterRealAdapter;
import com.io7m.aradine.instrument.spi1.ARI1ParameterNumber;
import com.io7m.lanark.core.RDottedName;
import com.io7m.verona.core.Version;

import java.util.List;
import java.util.Map;

public final class ARI1ParameterRealTest
  extends ARI1ParameterRealContract<AREnsSPI1ParameterRealAdapter>
{
  private static ARAudioSystemUsableType createAudioSystem()
    throws ARException
  {
    final var provider =
      new ARAudioBackendZeroProvider();

    return ARAudioSystem.open(
      List.of(provider)
    );
  }

  @Override
  protected AREnsSPI1ParameterRealAdapter createParameter(
    final ARI1ParameterNumber id,
    final double valueMinimum,
    final double valueMaximum,
    final double valueDefault)
  {
    final ARAudioSystemUsableType audioSystem;
    try {
      audioSystem = createAudioSystem();
    } catch (final ARException e) {
      throw new RuntimeException(e);
    }

    final var instanceId =
      ARInstrumentInstanceID.random();

    final var baseContext =
      AREnsInstrumentContext.create(
        audioSystem,
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
        audioSystem,
        baseContext
      );

    final var number =
      new ARParameterNumber(id.value());

    return new AREnsSPI1ParameterRealAdapter(
      context,
      new AREnsParameterReal(
        baseContext,
        new ARParameterDescriptionReal(
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
    final AREnsSPI1ParameterRealAdapter parameter,
    final int time,
    final double value)
  {
    parameter.valueChange(time, value);
  }

  @Override
  protected void clearChanges(
    final AREnsSPI1ParameterRealAdapter parameter)
  {
    parameter.valueChangesClear();
  }
}
