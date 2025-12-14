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


package com.io7m.aradine.inventory.internal;

import com.io7m.aradine.inventory.api.ARInventoryConnectionType;
import com.io7m.aradine.inventory.api.ARInventoryDatabaseType;
import com.io7m.aradine.inventory.api.ARInventoryException;
import com.io7m.aradine.inventory.api.ARInventoryQueryType;
import com.io7m.aradine.inventory.api.ARInventoryTransactionCloseBehavior;
import com.io7m.aradine.inventory.api.ARInventoryTransactionType;
import com.io7m.aradine.inventory.api.queries.ARQueryBlobGetType;
import com.io7m.aradine.inventory.api.queries.ARQueryBlobPutType;
import com.io7m.aradine.inventory.api.queries.ARQuerySchemaVersionType;
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

public final class ARInventoryDB implements CloseableType,
  ARInventoryDatabaseType
{
  private static final Map<Class<?>, ARInventoryQueryType<?, ?>> QUERIES =
    createQueries();

  private final SQLiteDataSource dataSource;
  private final AtomicBoolean closed;

  ARInventoryDB(
    final SQLiteDataSource inDataSource)
  {
    this.dataSource =
      Objects.requireNonNull(inDataSource, "dataSource");
    this.closed =
      new AtomicBoolean(false);
  }

  private static Map<Class<?>, ARInventoryQueryType<?, ?>>
  createQueries()
  {
    final var m = new HashMap<Class<?>, ARInventoryQueryType<?, ?>>();
    m.put(ARQuerySchemaVersionType.class, ARQuerySchemaVersion.INSTANCE);
    m.put(ARQueryBlobPutType.class, ARQueryBlobPut.INSTANCE);
    m.put(ARQueryBlobGetType.class, ARQueryBlobGet.INSTANCE);
    return Map.copyOf(m);
  }

  private static ARInventoryException generalSQLError(
    final SQLException e)
  {
    return new ARInventoryException(
      e.getMessage(),
      e,
      "error-sql",
      Map.of(),
      Optional.empty()
    );
  }

  private static void setWALMode(
    final Connection connection)
    throws ARInventoryException
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
   * @throws ARInventoryException On errors
   */

  public ARInventoryConnectionType openConnection()
    throws ARInventoryException
  {
    this.checkNotClosed();

    try {
      final var connection = this.dataSource.getConnection();
      setWALMode(connection);
      connection.setAutoCommit(false);
      return new ARInventoryDBConnection(connection);
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
      throws ARInventoryException;
  }

  static final class ARInventoryDBTransaction
    implements ARInventoryTransactionType
  {
    private final ARInventoryDBConnection connection;
    private final CloseOpType onClose;
    private final AtomicBoolean closed;

    ARInventoryDBConnection connection()
    {
      return this.connection;
    }

    private ARInventoryDBTransaction(
      final ARInventoryDBConnection inConnection,
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
    public <P, R, Q extends ARInventoryQueryType<P, R>> Q query(
      final Class<Q> queryType)
      throws ARInventoryException
    {
      this.checkNotClosed();

      final var query = QUERIES.get(queryType);
      if (query == null) {
        throw new ARInventoryException(
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

    private void checkNotClosed()
    {
      if (this.closed.get()) {
        throw new IllegalStateException("Transaction is closed.");
      }
    }

    @Override
    public void rollback()
      throws ARInventoryException
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
      throws ARInventoryException
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
      throws ARInventoryException
    {
      this.rollback();

      if (this.closed.compareAndSet(false, true)) {
        this.onClose.execute();
      }
    }
  }

  static final class ARInventoryDBConnection
    implements ARInventoryConnectionType
  {
    private final Connection connection;

    private ARInventoryDBConnection(
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
    public ARInventoryTransactionType openTransaction(
      final ARInventoryTransactionCloseBehavior closeBehavior)
      throws ARInventoryException
    {
      return switch (closeBehavior) {
        case ON_CLOSE_CLOSE_CONNECTION -> {
          yield new ARInventoryDBTransaction(
            this, this::close);
        }
        case ON_CLOSE_DO_NOTHING -> {
          yield new ARInventoryDBTransaction(
            this, () -> {
          });
        }
      };
    }

    @Override
    public void close()
      throws ARInventoryException
    {
      try {
        this.connection.close();
      } catch (final SQLException e) {
        throw generalSQLError(e);
      }
    }
  }
}
