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

import com.io7m.aradine.database.api.ARDBException;
import com.io7m.aradine.ensemble.internal.json_v1.AREnsJMappers;
import com.io7m.aradine.ensemble.internal.model.AREnsModelCommandRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

final class AREnsUndoRedo
{
  private AREnsUndoRedo()
  {

  }

  static AREnsModelCommandRecord readRedoRecord(
    final ResultSet rs)
    throws SQLException, ARDBException
  {
    final var dataType =
      rs.getString("redo_data_type");

    final var mapper =
      AREnsJMappers.mapper();

    return switch (dataType) {
      case "AREnsModelCommandRecord" -> {
        yield mapper.readValue(
          rs.getBytes("redo_data"),
          AREnsModelCommandRecord.class
        );
      }
      default ->
        throw new ARDBException(
          "Unsupported redo record type.",
          "error-redo-record-unsupported",
          Map.of("Type", dataType),
          Optional.empty()
        );
    };
  }

  static AREnsModelCommandRecord readUndoRecord(
    final ResultSet rs)
    throws SQLException, ARDBException
  {
    final var dataType =
      rs.getString("undo_data_type");

    final var mapper =
      AREnsJMappers.mapper();

    return switch (dataType) {
      case "AREnsModelCommandRecord" -> {
        yield mapper.readValue(
          rs.getBytes("undo_data"),
          AREnsModelCommandRecord.class
        );
      }
      default ->
        throw new ARDBException(
          "Unsupported undo record type.",
          "error-undo-record-unsupported",
          Map.of("Type", dataType),
          Optional.empty()
        );
    };
  }
}
