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


package com.io7m.aradine.tests.sampler_xp0;

import com.io7m.aradine.instrument.sampler_xp0.internal.ARIXP0SampleState;
import com.io7m.aradine.instrument.spi1.ARI1SampleMapEntryType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public final class ARIXP0SamplerTest
{
  @Test
  public void testSampleStatePlaybackPitchedDown(
    final @Mock ARI1SampleMapEntryType entry)
  {
    final var mock = lenient();
    mock.when(entry.frames()).thenReturn(3L);
    mock.when(entry.playbackRate()).thenReturn(1.0);

    final var state =
      new ARIXP0SampleState(entry, 1.0);

    final var frame = new double[2];

    for (int index = 0; index < 3L * 8L; ++index) {
      state.evaluate(-1.0, frame);
    }
  }
}
