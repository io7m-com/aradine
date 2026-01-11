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

package com.io7m.aradine.ensemble.internal.v1.context;

import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.instrument.ARInstrumentDescription;
import com.io7m.aradine.api.instrument.ARInstrumentExecutableType;
import com.io7m.aradine.api.parameters.ARParameterID;
import com.io7m.aradine.api.ports.ARPortID;
import com.io7m.aradine.ensemble.internal.model.AREnsInstrumentType;
import com.io7m.aradine.ensemble.internal.model.AREnsParameterType;
import com.io7m.aradine.ensemble.internal.model.AREnsPortInstanceType;
import com.io7m.jdeferthrow.core.ExceptionTracker;

import java.util.Map;
import java.util.Objects;

/**
 * A loaded SPI 1 instrument.
 */

public final class AREnsSPI1Instrument
  implements AREnsInstrumentType
{
  private final ARInstrumentExecutableType executable;
  private final AREnsSPI1InstrumentContextAdapter context;

  /**
   * Construct an instrument.
   *
   * @param inExecutable The executable
   * @param inContext    The instrument context
   */

  public AREnsSPI1Instrument(
    final ARInstrumentExecutableType inExecutable,
    final AREnsSPI1InstrumentContextAdapter inContext)
  {
    this.executable =
      Objects.requireNonNull(inExecutable, "Executable");
    this.context =
      Objects.requireNonNull(inContext, "Context");
  }

  @Override
  public String toString()
  {
    return "[AREnsSPI1Instrument %s]".formatted(this.description().instanceId());
  }

  @Override
  public Map<ARPortID, AREnsPortInstanceType> ports()
  {
    return this.context.baseContext().ports();
  }

  @Override
  public Map<ARParameterID, AREnsParameterType> parameters()
  {
    return this.context.baseContext().parameters();
  }

  @Override
  public ARInstrumentDescription description()
  {
    return this.executable.description();
  }

  @Override
  public void close()
    throws ARException
  {
    final var exceptions =
      new ExceptionTracker<ARException>();

    try {
      this.executable.close();
    } catch (final ARException e) {
      exceptions.addException(e);
    }

    try {
      this.context.close();
    } catch (final ARException e) {
      exceptions.addException(e);
    }

    exceptions.throwIfNecessary();
  }
}
