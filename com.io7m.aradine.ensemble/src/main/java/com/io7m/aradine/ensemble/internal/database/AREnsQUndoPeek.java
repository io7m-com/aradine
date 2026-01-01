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

import com.io7m.aradine.database.api.ARDBException;
import com.io7m.aradine.database.api.ARDBQueryProviderType;
import com.io7m.aradine.database.api.ARDBTransactionType;
import com.io7m.aradine.database.api.ARDBUnit;
import com.io7m.aradine.ensemble.internal.model.AREnsModelCommandRecord;

import java.util.Optional;

/**
 * Peek a record from the undo stack.
 */

public enum AREnsQUndoPeek
  implements AREnsQUndoPeekType, ARDBQueryProviderType
{
  /**
   * Peek a record from the undo stack.
   */

  INSTANCE;

  private static final String QUERY_TEXT = """
    SELECT undo_id, undo_data_type, undo_data FROM undo
      WHERE undo_id = (SELECT max(undo_id) FROM undo)
        LIMIT 1
    """;

  @Override
  public Class<?> queryInterface()
  {
    return AREnsQUndoPeekType.class;
  }

  @Override
  public AREnsQUndoPeekType queryInstance()
  {
    return this;
  }

  @Override
  public Optional<AREnsModelCommandRecord> execute(
    final ARDBTransactionType transaction,
    final ARDBUnit ignored)
    throws ARDBException
  {
    final var connection =
      transaction.connection().connection();

    try (var st = connection.prepareStatement(QUERY_TEXT)) {
      try (var rs = st.executeQuery()) {
        while (rs.next()) {
          return Optional.of(AREnsUndoRedo.readUndoRecord(rs));
        }
        return Optional.empty();
      }
    } catch (final Exception e) {
      throw AREnsDBExceptions.wrap(e);
    }
  }
}
