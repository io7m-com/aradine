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

package com.io7m.aradine.ensemble.internal.model;

import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.instrument.ARInstrumentInstanceID;
import com.io7m.aradine.api.instrument.ARInstrumentType;
import com.io7m.aradine.api.ports.ARPort;
import com.io7m.aradine.database.api.ARDBTransactionType;
import com.io7m.aradine.ensemble.internal.graph.AREnsGraphType;
import com.io7m.aradine.instrument.loader.api.ARInstrumentLoaderFactoryType;
import com.io7m.aradine.instrument.loader.api.ARInstrumentLoaderServicesConstructorType;
import com.io7m.aradine.instrument.loader.api.ARInstrumentPortAssignerType;
import com.io7m.aradine.inventory.api.ARInventoryType;

/**
 * The context of a model command.
 */

public interface AREnsModelCommandContextType
{
  /**
   * @return The new graph
   */

  AREnsGraphType graph();

  /**
   * @return The database transaction
   */

  ARDBTransactionType databaseTransaction();

  /**
   * @return The inventory
   */

  ARInventoryType inventory();

  /**
   * @return The instrument loaders
   */

  ARInstrumentLoaderFactoryType instrumentLoaders();

  /**
   * @param instanceID The instrument instance ID
   *
   * @return The instrument loader services constructor
   */

  ARInstrumentLoaderServicesConstructorType instrumentServicesConstructor(
    ARInstrumentInstanceID instanceID);

  /**
   * @return The instrument port assigner
   */

  ARInstrumentPortAssignerType instrumentPortAssigner();

  /**
   * Mark an instrument to be registered.
   *
   * @param instrument The instrument
   *
   * @throws ARException On errors
   */

  void instrumentRegister(
    ARInstrumentType instrument)
    throws ARException;

  /**
   * Mark an instrument port to be registered.
   *
   * @param port The port
   *
   * @throws ARException On errors
   */

  void instrumentPortRegister(
    ARPort port)
    throws ARException;

  /**
   * Mark an instrument port to be deregistered.
   *
   * @param port The port
   *
   * @throws ARException On errors
   */

  void instrumentPortDeregister(
    ARPort port)
    throws ARException;

  /**
   * Mark an instrument to be deregistered.
   *
   * @param instrument The instrument
   *
   * @throws ARException On errors
   */

  void instrumentDeregister(
    ARInstrumentType instrument)
    throws ARException;

  /**
   * Get an instrument.
   *
   * @param instrumentInstanceID The instance ID
   *
   * @return The instrument
   *
   * @throws ARException On errors
   */

  ARInstrumentType instrumentGet(
    ARInstrumentInstanceID instrumentInstanceID)
    throws ARException;
}
