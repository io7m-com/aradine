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

package com.io7m.aradine.ensemble.internal.database;

import com.io7m.aradine.api.ARException;
import com.io7m.aradine.database.api.ARDBQueryProviderType;
import com.io7m.aradine.database.api.ARDBTransactionType;
import com.io7m.aradine.database.api.ARDBUnit;

/**
 * Return the next available command ID.
 */

public enum AREnsQCommandIDNext
  implements AREnsQCommandIDNextType, ARDBQueryProviderType
{
  /**
   * Return the next available command ID.
   */

  INSTANCE;

  private static final String QUERY_TEXT = """
WITH
undo_max AS (SELECT COALESCE (MAX (undo_id), 0) AS u FROM undo),
redo_max AS (SELECT COALESCE (MAX (redo_id), 0) AS r FROM redo)
SELECT 1 + MAX(u, r) FROM undo_max, redo_max;
    """;

  @Override
  public Class<?> queryInterface()
  {
    return AREnsQCommandIDNextType.class;
  }

  @Override
  public AREnsQCommandIDNextType queryInstance()
  {
    return this;
  }

  @Override
  public Long execute(
    final ARDBTransactionType transaction,
    final ARDBUnit parameters)
    throws ARException
  {
    final var connection =
      transaction.connection().connection();

    try (var st = connection.prepareStatement(QUERY_TEXT)) {
      try (var rs = st.executeQuery()) {
        if (rs.next()) {
          return Long.valueOf(rs.getLong(1));
        }
      }
      return Long.valueOf(1L);
    } catch (final Exception e) {
      throw AREnsDBExceptions.wrap(e);
    }
  }
}
