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

package com.io7m.aradine.ensemble.internal.v1.commands;

import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.instrument.ARInstrumentID;
import com.io7m.aradine.api.instrument.ARInstrumentInstanceID;
import com.io7m.aradine.api.instrument.ARInstrumentType;
import com.io7m.aradine.ensemble.internal.model.AREnsCommandUndoable;
import com.io7m.aradine.ensemble.internal.model.AREnsCommandUndoableType;
import com.io7m.aradine.ensemble.internal.model.AREnsModelCommandContextType;
import com.io7m.aradine.instrument.loader.api.ARInstrumentLoaderFactoryType;
import com.io7m.aradine.inventory.api.ARInventoryType;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/**
 * An instrument load command.
 */

public enum AREnsModelCommandInstrumentLoad
  implements AREnsModelCommand1Type<
  AREnsModelCommandInstrumentLoadParameters,
  AREnsModelCommandInstrumentLoadState>
{
  /**
   * An instrument load command.
   */

  INSTANCE;

  private static ARException errorInstrumentNotInstalled(
    final AREnsModelCommandInstrumentLoadParameters parameters)
  {
    return new ARException(
      "Instrument is not installed.",
      "error-instrument-not-installed",
      Map.ofEntries(
        Map.entry("InstrumentID", parameters.instrumentID().toString())
      ),
      Optional.empty()
    );
  }

  private static Path instrumentFile(
    final AREnsModelCommandInstrumentLoadParameters parameters,
    final ARInventoryType inventory,
    final ARInstrumentID instrumentID)
    throws ARException
  {
    final var instrumentFileOpt =
      inventory.instrumentFile(instrumentID);
    if (instrumentFileOpt.isEmpty()) {
      throw errorInstrumentNotInstalled(parameters);
    }
    return instrumentFileOpt.get();
  }

  private static void instrumentPortsRegister(
    final AREnsModelCommandContextType context,
    final ARInstrumentType instrument)
    throws ARException
  {
    final var ports = instrument.description().ports();
    for (final var entry : ports.entrySet()) {
      final var port = entry.getValue();
      context.instrumentPortRegister(port);
    }
  }

  private static ARInstrumentType instrumentLoadAndRegister(
    final AREnsModelCommandContextType context,
    final ARInstrumentInstanceID instanceID,
    final ARInstrumentLoaderFactoryType loaders,
    final Path file)
    throws ARException
  {
    final var services =
      context.instrumentServicesConstructor(instanceID);

    final ARInstrumentType instrument;
    try (var loader = loaders.createLoader(services, file)) {
      instrument = loader.execute(context.instrumentPortAssigner(), instanceID);
    }
    context.instrumentRegister(instrument);

    instrumentPortsRegister(context, instrument);
    return instrument;
  }

  @Override
  public AREnsCommandUndoableType<AREnsModelCommandInstrumentLoadState>
  execute(
    final AREnsModelCommandContextType context,
    final AREnsModelCommandInstrumentLoadParameters parameters)
    throws ARException
  {
    final var inventory =
      context.inventory();
    final var loaders =
      context.instrumentLoaders();

    final var instrumentID =
      parameters.instrumentID();
    final var instanceID =
      parameters.instanceID();

    final var file = instrumentFile(parameters, inventory, instrumentID);
    instrumentLoadAndRegister(context, instanceID, loaders, file);

    return new AREnsCommandUndoable<>(
      new AREnsModelCommandInstrumentLoadState(instanceID, instrumentID)
    );
  }

  @Override
  public void undo(
    final AREnsModelCommandContextType context,
    final AREnsModelCommandInstrumentLoadState state)
    throws ARException
  {
    final var instrument =
      context.instrumentGet(state.instanceID());

    final var ports = instrument.description().ports();
    for (final var entry : ports.entrySet()) {
      final var port = entry.getValue();
      context.instrumentPortDeregister(port);
    }

    context.instrumentDeregister(instrument);
  }

  @Override
  public void redo(
    final AREnsModelCommandContextType context,
    final AREnsModelCommandInstrumentLoadState state)
    throws ARException
  {
    this.execute(
      context,
      new AREnsModelCommandInstrumentLoadParameters(
        state.instanceID(),
        state.instrumentID()
      )
    );
  }

  @Override
  public String description()
  {
    return "Load instrument";
  }
}
