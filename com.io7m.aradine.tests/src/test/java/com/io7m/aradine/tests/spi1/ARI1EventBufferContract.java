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


package com.io7m.aradine.tests.spi1;

import com.io7m.aradine.instrument.spi1.ARI1EventBufferType;
import com.io7m.aradine.instrument.spi1.ARI1EventNoteOn;
import com.io7m.aradine.instrument.spi1.ARI1EventType;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class ARI1EventBufferContract<T extends ARI1EventBufferType<ARI1EventType>>
{
  protected abstract T createEventBuffer();

  private static <U> int indexOfReference(
    final List<U> xs,
    final U value)
  {
    for (int index = 0; index < xs.size(); ++index) {
      final var v = xs.get(index);
      if (v == value) {
        return index;
      }
    }
    throw new NoSuchElementException();
  }

  /**
   * An empty event buffer returns nothing for all indices.
   *
   * @param values The indices
   */

  @Property
  public void testEmpty(
    final @ForAll List<Integer> values)
  {
    final var e = this.createEventBuffer();
    for (final var index : values) {
      assertEquals(List.of(), e.eventsTake(index.intValue()));
    }
  }

  /**
   * All events are represented and are delivered in the correct order.
   *
   * @param events The events
   */

  @Property
  public void testAllEventsInCorrectOrder(
    final @ForAll List<ARI1EventType> events)
  {
    final var e = this.createEventBuffer();

    for (final var event : events) {
      e.eventAdd(event);
    }

    final var times =
      events.stream()
        .map(event -> Integer.valueOf(event.timeOffsetInFrames()))
        .collect(Collectors.toSet());

    for (final var time : times) {
      final var forTime = e.eventsTake(time);

      for (int index = 0; index < forTime.size(); ++index) {
        final var current = forTime.get(index);
        for (int j = index + 1; j < forTime.size(); ++j) {
          final var next = forTime.get(j);

          final var indexCur =
            indexOfReference(events, current);
          final var indexNxt =
            indexOfReference(events, next);

          assertTrue(
            indexCur <= indexNxt,
            String.format(
              "Index %d of (%s) must be <= Index %d of (%s)",
              Integer.valueOf(indexCur),
              current,
              Integer.valueOf(indexNxt),
              next
            )
          );
        }
      }
    }
  }

  /**
   * All events are represented and are delivered in the correct order.
   */

  @Property
  public void testAllEventsInCorrectOrderSpecific0()
  {
    final var e = this.createEventBuffer();

    final var events = List.<ARI1EventType>of(
      new ARI1EventNoteOn(0, 0, 0.0),
      new ARI1EventNoteOn(0, 1, 0.0),
      new ARI1EventNoteOn(0, 0, 0.0)
    );

    for (final var event : events) {
      e.eventAdd(event);
    }

    final var forTime = e.eventsTake(0);

    for (int index = 0; index < forTime.size(); ++index) {
      final var current = forTime.get(index);
      for (int j = index + 1; j < forTime.size(); ++j) {
        final var next = forTime.get(j);

        final var indexCur =
          indexOfReference(events, current);
        final var indexNxt =
          indexOfReference(events, next);

        assertTrue(
          indexCur <= indexNxt,
          String.format(
            "Index %d of (%s) must be <= Index %d of (%s)",
            Integer.valueOf(indexCur),
            current,
            Integer.valueOf(indexNxt),
            next
          )
        );
      }
    }
  }

  /**
   * Events are not delivered after clearing.
   *
   * @param events The events
   */

  @Property
  public void testEventsCleared(
    final @ForAll List<ARI1EventType> events)
  {
    final var e = this.createEventBuffer();

    for (final var event : events) {
      e.eventAdd(event);
    }

    final var times =
      events.stream()
        .map(event -> Integer.valueOf(event.timeOffsetInFrames()))
        .collect(Collectors.toSet());

    e.eventsClear();

    for (final var time : times) {
      final var forTime = e.eventsTake(time);
      assertEquals(List.of(), forTime);
    }
  }
}
