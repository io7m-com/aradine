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

import java.util.Arrays;

public final class ARI1SampleMapEmpty
  implements ARI1SampleMapType
{
  private final MapEntry entry;

  public ARI1SampleMapEmpty()
  {
    this.entry = new MapEntry();
  }

  @Override
  public ARI1SampleMapEntryType forNote(
    final int note)
  {
    return this.entry;
  }

  private static final class MapEntry
    implements ARI1SampleMapEntryType
  {
    @Override
    public long frames()
    {
      return 0L;
    }

    @Override
    public int channels()
    {
      return 2;
    }

    @Override
    public double playbackRate()
    {
      return 1.0;
    }

    @Override
    public void evaluate(
      final long frameIndex,
      final double velocity,
      final double[] output)
    {
      Arrays.fill(output, 0.0);
    }
  }
}
