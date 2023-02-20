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

import java.util.List;

/**
 * The type of event buffers.
 */

public interface ARI1ControlEventBufferType
{
  /**
   * Clear the buffer. This should typically be called at the end of each
   * processing period.
   */

  void eventsClear();

  /**
   * Add an event to the buffer.
   *
   * @param event The event
   */

  void eventAdd(ARI1ControlEventType event);

  /**
   * Take all events that apply to the given frame index/time.
   *
   * @param frameIndex The frame index/time
   *
   * @return The events that apply, if any
   */

  List<ARI1ControlEventType> eventsTake(int frameIndex);
}
