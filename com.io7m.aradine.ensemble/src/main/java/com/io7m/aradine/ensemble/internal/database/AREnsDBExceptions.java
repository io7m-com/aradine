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
import org.sqlite.SQLiteException;
import tools.jackson.core.exc.StreamReadException;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

/**
 * Functions to wrap exceptions.
 */

public final class AREnsDBExceptions
{
  private AREnsDBExceptions()
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
    final SQLException e)
  {
    return new ARException(
      e.getMessage(),
      e,
      "error-sql",
      Map.of(),
      Optional.empty()
    );
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
      case final StreamReadException x -> {
        yield new ARException(
          x.getMessage(),
          x,
          "error-json-parse-exception",
          Map.of(),
          Optional.empty()
        );
      }
      case final ARException x -> {
        yield x;
      }
      case final SQLiteException x -> {
        yield wrapSQLiteException(x);
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
    if (message.contains("Source port must have AR_SOURCE direction.")) {
      return new ARException(
        "Source port must have AR_SOURCE direction.",
        x,
        "error-source-port-source",
        Map.of(),
        Optional.empty()
      );
    }

    if (message.contains("Target port must have AR_TARGET direction.")) {
      return new ARException(
        "Target port must have AR_TARGET direction.",
        x,
        "error-target-port-target",
        Map.of(),
        Optional.empty()
      );
    }

    if (message.contains("UNIQUE constraint failed: port_connections.port_connection_target")) {
      return new ARException(
        "Target port is already connected.",
        x,
        "error-target-port-connected",
        Map.of(),
        Optional.empty()
      );
    }

    if (message.contains("Source and target ports must belong to different instruments.")) {
      return new ARException(
        "Source and target port must belong to different instruments.",
        x,
        "error-source-target-port-instrument-self",
        Map.of(),
        Optional.empty()
      );
    }

    return new ARException(
      message,
      x,
      "error-exception",
      Map.of(),
      Optional.empty()
    );
  }
}
