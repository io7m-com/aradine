/*
 * Copyright © 2026 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

import com.io7m.aradine.ensemble.internal.model.AREnsModelCommandRecord;
import com.io7m.aradine.ensemble.internal.v1.json.AREnsJMappers;

import java.sql.ResultSet;
import java.sql.SQLException;

final class AREnsUndoRedo
{
  private AREnsUndoRedo()
  {

  }

  static AREnsModelCommandRecord readRedoRecord(
    final ResultSet rs)
    throws SQLException
  {
    final var mapper =
      AREnsJMappers.mapper();

    return mapper.readValue(
      rs.getBytes("redo_command_state"),
      AREnsModelCommandRecord.class
    );
  }

  static AREnsModelCommandRecord readUndoRecord(
    final ResultSet rs)
    throws SQLException
  {
    final var mapper =
      AREnsJMappers.mapper();

    return mapper.readValue(
      rs.getBytes("undo_command_state"),
      AREnsModelCommandRecord.class
    );
  }
}
