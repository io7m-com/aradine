/*
 * Copyright © 2025 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

package com.io7m.aradine.api.instrument;

import com.io7m.aradine.api.data.ARBlob;
import com.io7m.aradine.api.data.ARBytes;
import com.io7m.lanark.core.RDottedName;

import java.util.Objects;

/**
 * An instrument.
 *
 * @param identifier     The identifier
 * @param metadataFormat The metadata format
 * @param metadataText   The metadata text
 * @param title          The title
 * @param description    The description
 * @param blob           The blob
 */

public record ARInstrumentData(
  ARInstrumentID identifier,
  RDottedName metadataFormat,
  ARBytes metadataText,
  String title,
  String description,
  ARBlob blob)
{
  /**
   * An instrument.
   *
   * @param identifier     The identifier
   * @param metadataFormat The metadata format
   * @param metadataText   The metadata text
   * @param title          The title
   * @param description    The description
   * @param blob           The blob
   */

  public ARInstrumentData
  {
    Objects.requireNonNull(identifier, "identifier");
    Objects.requireNonNull(metadataFormat, "MetadataFormat");
    Objects.requireNonNull(metadataText, "MetadataText");
    Objects.requireNonNull(title, "Title");
    Objects.requireNonNull(description, "Description");
    Objects.requireNonNull(blob, "Blob");
  }
}
