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
import com.io7m.aradine.ensemble.internal.model.AREnsModelCommandRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * List the redo stack.
 */

public enum AREnsQRedoList
  implements AREnsQRedoListType, ARDBQueryProviderType
{
  /**
   * List the redo stack.
   */

  INSTANCE;

  private static final String QUERY_TEXT = """
    SELECT
      redo_id,
      redo_description,
      redo_time,
      redo_command,
      redo_command_state
    FROM redo
    ORDER BY redo_id ASC
    LIMIT $1
    """;

  private static final String QUERY_TEXT_WITH_START = """
    SELECT
      redo_id,
      redo_description,
      redo_time,
      redo_command,
      redo_command_state
    FROM redo
    WHERE redo_id > $1
    ORDER BY redo_id ASC
    LIMIT $2
    """;

  private static List<AREnsModelCommandRecord>
  readAllResults(
    final PreparedStatement st)
    throws SQLException, ARException
  {
    try (var rs = st.executeQuery()) {
      final var r = new ArrayList<AREnsModelCommandRecord>();
      while (rs.next()) {
        r.add(AREnsUndoRedo.readRedoRecord(rs));
      }
      return List.copyOf(r);
    }
  }

  private static List<AREnsModelCommandRecord>
  executeWithStart(
    final Connection connection,
    final Long start,
    final Parameters parameters)
    throws ARException
  {
    try (var st = connection.prepareStatement(QUERY_TEXT_WITH_START)) {
      st.setLong(1, start.longValue());
      st.setLong(2, (long) parameters.limit());
      return readAllResults(st);
    } catch (final Exception e) {
      throw AREnsDBExceptions.wrap(e);
    }
  }

  @Override
  public Class<?> queryInterface()
  {
    return AREnsQRedoListType.class;
  }

  @Override
  public AREnsQRedoListType queryInstance()
  {
    return this;
  }

  @Override
  public List<AREnsModelCommandRecord> execute(
    final ARDBTransactionType transaction,
    final Parameters parameters)
    throws ARException
  {
    final var connection =
      transaction.connection().connection();

    final var startOpt = parameters.start();
    if (startOpt.isPresent()) {
      return executeWithStart(connection, startOpt.get(), parameters);
    } else {
      return this.executeWithoutStart(connection, parameters);
    }
  }

  private List<AREnsModelCommandRecord> executeWithoutStart(
    final Connection connection,
    final Parameters parameters)
    throws ARException
  {
    try (var st = connection.prepareStatement(QUERY_TEXT)) {
      st.setLong(1, (long) parameters.limit());
      return readAllResults(st);
    } catch (final Exception e) {
      throw AREnsDBExceptions.wrap(e);
    }
  }
}
