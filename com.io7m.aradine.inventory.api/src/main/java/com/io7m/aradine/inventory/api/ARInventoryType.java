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

package com.io7m.aradine.inventory.api;

import com.io7m.aradine.api.ARBlob;
import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.instrument.ARInstrumentID;
import com.io7m.aradine.api.progress.ARProgress;
import com.io7m.aradine.database.api.ARDBType;
import com.io7m.mime2045.core.MimeType;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * A inventory of instruments, sample maps, and other resources.
 */

public interface ARInventoryType
  extends AutoCloseable
{
  /**
   * The instrument jar MIME type.
   */

  MimeType INSTRUMENT_JAR_MIME_TYPE =
    MimeType.of("application", "jar-archive");

  /**
   * @return The database
   */

  ARDBType database();

  /**
   * Copy the given blob into the inventory.
   *
   * @param file             The source file
   * @param type             The file type
   * @param progressConsumer A consumer of progress
   *
   * @return The operation in progress
   */

  CompletableFuture<ARBlob> blobInstall(
    Path file,
    MimeType type,
    Consumer<ARProgress> progressConsumer);

  /**
   * Install the given instrument into the inventory.
   *
   * @param file             The source file
   * @param progressConsumer A consumer of progress
   *
   * @return The operation in progress
   */

  CompletableFuture<ARInstrumentID> instrumentInstall(
    Path file,
    Consumer<ARProgress> progressConsumer);

  /**
   * Get the file for the installed instrument.
   *
   * @param instrument The instrument
   *
   * @return The file, if the instrument exists
   *
   * @throws ARException On errors
   */

  Optional<Path> instrumentFile(
    ARInstrumentID instrument)
    throws ARException;

  @Override
  void close()
    throws ARException;
}
