/*
 * Copyright © 2024 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

import com.io7m.aradine.database.api.ARDBConnectionType;
import com.io7m.aradine.database.api.ARDBException;
import com.io7m.aradine.database.api.ARDBQueryType;
import com.io7m.aradine.database.api.ARDBTransactionCloseBehavior;
import com.io7m.aradine.database.api.ARDBTransactionType;
import com.io7m.aradine.database.api.ARDBType;
import com.io7m.jmulticlose.core.CloseableType;
import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The SQLite database.
 */

public final class ARDB implements CloseableType, ARDBType
{
  private final SQLiteDataSource dataSource;
  private final AtomicBoolean closed;
  private final Map<Class<?>, ARDBQueryType<?, ?>> queries;

  /**
   * The SQLite database.
   *
   * @param inDataSource The data source
   * @param inQueries    The query list
   */

  public ARDB(
    final SQLiteDataSource inDataSource,
    final Map<Class<?>, ARDBQueryType<?, ?>> inQueries)
  {
    this.dataSource =
      Objects.requireNonNull(inDataSource, "dataSource");
    this.closed =
      new AtomicBoolean(false);
    this.queries =
      Map.copyOf(inQueries);
  }

  private static ARDBException generalSQLError(
    final SQLException e)
  {
    return new ARDBException(
      e.getMessage(),
      e,
      "error-sql",
      Map.of(),
      Optional.empty()
    );
  }

  private static void setWALMode(
    final Connection connection)
    throws ARDBException
  {
    try (var st = connection.createStatement()) {
      st.execute("PRAGMA journal_mode=WAL;");
    } catch (final SQLException e) {
      throw generalSQLError(e);
    }
  }

  /**
   * Open a connection.
   *
   * @return The connection
   *
   * @throws ARDBException On errors
   */

  public ARDBConnectionType openConnection()
    throws ARDBException
  {
    this.checkNotClosed();

    try {
      final var connection = this.dataSource.getConnection();
      setWALMode(connection);
      connection.setAutoCommit(false);
      return new ARDBDBConnection(this, connection);
    } catch (final SQLException e) {
      throw generalSQLError(e);
    }
  }

  private void checkNotClosed()
  {
    if (this.closed.get()) {
      throw new IllegalStateException("Transaction is closed.");
    }
  }

  @Override
  public boolean isClosed()
  {
    return this.closed.get();
  }

  @Override
  public void close()
  {
    if (this.closed.compareAndSet(false, true)) {
      // Nothing currently.
    }
  }

  private <P, R, Q extends ARDBQueryType<P, R>> Q query(
    final Class<Q> queryType)
    throws ARDBException
  {
    final var query = this.queries.get(queryType);
    if (query == null) {
      throw new ARDBException(
        "No such query.",
        "error-inventory-query-nonexistent",
        Map.ofEntries(
          Map.entry("Query", queryType.getSimpleName())
        ),
        Optional.empty()
      );
    }
    return (Q) query;
  }

  interface CloseOpType
  {
    void execute()
      throws ARDBException;
  }

  static final class ARDBDBTransaction
    implements ARDBTransactionType
  {
    private final ARDBDBConnection connection;
    private final CloseOpType onClose;
    private final AtomicBoolean closed;

    private ARDBDBTransaction(
      final ARDBDBConnection inConnection,
      final CloseOpType inOnClose)
    {
      this.connection =
        Objects.requireNonNull(inConnection, "Connection");
      this.onClose =
        Objects.requireNonNull(inOnClose, "OnClose");
      this.closed =
        new AtomicBoolean(false);
    }

    @Override
    public ARDBDBConnection connection()
    {
      return this.connection;
    }

    @Override
    public <P, R, Q extends ARDBQueryType<P, R>> Q query(
      final Class<Q> queryType)
      throws ARDBException
    {
      this.checkNotClosed();
      return this.connection.db.query(queryType);
    }

    private void checkNotClosed()
    {
      if (this.closed.get()) {
        throw new IllegalStateException("Transaction is closed.");
      }
    }

    @Override
    public void rollback()
      throws ARDBException
    {
      this.checkNotClosed();

      try {
        this.connection.connection.rollback();
      } catch (final SQLException e) {
        throw generalSQLError(e);
      }
    }

    @Override
    public void commit()
      throws ARDBException
    {
      this.checkNotClosed();

      try {
        this.connection.connection.commit();
      } catch (final SQLException e) {
        throw generalSQLError(e);
      }
    }

    @Override
    public void close()
      throws ARDBException
    {
      this.rollback();

      if (this.closed.compareAndSet(false, true)) {
        this.onClose.execute();
      }
    }
  }

  static final class ARDBDBConnection
    implements ARDBConnectionType
  {
    private final Connection connection;
    private final ARDB db;

    private ARDBDBConnection(
      final ARDB inDB,
      final Connection inConnection)
    {
      this.db =
        Objects.requireNonNull(inDB, "DB");
      this.connection =
        Objects.requireNonNull(inConnection, "Connection");
    }

    @Override
    public Connection connection()
    {
      return this.connection;
    }

    @Override
    public ARDBTransactionType openTransaction(
      final ARDBTransactionCloseBehavior closeBehavior)
      throws ARDBException
    {
      return switch (closeBehavior) {
        case ON_CLOSE_CLOSE_CONNECTION -> {
          yield new ARDBDBTransaction(
            this, this::close);
        }
        case ON_CLOSE_DO_NOTHING -> {
          yield new ARDBDBTransaction(
            this, () -> {
          });
        }
      };
    }

    @Override
    public void close()
      throws ARDBException
    {
      try {
        this.connection.close();
      } catch (final SQLException e) {
        throw generalSQLError(e);
      }
    }
  }
}
