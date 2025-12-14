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

package com.io7m.aradine.catalog.api;

import com.io7m.mime2045.core.MimeType;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * A catalog of instruments, sample maps, and other resources.
 */

public interface ARCatalogType
  extends AutoCloseable
{
  /**
   * @return The database
   */

  ARCatalogDatabaseType database();

  /**
   * Copy the given blob into the catalog.
   *
   * @param file             The source file
   * @param type             The file type
   * @param progressConsumer A consumer of progress
   *
   * @return The operation in progress
   *
   * @throws ARCatalogException On errors
   */

  CompletableFuture<ARCatalogBlob> blobInstall(
    Path file,
    MimeType type,
    Consumer<ARCatalogProgress> progressConsumer)
    throws ARCatalogException;

  @Override
  void close()
    throws ARCatalogException;
}
