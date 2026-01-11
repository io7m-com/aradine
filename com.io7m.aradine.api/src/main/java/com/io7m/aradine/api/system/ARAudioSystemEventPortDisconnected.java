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

package com.io7m.aradine.api.system;

import java.util.Objects;

/**
 * Two ports in the audio system were disconnected.
 *
 * @param port0 The first port
 * @param port1 The second port
 */

public record ARAudioSystemEventPortDisconnected(
  String port0,
  String port1)
  implements ARAudioSystemEventType
{
  /**
   * Two ports in the audio system were disconnected.
   *
   * @param port0 The first port
   * @param port1 The second port
   */

  public ARAudioSystemEventPortDisconnected
  {
    Objects.requireNonNull(port0, "Port0");
    Objects.requireNonNull(port1, "Port1");
  }
}
