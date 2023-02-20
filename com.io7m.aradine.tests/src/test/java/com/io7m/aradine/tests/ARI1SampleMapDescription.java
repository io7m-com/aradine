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
import com.io7m.jsamplebuffer.api.SampleBufferException;
import com.io7m.jsamplebuffer.api.SampleBufferRateConverterType;
import com.io7m.jsamplebuffer.api.SampleBufferType;
import com.io7m.jsamplebuffer.vanilla.SampleBufferDouble;
import com.io7m.jsamplebuffer.xmedia.SXMSampleBuffers;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMaps;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.file.Path;

public record ARI1SampleMapDescription(
  Int2ObjectSortedMap<Path> filesByNote)
{
  /**
   * For a given frequency {@code f}, multiply {@code f} by this value to yield
   * a frequency exactly one semitone higher.
   */

  private static final double ONE_SEMITONE_UP = 1.059462351977181;

  /**
   * For a given frequency {@code f}, multiply {@code f} by this value to yield
   * a frequency exactly one semitone lower.
   */

  private static final double ONE_SEMITONE_DOWN = 0.9438743126810656;

  public static ARI1SampleMapDescription empty()
  {
    return new ARI1SampleMapDescription(Int2ObjectSortedMaps.emptyMap());
  }

  public ARI1SampleMapType load(
    final SampleBufferRateConverterType converter,
    final int sampleRate)
    throws SampleBufferException
  {
    try {
      if (this.filesByNote.isEmpty()) {
        return ARI1SampleMap.empty();
      }

      final var sampleBuffersByNote =
        new Int2ObjectRBTreeMap<SampleBufferType>();

      for (final var entry : this.filesByNote.int2ObjectEntrySet()) {
        final var sampleBuffer =
          SXMSampleBuffers.sampleBufferOfFile(
            entry.getValue(),
            SampleBufferDouble::createWithHeapBuffer
          );

        final SampleBufferType outputBuffer;
        final var currentRate = (int) sampleBuffer.sampleRate();
        if (currentRate != sampleRate) {
          outputBuffer = converter.convert(
            SampleBufferDouble::createWithHeapBuffer,
            sampleBuffer,
            sampleRate
          );
        } else {
          outputBuffer = sampleBuffer;
        }

        sampleBuffersByNote.put(entry.getIntKey(), outputBuffer);
      }

      final var sampleEntriesByNote =
        new Int2ObjectRBTreeMap<ARI1SampleMapEntryType>();

      for (final var entry : sampleBuffersByNote.int2ObjectEntrySet()) {
        final var note = entry.getIntKey();
        final var sample = entry.getValue();

        sampleEntriesByNote.put(note, new ARI1SampleMapEntry(sample, 1.0));

        {
          var rate = 1.0;
          for (int noteBefore = note - 1; noteBefore >= 0; --noteBefore) {
            final var previous = sampleBuffersByNote.get(noteBefore);
            if (previous != null) {
              break;
            }
            rate = rate * ONE_SEMITONE_DOWN;
            sampleEntriesByNote.put(
              noteBefore,
              new ARI1SampleMapEntry(sample, rate));
          }
        }

        {
          var rate = 1.0;
          for (int noteAfter = note + 1; noteAfter <= 127; ++noteAfter) {
            final var next = sampleBuffersByNote.get(noteAfter);
            if (next != null) {
              break;
            }
            rate = rate * ONE_SEMITONE_UP;
            sampleEntriesByNote.put(
              noteAfter,
              new ARI1SampleMapEntry(sample, rate));
          }
        }
      }

      return new ARI1SampleMap(sampleEntriesByNote, this);
    } catch (final IOException
                   | UnsupportedAudioFileException
                   | SampleBufferException e) {
      throw new SampleBufferException(e);
    }
  }
}
