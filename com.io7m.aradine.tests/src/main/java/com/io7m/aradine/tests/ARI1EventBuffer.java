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

package com.io7m.aradine.tests;

import com.io7m.aradine.instrument.spi1.ARI1EventBufferType;
import com.io7m.aradine.instrument.spi1.ARI1EventType;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.LinkedList;
import java.util.List;

public final class ARI1EventBuffer<T extends ARI1EventType>
  implements ARI1EventBufferType<T>
{
  private final Int2ObjectOpenHashMap<LinkedList<T>> events;

  public ARI1EventBuffer()
  {
    this.events = new Int2ObjectOpenHashMap<>(1024);
  }

  @Override
  public void eventsClear()
  {
    this.events.clear();
  }

  @Override
  public void eventAdd(
    final T event)
  {
    final var time = event.timeOffsetInFrames();
    var byArrival = this.events.get(time);
    if (byArrival == null) {
      byArrival = new LinkedList<>();
    }
    byArrival.add(event);
    this.events.put(time, byArrival);
  }

  @Override
  public List<? extends T> eventsTake(
    final int time)
  {
    final var byArrival = this.events.remove(time);
    if (byArrival == null) {
      return List.of();
    }
    return byArrival;
  }
}
