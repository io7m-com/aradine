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

import java.util.function.Consumer;

/**
 * A source audio port in the audio graph.
 */

public interface ARAudioGraphPortSourceAudioType
  extends ARAudioGraphPortSourceType
{
  /**
   * Connect this port to the given target port.
   *
   * @param target The target port
   *
   * @return The new connection
   *
   * @throws ARAudioGraphException For the same reasons as {@code connectAudio}
   * @see ARAudioGraphType#connectAudio(ARAudioGraphPortSourceAudioType, ARAudioGraphPortTargetAudioType)
   */

  ARAudioGraphConnectionAudio connect(
    ARAudioGraphPortTargetAudioType target)
    throws ARAudioGraphException;

  /**
   * Disconnect this port from the given target port.
   *
   * @param target The target port
   *
   * @throws ARAudioGraphException For the same reasons as {@code disconnectAudio}
   * @see ARAudioGraphType#disconnectAudio(ARAudioGraphPortSourceAudioType, ARAudioGraphPortTargetAudioType)
   */

  void disconnect(
    ARAudioGraphPortTargetAudioType target)
    throws ARAudioGraphException;

  /**
   * Read samples from the source.
   *
   * @param receiver The function that will receive the current source buffer
   */

  void read(
    Consumer<double[]> receiver);
}
