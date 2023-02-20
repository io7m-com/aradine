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

import com.io7m.aradine.instrument.spi1.ARI1SampleMapEntryType;
import com.io7m.aradine.instrument.spi1.ARI1SampleMapType;
import com.io7m.jsamplebuffer.api.SampleBufferType;
import com.io7m.jsamplebuffer.vanilla.SampleBufferDouble;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMaps;

import java.util.Objects;

public final class ARI1SampleMap implements ARI1SampleMapType
{
  private static final SampleBufferType EMPTY_SAMPLE =
    SampleBufferDouble.createWithHeapBuffer(2, 1L, 44100.0);

  private static final ARI1SampleMapEntry EMPTY_ENTRY =
    new ARI1SampleMapEntry(EMPTY_SAMPLE, 1.0);

  private static final ARI1SampleMapType EMPTY_MAP =
    new ARI1SampleMap(
      Int2ObjectSortedMaps.emptyMap(),
      ARI1SampleMapDescription.empty()
    );

  private final Int2ObjectSortedMap<ARI1SampleMapEntryType> samples;
  private final ARI1SampleMapDescription description;

  public ARI1SampleMap(
    final Int2ObjectSortedMap<ARI1SampleMapEntryType> inSamples,
    final ARI1SampleMapDescription inDescription)
  {
    this.samples =
      Objects.requireNonNull(inSamples, "samples");
    this.description =
      Objects.requireNonNull(inDescription, "description");

    if (!inSamples.isEmpty()) {
      if (inSamples.size() != 128) {
        throw new IllegalArgumentException("Must map all 128 notes to samples.");
      }
    }
  }

  public static ARI1SampleMapType empty()
  {
    return EMPTY_MAP;
  }

  @Override
  public ARI1SampleMapEntryType forNote(final int note)
  {
    return this.samples.getOrDefault(note, EMPTY_ENTRY);
  }
}
