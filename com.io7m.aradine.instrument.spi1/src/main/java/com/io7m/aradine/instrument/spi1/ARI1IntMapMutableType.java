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
 * A mutable map of unboxed integers to values.
 *
 * @param <T> The type of values
 */

public interface ARI1IntMapMutableType<T>
  extends ARI1IntMapReadableType<T>
{
  /**
   * Associate a value with a key, replacing any existing mapping.
   *
   * @param key   The key
   * @param value The value
   *
   * @return The old key, if any
   */

  T put(
    int key,
    T value);

  /**
   * Remove a mapping.
   *
   * @param key The key
   *
   * @return The old value, if any
   */

  T remove(int key);
}
