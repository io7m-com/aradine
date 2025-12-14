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


package com.io7m.aradine.catalog.internal;

import com.io7m.aradine.catalog.api.ARCatalogConnectionType;
import com.io7m.aradine.catalog.api.ARCatalogDatabaseType;
import com.io7m.aradine.catalog.api.ARCatalogException;
import com.io7m.aradine.catalog.api.ARCatalogQueryType;
import com.io7m.aradine.catalog.api.ARCatalogTransactionCloseBehavior;
import com.io7m.aradine.catalog.api.ARCatalogTransactionType;
import com.io7m.aradine.catalog.api.queries.ARQueryBlobGetType;
import com.io7m.aradine.catalog.api.queries.ARQueryBlobPutType;
import com.io7m.aradine.catalog.api.queries.ARQuerySchemaVersionType;
import com.io7m.jmulticlose.core.CloseableType;
import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The SQLite database.
 */

public final class ARCatalogDB implements CloseableType, ARCatalogDatabaseType
{
  private static final Map<Class<?>, ARCatalogQueryType<?, ?>> QUERIES =
    createQueries();

  private final SQLiteDataSource dataSource;
  private final AtomicBoolean closed;

  ARCatalogDB(
    final SQLiteDataSource inDataSource)
  {
    this.dataSource =
      Objects.requireNonNull(inDataSource, "dataSource");
    this.closed =
      new AtomicBoolean(false);
  }

  private static Map<Class<?>, ARCatalogQueryType<?, ?>>
  createQueries()
  {
    final var m = new HashMap<Class<?>, ARCatalogQueryType<?, ?>>();
    m.put(ARQuerySchemaVersionType.class, ARQuerySchemaVersion.INSTANCE);
    m.put(ARQueryBlobPutType.class, ARQueryBlobPut.INSTANCE);
    m.put(ARQueryBlobGetType.class, ARQueryBlobGet.INSTANCE);
    return Map.copyOf(m);
  }

  private static ARCatalogException generalSQLError(
    final SQLException e)
  {
    return new ARCatalogException(
      e.getMessage(),
      e,
      "error-sql",
      Map.of(),
      Optional.empty()
    );
  }

  private static void setWALMode(
    final Connection connection)
    throws ARCatalogException
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
   * @throws ARCatalogException On errors
   */

  public ARCatalogConnectionType openConnection()
    throws ARCatalogException
  {
    this.checkNotClosed();

    try {
      final var connection = this.dataSource.getConnection();
      setWALMode(connection);
      connection.setAutoCommit(false);
      return new ARCatalogDBConnection(connection);
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

  interface CloseOpType
  {
    void execute()
      throws ARCatalogException;
  }

  static final class ARCatalogDBTransaction
    implements ARCatalogTransactionType
  {
    private final ARCatalogDBConnection connection;
    private final CloseOpType onClose;
    private final AtomicBoolean closed;

    ARCatalogDBConnection connection()
    {
      return this.connection;
    }

    private ARCatalogDBTransaction(
      final ARCatalogDBConnection inConnection,
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
    public <P, R, Q extends ARCatalogQueryType<P, R>> Q query(
      final Class<Q> queryType)
      throws ARCatalogException
    {
      this.checkNotClosed();

      final var query = QUERIES.get(queryType);
      if (query == null) {
        throw new ARCatalogException(
          "No such query.",
          "error-catalog-query-nonexistent",
          Map.ofEntries(
            Map.entry("Query", queryType.getSimpleName())
          ),
          Optional.empty()
        );
      }
      return (Q) query;
    }

    private void checkNotClosed()
    {
      if (this.closed.get()) {
        throw new IllegalStateException("Transaction is closed.");
      }
    }

    @Override
    public void rollback()
      throws ARCatalogException
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
      throws ARCatalogException
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
      throws ARCatalogException
    {
      this.rollback();

      if (this.closed.compareAndSet(false, true)) {
        this.onClose.execute();
      }
    }
  }

  static final class ARCatalogDBConnection
    implements ARCatalogConnectionType
  {
    private final Connection connection;

    private ARCatalogDBConnection(
      final Connection inConnection)
    {
      this.connection =
        Objects.requireNonNull(inConnection, "Connection");
    }

    @Override
    public Connection connection()
    {
      return this.connection;
    }

    @Override
    public ARCatalogTransactionType openTransaction(
      final ARCatalogTransactionCloseBehavior closeBehavior)
      throws ARCatalogException
    {
      return switch (closeBehavior) {
        case ON_CLOSE_CLOSE_CONNECTION -> {
          yield new ARCatalogDBTransaction(
            this, this::close);
        }
        case ON_CLOSE_DO_NOTHING -> {
          yield new ARCatalogDBTransaction(
            this, () -> {
          });
        }
      };
    }

    @Override
    public void close()
      throws ARCatalogException
    {
      try {
        this.connection.close();
      } catch (final SQLException e) {
        throw generalSQLError(e);
      }
    }
  }
}
