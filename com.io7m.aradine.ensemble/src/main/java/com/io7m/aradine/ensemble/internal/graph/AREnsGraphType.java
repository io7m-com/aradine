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

package com.io7m.aradine.ensemble.internal.graph;

import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.instrument.ARInstrumentInstanceID;
import com.io7m.aradine.api.instrument.ARInstrumentReference;
import com.io7m.aradine.api.ports.ARPortDescription;
import com.io7m.aradine.api.ports.ARPortID;

import java.util.List;

/**
 * The port/instrument graph.
 */

public interface AREnsGraphType
{
  /**
   * @return The graph steps
   */

  List<AREnsGraphExecutionStep> executionSteps();

  /**
   * Deregister an instrument.
   *
   * @param instrument The instrument
   *
   * @throws ARException On errors
   */

  void instrumentDeregister(
    ARInstrumentInstanceID instrument)
    throws ARException;

  /**
   * Register an instrument.
   *
   * @param instrument The instrument
   *
   * @throws ARException On errors
   */

  void instrumentRegister(
    ARInstrumentReference instrument)
    throws ARException;

  /**
   * Check if an instrument is registered.
   *
   * @param instrument The instrument ID
   *
   * @return {@code true} if the instrument is registered
   */

  boolean instrumentIsRegistered(
    ARInstrumentInstanceID instrument);

  /**
   * Connect two ports.
   *
   * @param portSourceID The source port
   * @param portTargetID The target port
   *
   * @throws ARException On errors
   */

  void portConnect(
    ARPortID portSourceID,
    ARPortID portTargetID)
    throws ARException;

  /**
   * Disconnect two ports.
   *
   * @param portSourceID The source port
   * @param portTargetID The target port
   *
   * @throws ARException On errors
   */

  void portDisconnect(
    ARPortID portSourceID,
    ARPortID portTargetID)
    throws ARException;

  /**
   * Register a port.
   *
   * @param port The port
   *
   * @throws ARException On errors
   */

  void portRegister(
    ARPortDescription port)
    throws ARException;

  /**
   * Deregister a port.
   *
   * @param port The port
   *
   * @throws ARException On errors
   */

  void portDeregister(
    ARPortDescription port)
    throws ARException;

  /**
   * Check if a port is registered.
   *
   * @param port The port ID
   *
   * @return {@code true} if the port is registered
   */

  boolean portIsRegistered(
    ARPortID port);

  /**
   * Check if two ports are connected. Direction is significant (the function
   * is anticommutative).
   *
   * @param portSourceID The source port
   * @param portTargetID The target port
   *
   * @return {@code true} if the ports are connected
   *
   * @throws ARException On errors
   */

  boolean portIsConnected(
    ARPortID portSourceID,
    ARPortID portTargetID)
    throws ARException;
}
