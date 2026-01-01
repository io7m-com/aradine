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

import com.io7m.anethum.api.ParsingException;
import com.io7m.aradine.database.api.ARDBConnectionType;
import com.io7m.aradine.database.api.ARDBException;
import com.io7m.aradine.database.api.ARDBQueryType;
import com.io7m.aradine.database.api.ARDBTransactionCloseBehavior;
import com.io7m.aradine.database.api.ARDBTransactionType;
import com.io7m.aradine.database.api.ARDBType;
import com.io7m.jxe.core.JXEHardenedSAXParsers;
import com.io7m.lanark.core.RDottedName;
import com.io7m.trasco.api.TrArguments;
import com.io7m.trasco.api.TrExecutorConfiguration;
import com.io7m.trasco.api.TrExecutorUpgrade;
import com.io7m.trasco.api.TrSchemaRevisionSet;
import com.io7m.trasco.vanilla.TrExecutors;
import com.io7m.trasco.vanilla.TrSchemaRevisionSetParsers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.Function;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConfig.Pragma;
import org.sqlite.SQLiteConnection;
import org.sqlite.SQLiteDataSource;
import org.sqlite.SQLiteOpenMode;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * The ensemble database.
 */

public final class AREnsDB
  implements ARDBType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(AREnsDB.class);

  private static final RDottedName APPLICATION_ID =
    new RDottedName("com.io7m.aradine.ensemble");

  // https://sqlite.org/c3ref/c_deterministic.html#sqliteinnocuous
  private static final int SQLITE_INNOCUOUS = 0x000200000;
  // https://sqlite.org/c3ref/c_deterministic.html#sqlitedeterministic
  private static final int SQLITE_DETERMINISTIC = 0x000000800;

  private final Map<Class<?>, ARDBQueryType<?, ?>> queries;
  private final AtomicBoolean closed;
  private final SQLiteDataSource dataSource;

  private AREnsDB(
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

  /**
   * Create or open an ensemble file.
   *
   * @param file The file
   *
   * @return An ensemble file
   *
   * @throws ARDBException On errors
   */

  public static AREnsDB createDatabase(
    final Path file)
    throws ARDBException
  {
    Objects.requireNonNull(file, "file");
    return createDatabase(file, AREnsDBQueries.queries());
  }

  /**
   * Create or open an ensemble file.
   *
   * @param file    The file
   * @param queries The queries
   *
   * @return An ensemble file
   *
   * @throws ARDBException On errors
   */

  public static AREnsDB createDatabase(
    final Path file,
    final Map<Class<?>, ARDBQueryType<?, ?>> queries)
    throws ARDBException
  {
    Objects.requireNonNull(file, "file");

    try {
      final var url = new StringBuilder(128);
      url.append("jdbc:sqlite:");
      url.append(file);

      final var config = new SQLiteConfig();
      config.setApplicationId(0x41456e73);
      config.setOpenMode(SQLiteOpenMode.CREATE);
      config.setEncoding(SQLiteConfig.Encoding.UTF8);
      config.enforceForeignKeys(true);
      config.enableLoadExtension(false);
      setSecurityOptions(config);

      final var dataSource = new SQLiteDataSource(config);
      dataSource.setUrl(url.toString());

      final var revisions =
        getSchemaRevisions();

      try (var connection = (SQLiteConnection) dataSource.getConnection()) {
        setupInitialConnection(connection);

        new TrExecutors().create(
          new TrExecutorConfiguration(
            AREnsDB::schemaVersionGet,
            AREnsDB::schemaVersionSet,
            event -> {
            },
            revisions,
            TrExecutorUpgrade.PERFORM_UPGRADES,
            TrArguments.empty(),
            connection
          )
        ).execute();
        connection.commit();
      }

      return new AREnsDB(dataSource, queries);
    } catch (final Exception e) {
      throw AREnsDBExceptions.wrap(e);
    }
  }

  private static TrSchemaRevisionSet getSchemaRevisions()
    throws ParsingException, IOException
  {
    final var parsers = new TrSchemaRevisionSetParsers();
    try (var stream = schemaStream()) {
      final var parser =
        parsers.createParserWithContext(
          new JXEHardenedSAXParsers(),
          URI.create("urn:source"),
          stream,
          parseStatus -> {

          }
        );
      return parser.execute();
    }
  }

  private static InputStream schemaStream()
  {
    return AREnsDB.class.getResourceAsStream(
      "/com/io7m/aradine/ensemble/internal/Database.xml"
    );
  }

  /**
   * @see "https://sqlite.org/security.html"
   */

  private static void setSecurityOptions(
    final SQLiteConfig config)
  {
    config.setPragma(Pragma.LIMIT_ATTACHED, "1");
    config.setPragma(Pragma.LIMIT_COLUMN, "64");
    config.setPragma(Pragma.LIMIT_COMPOUND_SELECT, "3");
    config.setPragma(Pragma.LIMIT_EXPR_DEPTH, "10");
    config.setPragma(Pragma.LIMIT_FUNCTION_ARG, "8");
    config.setPragma(Pragma.LIMIT_LENGTH, "1000000");
    config.setPragma(Pragma.LIMIT_LIKE_PATTERN_LENGTH, "50");
    config.setPragma(Pragma.LIMIT_SQL_LENGTH, "10000");
    config.setPragma(Pragma.LIMIT_TRIGGER_DEPTH, "1");
    config.setPragma(Pragma.LIMIT_VARIABLE_NUMBER, "32");
    config.setPragma(Pragma.LIMIT_VDBE_OP, "25000");
    config.enableLoadExtension(false);
  }

  private static void setWALMode(
    final Connection connection)
    throws ARDBException
  {
    try (var st = connection.createStatement()) {
      st.execute("PRAGMA journal_mode=WAL;");
    } catch (final Exception e) {
      throw AREnsDBExceptions.wrap(e);
    }
  }

  private static void schemaVersionSet(
    final BigInteger version,
    final Connection connection)
    throws SQLException
  {
    final String statementText;
    if (Objects.equals(version, BigInteger.ZERO)) {
      statementText = "INSERT INTO schema_version (sv_application_id, sv_version) VALUES (?, ?)";
      try (var statement =
             connection.prepareStatement(statementText)) {
        statement.setString(1, APPLICATION_ID.value());
        statement.setLong(2, checkLong(version));
        statement.execute();
      }

      Function.create(connection, "REGEXP", new AREnsRegexpFunction());
    } else {
      statementText = "UPDATE schema_version SET sv_version = ?";
      try (var statement =
             connection.prepareStatement(statementText)) {
        statement.setLong(1, checkLong(version));
        statement.execute();
      }
    }
  }

  private static Optional<BigInteger> schemaVersionGet(
    final Connection connection)
    throws SQLException
  {
    Objects.requireNonNull(connection, "connection");

    try {
      final var statementText =
        "SELECT sv_application_id, sv_version FROM schema_version";

      try (var statement = connection.prepareStatement(statementText)) {
        try (var result = statement.executeQuery()) {
          if (!result.next()) {
            throw new SQLException("schema_version table is empty!");
          }
          final var applicationCA =
            result.getString(1);
          final var version =
            result.getLong(2);
          final var applicationId =
            APPLICATION_ID.value();

          if (!Objects.equals(applicationCA, applicationId)) {
            throw new SQLException(
              String.format(
                "Database application ID is %s but should be %s",
                applicationCA,
                applicationId
              )
            );
          }

          return Optional.of(BigInteger.valueOf(version));
        }
      }
    } catch (final SQLException e) {
      if (e.getMessage().contains("no such table")) {
        connection.rollback();
        return Optional.empty();
      }
      throw e;
    }
  }

  private static long checkLong(
    final BigInteger version)
  {
    final var tooSmall =
      version.compareTo(BigInteger.ZERO) < 0;
    final var tooLarge =
      version.compareTo(new BigInteger("2147483647")) > 0;

    if (tooSmall || tooLarge) {
      throw new IllegalArgumentException(
        "Version %s must be in the range [0, 2147483647]"
          .formatted(version)
      );
    }
    return version.longValue();
  }

  /**
   * @see "https://sqlite.org/security.html"
   */

  private static void setupInitialConnection(
    final SQLiteConnection connection)
    throws SQLException, ARDBException
  {
    setWALMode(connection);
    setSynchronous(connection);
    setSecureDelete(connection);
    connection.setAutoCommit(false);
    setTrustedSchemaOff(connection);
    createRegexpFunction(connection);
    setIntegrityCheck(connection);
    setCellSizeCheck(connection);
    setNoMMAP(connection);
  }

  /**
   * @see "https://sqlite.org/security.html"
   */

  private static void setupConnection(
    final SQLiteConnection connection)
    throws SQLException, ARDBException
  {
    setWALMode(connection);
    setSynchronous(connection);
    setSecureDelete(connection);
    connection.setAutoCommit(false);
    setTrustedSchemaOff(connection);
    createRegexpFunction(connection);
    setCellSizeCheck(connection);
    setNoMMAP(connection);
  }

  private static void createRegexpFunction(
    final SQLiteConnection connection)
    throws SQLException
  {
    Function.create(
      connection,
      "REGEXP",
      new AREnsRegexpFunction(),
      2,
      SQLITE_INNOCUOUS | SQLITE_DETERMINISTIC
    );
  }

  private static void setNoMMAP(
    final Connection connection)
    throws ARDBException
  {
    try (var st = connection.createStatement()) {
      st.execute("PRAGMA mmap_size = 0;");
    } catch (final Exception e) {
      throw AREnsDBExceptions.wrap(e);
    }
  }

  private static void setSecureDelete(
    final SQLiteConnection connection)
    throws ARDBException
  {
    try (var st = connection.createStatement()) {
      st.execute("PRAGMA secure_delete = on;");
    } catch (final Exception e) {
      throw AREnsDBExceptions.wrap(e);
    }
  }

  private static void setSynchronous(
    final SQLiteConnection connection)
    throws ARDBException
  {
    try (var st = connection.createStatement()) {
      st.execute("PRAGMA synchronous = EXTRA;");
    } catch (final Exception e) {
      throw AREnsDBExceptions.wrap(e);
    }
  }

  private static void setCellSizeCheck(
    final Connection connection)
    throws ARDBException
  {
    try (var st = connection.createStatement()) {
      st.execute("PRAGMA cell_size_check = on;");
    } catch (final Exception e) {
      throw AREnsDBExceptions.wrap(e);
    }
  }

  private static void setIntegrityCheck(
    final Connection connection)
    throws ARDBException
  {
    try (var st = connection.createStatement()) {
      st.execute("PRAGMA integrity_check;");
    } catch (final Exception e) {
      throw AREnsDBExceptions.wrap(e);
    }
  }

  private static void setTrustedSchemaOff(
    final Connection connection)
    throws ARDBException
  {
    try (var st = connection.createStatement()) {
      st.execute("PRAGMA trusted_schema = off;");
    } catch (final Exception e) {
      throw AREnsDBExceptions.wrap(e);
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

  }

  private <P, R, Q extends ARDBQueryType<P, R>> Q query(
    final Class<Q> queryType)
    throws ARDBException
  {
    final var query = this.queries.get(queryType);
    if (query == null) {
      throw new ARDBException(
        "No such query.",
        "error-query-nonexistent",
        Map.ofEntries(
          Map.entry("Query", queryType.getSimpleName())
        ),
        Optional.empty()
      );
    }
    return (Q) query;
  }

  @Override
  public ARDBConnectionType openConnection()
    throws ARDBException
  {
    try {
      final var connection = (SQLiteConnection) this.dataSource.getConnection();
      setupConnection(connection);
      return new AREnsDBConnection(this, connection);
    } catch (final SQLException e) {
      throw AREnsDBExceptions.wrap(e);
    }
  }

  interface CloseOpType
  {
    void execute()
      throws ARDBException;
  }

  private static final class AREnsRegexpFunction
    extends Function
  {
    AREnsRegexpFunction()
    {

    }

    @Override
    protected void xFunc()
      throws SQLException
    {
      final var expression = this.value_text(0);
      var value = this.value_text(1);
      if (value == null) {
        value = "";
      }

      final var pattern =
        Pattern.compile(expression);
      final var matcher =
        pattern.matcher(value);

      if (matcher.matches()) {
        this.result(1);
      } else {
        this.result(0);
      }
    }
  }

  private static final class AREnsTransaction
    implements ARDBTransactionType
  {
    private final AREnsDBConnection connection;
    private final CloseOpType onClose;
    private final AtomicBoolean closed;

    private AREnsTransaction(
      final AREnsDBConnection inConnection,
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
    public ARDBConnectionType connection()
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
        throw AREnsDBExceptions.wrap(e);
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
        throw AREnsDBExceptions.wrap(e);
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

  private static final class AREnsDBConnection
    implements ARDBConnectionType
  {
    private final AREnsDB db;
    private final Connection connection;

    AREnsDBConnection(
      final AREnsDB inDb,
      final Connection inConnection)
    {
      this.db = inDb;
      this.connection = inConnection;
    }

    @Override
    public Connection connection()
    {
      return this.connection;
    }

    @Override
    public ARDBTransactionType openTransaction(
      final ARDBTransactionCloseBehavior closeBehavior)
    {
      return switch (closeBehavior) {
        case ON_CLOSE_CLOSE_CONNECTION -> {
          yield new AREnsTransaction(
            this, this::close);
        }
        case ON_CLOSE_DO_NOTHING -> {
          yield new AREnsTransaction(
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
        throw AREnsDBExceptions.wrap(e);
      }
    }
  }
}
