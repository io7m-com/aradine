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

import java.util.Objects;

/**
 * The type of inventory transactions.
 */

public interface ARInventoryTransactionType
  extends AutoCloseable
{
  /**
   * Find a database query.
   *
   * @param queryType The query type
   * @param <P>       The parameters type
   * @param <R>       The return type
   * @param <Q>       The query type
   *
   * @return A query
   *
   * @throws ARInventoryException On errors
   */

  <P, R, Q extends ARInventoryQueryType<P, R>> Q query(
    Class<Q> queryType)
    throws ARInventoryException;

  /**
   * Find a query and execute it.
   *
   * @param queryType  The query type
   * @param <P>        The parameters type
   * @param <R>        The return type
   * @param <Q>        The query type
   * @param parameters The parameters
   *
   * @return The query result
   *
   * @throws ARInventoryException On errors
   */

  default <P, R, Q extends ARInventoryQueryType<P, R>> R execute(
    final Class<Q> queryType,
    final P parameters)
    throws ARInventoryException
  {
    Objects.requireNonNull(queryType, "QueryType");
    Objects.requireNonNull(parameters, "Parameters");
    return this.query(queryType).execute(this, parameters);
  }

  /**
   * Roll back the transaction.
   *
   * @throws ARInventoryException On errors
   */

  void rollback()
    throws ARInventoryException;

  /**
   * Commit the transaction.
   *
   * @throws ARInventoryException On errors
   */

  void commit()
    throws ARInventoryException;

  @Override
  void close()
    throws ARInventoryException;
}
