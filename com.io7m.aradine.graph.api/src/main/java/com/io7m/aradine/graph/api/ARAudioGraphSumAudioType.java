/*
 * Copyright © 2021 Mark Raynsford <code@io7m.com> http://io7m.com
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

package com.io7m.aradine.graph.api;

import com.io7m.aradine.instrument.metadata.ARInstrumentPortID;

/**
 * A node that sums a set of inputs, writing the result to a single output.
 */

public interface ARAudioGraphSumAudioType extends ARAudioGraphNodeType
{
  /**
   * Create a target port.
   *
   * @param portID The port ID
   *
   * @return A port
   *
   * @throws ARAudioGraphException If a port already exists with the given ID
   */

  ARAudioGraphPortTargetAudioType createPortTarget(
    ARInstrumentPortID portID)
    throws ARAudioGraphException;

  /**
   * Create a target port.
   *
   * @param portID The port ID
   *
   * @return A port
   *
   * @throws ARAudioGraphException If a port already exists with the given ID
   */

  default ARAudioGraphPortTargetAudioType createPortTarget(
    final String portID)
    throws ARAudioGraphException
  {
    return this.createPortTarget(ARInstrumentPortID.of(portID));
  }

  /**
   * @return The source port that produces the summed audio
   */

  ARAudioGraphPortSourceAudioType port();
}
