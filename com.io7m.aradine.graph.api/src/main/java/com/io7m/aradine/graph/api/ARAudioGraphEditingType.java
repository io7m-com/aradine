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

import net.jcip.annotations.NotThreadSafe;

import java.util.UUID;

/**
 * Functions to edit the audio graph.
 *
 * The methods defined within this interface are <i>not</i> safe to be called
 * from multiple threads; applications should use exactly one thread to modify
 * the audio graph.
 */

@NotThreadSafe
public interface ARAudioGraphEditingType
{
  /**
   * Create a new audio system source.
   *
   * @return The source
   */

  ARAudioGraphSystemSourceAudioType createAudioSystemSource();

  /**
   * Create a new audio system source with the given ID.
   *
   * @param id The ID
   *
   * @return The source
   */

  ARAudioGraphSystemSourceAudioType createAudioSystemSource(UUID id);

  /**
   * Create a new audio system target.
   *
   * @return The target
   */

  ARAudioGraphSystemTargetAudioType createAudioSystemTarget();

  /**
   * Create a new audio system target with the given ID.
   *
   * @param id The ID
   *
   * @return The target
   */

  ARAudioGraphSystemTargetAudioType createAudioSystemTarget(UUID id);

  /**
   * Create a new audio summing node.
   *
   * @return The summing node
   */

  ARAudioGraphSumAudioType createAudioSum();

  /**
   * Create a new audio summing node with the given ID.
   *
   * @param id The ID
   *
   * @return The summing node
   */

  ARAudioGraphSumAudioType createAudioSum(UUID id);

  /**
   * Create a new loopback pair.
   *
   * @param source The ID of the source
   * @param target The ID of the target
   *
   * @return The loopback pair
   */

  ARAudioGraphLoopbackPair createLoopbackPair(
    UUID source,
    UUID target
  );

  /**
   * Create a new loopback pair.
   *
   * @return The loopback pair
   */

  ARAudioGraphLoopbackPair createLoopbackPair();

  /**
   * Connect the two audio ports.
   *
   * The method may fail for any of the following reasons:
   *
   * <ul>
   *   <li>The target port is already connected.</li>
   *   <li>Connecting the ports would create a cycle in the graph.</li>
   * </ul>
   *
   * @param source The source port
   * @param target The target port
   *
   * @return The new connection
   *
   * @throws ARAudioGraphException On the errors detailed above
   */

  ARAudioGraphConnectionAudio connectAudio(
    ARAudioGraphPortSourceAudioType source,
    ARAudioGraphPortTargetAudioType target)
    throws ARAudioGraphException;

  /**
   * Disconnect the two audio ports.
   *
   * The method may fail for any of the following reasons:
   *
   * <ul>
   *   <li>The ports are not connected.</li>
   * </ul>
   *
   * @param source The source port
   * @param target The target port
   *
   * @throws ARAudioGraphException On the errors detailed above
   */

  void disconnectAudio(
    ARAudioGraphPortSourceAudioType source,
    ARAudioGraphPortTargetAudioType target)
    throws ARAudioGraphException;
}
