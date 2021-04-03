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

package com.io7m.aradine.graph.vanilla.internal;

import com.io7m.aradine.graph.api.ARAudioGraphStringsType;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

@ThreadSafe
public final class ARObjectMap
{
  private static final Object CONSTRUCTING = new Object();

  private final Object objectsLock;
  @GuardedBy("objectsLock")
  private final HashMap<UUID, Object> objects;
  private final ARAudioGraphStringsType strings;

  public ARObjectMap(
    final ARAudioGraphStringsType inStrings)
  {
    this.strings = Objects.requireNonNull(inStrings, "inStrings");
    this.objectsLock = new Object();
    this.objects = new HashMap<UUID, Object>();
  }

  private UUID freshId()
  {
    synchronized (this.objectsLock) {
      while (true) {
        final var id = UUID.randomUUID();
        if (this.objects.containsKey(id)) {
          continue;
        }
        this.objects.put(id, CONSTRUCTING);
        return id;
      }
    }
  }

  public void remove(
    final UUID id)
  {
    Objects.requireNonNull(id, "id");

    synchronized (this.objectsLock) {
      this.remove(id);
    }
  }

  public <T> T withFresh(
    final Function<UUID, T> creator)
  {
    Objects.requireNonNull(creator, "creator");

    final var id = this.freshId();
    try {
      final var value = creator.apply(id);
      synchronized (this.objectsLock) {
        this.objects.put(id, value);
      }
      return value;
    } catch (final Exception exception) {
      synchronized (this.objectsLock) {
        this.objects.remove(id);
        throw exception;
      }
    }
  }

  public <T> T withSpecific(
    final UUID id,
    final Function<UUID, T> creator)
  {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(creator, "creator");

    synchronized (this.objectsLock) {
      final var existing = this.objects.get(id);
      if (existing != null) {
        throw new IllegalArgumentException(
          this.strings.format("identifierAlreadyUsed", id, existing)
        );
      }
      this.objects.put(id, CONSTRUCTING);
    }

    try {
      final var value = creator.apply(id);
      synchronized (this.objectsLock) {
        this.objects.put(id, value);
      }
      return value;
    } catch (final Exception exception) {
      synchronized (this.objectsLock) {
        this.objects.remove(id);
        throw exception;
      }
    }
  }
}
