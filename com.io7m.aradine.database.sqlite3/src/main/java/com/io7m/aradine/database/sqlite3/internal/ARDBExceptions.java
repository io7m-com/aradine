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

package com.io7m.aradine.database.sqlite3.internal;

import com.io7m.aradine.api.ARException;
import org.sqlite.SQLiteException;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

/**
 * Functions to wrap exceptions.
 */

public final class ARDBExceptions
{
  private ARDBExceptions()
  {

  }

  /**
   * Wrap an exception.
   *
   * @param e The source
   *
   * @return The wrapped exception
   */

  public static ARException wrap(
    final Exception e)
  {
    return switch (e) {
      case final ARException x -> {
        yield x;
      }
      case final SQLiteException x -> {
        yield wrapSQLiteException(x);
      }
      case final SQLException x -> {
        yield new ARException(
          e.getMessage(),
          e,
          "error-sql",
          Map.of(),
          Optional.empty()
        );
      }
      case Exception _ -> {
        yield new ARException(
          e.getMessage(),
          e,
          "error-exception",
          Map.of(),
          Optional.empty()
        );
      }
    };
  }

  private static ARException wrapSQLiteException(
    final SQLiteException x)
  {
    final var message = x.getMessage();
    if (message.contains("[SQLITE_NOTADB] File opened that is not a database file")) {
      return new ARException(
        "File opened that is not a database file.",
        x,
        "error-file-not-database",
        Map.of(),
        Optional.empty()
      );
    }

    return new ARException(
      message,
      x,
      "error-sqlite-exception",
      Map.of(),
      Optional.empty()
    );
  }
}
