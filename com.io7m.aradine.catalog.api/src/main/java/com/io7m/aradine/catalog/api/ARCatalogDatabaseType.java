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

import static com.io7m.aradine.catalog.api.ARCatalogTransactionCloseBehavior.ON_CLOSE_CLOSE_CONNECTION;

/**
 * A catalog of instruments, sample maps, and other resources.
 */

public interface ARCatalogDatabaseType
  extends AutoCloseable
{
  /**
   * @return A new connection
   */

  ARCatalogConnectionType openConnection()
    throws ARCatalogException;

  /**
   * Open a transaction.
   *
   * @return A transaction
   *
   * @throws ARCatalogException On errors
   */

  default ARCatalogTransactionType openTransaction()
    throws ARCatalogException
  {
    return this.openConnection().openTransaction(ON_CLOSE_CLOSE_CONNECTION);
  }

  @Override
  void close()
    throws ARCatalogException;
}
