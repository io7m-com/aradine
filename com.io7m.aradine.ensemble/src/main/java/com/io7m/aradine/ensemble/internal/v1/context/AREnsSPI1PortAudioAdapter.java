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

import com.io7m.aradine.ensemble.internal.model.AREnsPortAudio;
import com.io7m.aradine.instrument.spi1.ARI1PortNumber;
import com.io7m.aradine.instrument.spi1.ARI1PortSourceAudioType;
import com.io7m.aradine.instrument.spi1.ARI1PortTargetAudioType;

import java.util.Objects;

/**
 * An SPI1 audio port adapter. This wraps a core API audio port and exposes
 * the API required by SPI1 instruments.
 */

public final class AREnsSPI1PortAudioAdapter
  implements ARI1PortSourceAudioType, ARI1PortTargetAudioType
{
  private final AREnsSPI1InstrumentContextAdapter context;
  private final AREnsPortAudio port;
  private final ARI1PortNumber number;

  AREnsSPI1PortAudioAdapter(
    final AREnsSPI1InstrumentContextAdapter inContext,
    final AREnsPortAudio inPort)
  {
    this.context =
      Objects.requireNonNull(inContext, "Context");
    this.port =
      Objects.requireNonNull(inPort, "Port");
    this.number =
      new ARI1PortNumber(this.port.portDescription().number().value());
  }

  /**
   * Get access to the port buffer. This buffer is only valid for the current
   * processing period.
   *
   * @return The underlying port buffer
   */

  public double[] buffer()
  {
    return this.port.buffer();
  }

  @Override
  public void write(
    final int frame,
    final double value)
  {
    this.port.write(frame, value);
  }

  @Override
  public double read(
    final int frame)
  {
    return this.port.read(frame);
  }

  @Override
  public ARI1PortNumber id()
  {
    return this.number;
  }
}
