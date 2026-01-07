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
import com.io7m.aradine.api.instrument.ARInstrumentExecutableType;
import com.io7m.aradine.api.instrument.ARInstrumentInstanceID;
import com.io7m.aradine.ensemble.internal.v1.context.AREns1InstrumentContext;
import com.io7m.jdeferthrow.core.ExceptionTracker;

import java.util.Objects;

/**
 * A loaded v1 instrument.
 *
 * @param instanceID The instance ID
 * @param executable The instrument executable
 * @param context    The instrument context
 */

public record AREnsInstrumentV1(
  ARInstrumentInstanceID instanceID,
  ARInstrumentExecutableType executable,
  AREns1InstrumentContext context)
  implements AREnsInstrumentType
{
  /**
   * A loaded v1 instrument.
   *
   * @param instanceID The instance ID
   * @param executable The instrument executable
   * @param context    The instrument context
   */

  public AREnsInstrumentV1
  {
    Objects.requireNonNull(instanceID, "InstanceID");
    Objects.requireNonNull(executable, "Executable");
    Objects.requireNonNull(context, "Context");
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
