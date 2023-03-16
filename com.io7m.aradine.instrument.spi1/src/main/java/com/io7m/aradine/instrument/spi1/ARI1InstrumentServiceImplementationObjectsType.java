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

package com.io7m.aradine.instrument.spi1;

/**
 * Methods to create objects for instrument implementations.
 */

public interface ARI1InstrumentServiceImplementationObjectsType
{
  /**
   * @param <T> The type of events in the buffer
   *
   * @return A new empty event buffer
   */

  <T extends ARI1EventType> ARI1EventBufferType<T> createEventBuffer();

  /**
   * Create an empty integer map.
   *
   * @param size The initial map size
   * @param <T>  The type of values
   *
   * @return A new map
   */

  <T> ARI1IntMapMutableType<T> createIntMap(int size);


  /**
   * @param seed The seed value
   *
   * @return A new RNG
   */

  ARI1RNGDeterministicType createDeterministicRNG(int seed);
}
