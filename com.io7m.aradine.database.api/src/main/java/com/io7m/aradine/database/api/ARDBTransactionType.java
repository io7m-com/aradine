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

package com.io7m.aradine.database.api;

import com.io7m.aradine.api.ARException;

import java.util.Objects;

/**
 * The type of database transactions.
 */

public interface ARDBTransactionType
  extends AutoCloseable
{
  /**
   * @return The underlying connection
   */

  ARDBConnectionType connection();

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
   * @throws ARException On errors
   */

  <P, R, Q extends ARDBQueryType<P, R>> Q query(
    Class<Q> queryType)
    throws ARException;

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
   * @throws ARException On errors
   */

  default <P, R, Q extends ARDBQueryType<P, R>> R execute(
    final Class<Q> queryType,
    final P parameters)
    throws ARException
  {
    Objects.requireNonNull(queryType, "QueryType");
    Objects.requireNonNull(parameters, "Parameters");
    return this.query(queryType).execute(this, parameters);
  }

  /**
   * Roll back the transaction.
   *
   * @throws ARException On errors
   */

  void rollback()
    throws ARException;

  /**
   * Commit the transaction.
   *
   * @throws ARException On errors
   */

  void commit()
    throws ARException;

  @Override
  void close()
    throws ARException;

  /**
   * Add a function to be executed after the transaction is committed.
   *
   * @param runnable The function
   */

  void addRunAfterCommit(
    Runnable runnable);
}
