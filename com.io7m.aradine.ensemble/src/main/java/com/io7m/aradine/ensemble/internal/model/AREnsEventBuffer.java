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

package com.io7m.aradine.ensemble.internal.model;

import com.io7m.aradine.ensemble.internal.events.AREnsEventInstrumentType;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * An event buffer.
 *
 * @param <T> The type of events
 */

public final class AREnsEventBuffer<T extends AREnsEventInstrumentType>
{
  private final Int2ObjectOpenHashMap<LinkedList<T>> events;

  /**
   * Create an event buffer.
   */

  public AREnsEventBuffer()
  {
    this.events = new Int2ObjectOpenHashMap<>(1024);
  }

  /**
   * Clear the buffer. This should typically be called at the end of each
   * processing period.
   */

  public void eventsClear()
  {
    this.events.clear();
  }

  /**
   * Add an event to the buffer.
   *
   * @param event The event
   */

  public void eventAdd(
    final T event)
  {
    Objects.requireNonNull(event, "Event");

    final var time = event.timeOffsetInFrames();
    var byArrival = this.events.get(time);
    if (byArrival == null) {
      byArrival = new LinkedList<>();
    }
    byArrival.add(event);
    this.events.put(time, byArrival);
  }

  /**
   * Take all events that apply to the given frame index/time.
   *
   * @param frameIndex The frame index/time
   *
   * @return The events that apply, if any
   */

  public List<? extends T> eventsTake(
    final int frameIndex)
  {
    final var byArrival = this.events.remove(frameIndex);
    if (byArrival == null) {
      return List.of();
    }
    return byArrival;
  }
}
