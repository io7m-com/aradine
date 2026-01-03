/*
 * Copyright © 2023 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

package com.io7m.aradine.database.sqlite3;

import com.io7m.aradine.api.ARCloseables;
import com.io7m.aradine.api.ARException;
import com.io7m.aradine.database.api.ARDBConfiguration;
import com.io7m.aradine.database.api.ARDBQueryProviderType;
import com.io7m.aradine.database.api.ARDBQueryType;
import com.io7m.aradine.database.api.ARDBType;
import com.io7m.aradine.database.sqlite3.internal.ARDB;
import com.io7m.aradine.database.sqlite3.internal.ARDBExceptions;
import com.io7m.trasco.api.TrArguments;
import com.io7m.trasco.api.TrEventExecutingSQL;
import com.io7m.trasco.api.TrEventType;
import com.io7m.trasco.api.TrEventUpgrading;
import com.io7m.trasco.api.TrExecutorConfiguration;
import com.io7m.trasco.api.TrSchemaRevisionSet;
import com.io7m.trasco.vanilla.TrExecutors;
import com.io7m.trasco.vanilla.TrSchemaRevisionSetParsers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import org.sqlite.SQLiteErrorCode;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static com.io7m.trasco.api.TrExecutorUpgrade.PERFORM_UPGRADES;
import static java.math.BigInteger.valueOf;

/**
 * The SQLite database implementation.
 */

public final class ARDBFactory
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ARDBFactory.class);

  /**
   * The SQLite database implementation.
   */

  public ARDBFactory()
  {

  }

  private static void schemaVersionSet(
    final ARDBConfiguration configuration,
    final BigInteger version,
    final Connection connection)
    throws SQLException
  {
    final String statementText;
    if (Objects.equals(version, BigInteger.ZERO)) {
      statementText = "insert into schema_version (version_application_id, version_number) values (?, ?)";
      try (var statement =
             connection.prepareStatement(statementText)) {
        statement.setString(1, configuration.applicationIdText().value());
        statement.setLong(2, version.longValueExact());
        statement.execute();
      }
    } else {
      statementText = "update schema_version set version_number = ?";
      try (var statement =
             connection.prepareStatement(statementText)) {
        statement.setLong(1, version.longValueExact());
        statement.execute();
      }
    }
  }

  private static Optional<BigInteger> schemaVersionGet(
    final ARDBConfiguration configuration,
    final Connection connection)
    throws SQLException
  {
    Objects.requireNonNull(connection, "connection");

    try {
      final var statementText =
        "SELECT version_application_id, version_number FROM schema_version";
      LOG.debug("execute: {}", statementText);

      try (var statement = connection.prepareStatement(statementText)) {
        try (var result = statement.executeQuery()) {
          if (!result.next()) {
            throw new SQLException("schema_version table is empty!");
          }
          final var applicationCA =
            result.getString(1);
          final var version =
            result.getLong(2);

          final var id = configuration.applicationIdText().value();
          if (!Objects.equals(applicationCA, id)) {
            throw new SQLException(
              String.format(
                "Database application ID is %s but should be %s",
                applicationCA,
                id
              )
            );
          }

          return Optional.of(valueOf(version));
        }
      }
    } catch (final SQLException e) {
      if (e.getErrorCode() == SQLiteErrorCode.SQLITE_ERROR.code) {
        connection.rollback();
        return Optional.empty();
      }
      throw e;
    }
  }

  private static ARDB connect(
    final ARDBConfiguration configuration,
    final Map<Class<?>, ARDBQueryType<?, ?>> queries)
  {
    final var url = new StringBuilder(128);
    url.append("jdbc:sqlite:");
    url.append(configuration.databaseFile());

    final var config = new SQLiteConfig();
    config.setApplicationId(configuration.applicationId());
    config.enforceForeignKeys(true);

    final var dataSource = new SQLiteDataSource(config);
    dataSource.setUrl(url.toString());
    return new ARDB(dataSource, queries);
  }

  private static void createOrUpgrade(
    final ARDBConfiguration configuration,
    final Consumer<String> startupMessages)
    throws ARException
  {
    final var resources =
      ARCloseables.create();
    final var arguments =
      new TrArguments(Map.of());

    try (var ignored1 = resources) {
      final var url = new StringBuilder(128);
      url.append("jdbc:sqlite:");
      url.append(configuration.databaseFile());

      final var config = new SQLiteConfig();
      config.setApplicationId(configuration.applicationId());
      config.enforceForeignKeys(true);

      final var dataSource = new SQLiteDataSource(config);
      dataSource.setUrl(url.toString());

      final var parsers = new TrSchemaRevisionSetParsers();
      final TrSchemaRevisionSet revisions;

      try (var stream = schemaText()) {
        revisions = parsers.parse(URI.create("urn:source"), stream);
      }

      try (var connection = dataSource.getConnection()) {
        connection.setAutoCommit(false);

        new TrExecutors().create(
          new TrExecutorConfiguration(
            c -> {
              return schemaVersionGet(configuration, c);
            },
            (v, c) -> {
              schemaVersionSet(configuration, v, c);
            },
            event -> publishTrEvent(startupMessages, event),
            revisions,
            PERFORM_UPGRADES,
            arguments,
            connection
          )
        ).execute();
        connection.commit();
      }
    } catch (final Exception e) {
      throw ARDBExceptions.wrap(e);
    }
  }

  private static InputStream schemaText()
  {
    return ARDBFactory.class.getResourceAsStream(
      "/com/io7m/aradine/database/sqlite3/Database.xml"
    );
  }

  private static void publishEvent(
    final Consumer<String> startupMessages,
    final String message)
  {
    try {
      LOG.trace("{}", message);
      startupMessages.accept(message);
    } catch (final Exception e) {
      LOG.error("Ignored consumer exception: ", e);
    }
  }

  private static void publishTrEvent(
    final Consumer<String> startupMessages,
    final TrEventType event)
  {
    switch (event) {
      case final TrEventExecutingSQL sql -> {
        publishEvent(
          startupMessages,
          String.format("Executing SQL: %s", sql.statement())
        );
        return;
      }
      case final TrEventUpgrading upgrading -> {
        publishEvent(
          startupMessages,
          String.format(
            "Upgrading database from version %s -> %s",
            upgrading.fromVersion(),
            upgrading.toVersion())
        );
        return;
      }
    }
  }

  private static HashMap<Class<?>, ARDBQueryType<?, ?>> combineQueries(
    final ARDBConfiguration configuration)
    throws ARException
  {
    final var queryList =
      configuration.queries();
    final var queryMap =
      new HashMap<Class<?>, ARDBQueryType<?, ?>>(queryList.size());

    for (final var query : queryList) {
      final var queryInterface =
        query.queryInterface();
      final var existing =
        queryMap.get(queryInterface);

      if (existing != null) {
        throw errorDuplicateQuery(query, existing);
      }
      queryMap.put(queryInterface, query.queryInstance());
    }
    return queryMap;
  }

  private static ARException errorDuplicateQuery(
    final ARDBQueryProviderType query,
    final ARDBQueryType<?, ?> existing)
  {
    return new ARException(
      "Multiple queries registered under the same interface type.",
      "error-query-conflict",
      Map.ofEntries(
        Map.entry(
          "Query Interface",
          query.queryInterface().getCanonicalName()
        ),
        Map.entry(
          "Query (Existing)",
          existing.getClass().getCanonicalName()
        ),
        Map.entry(
          "Query (New)",
          query.queryInstance().getClass().getCanonicalName()
        )
      ),
      Optional.empty()
    );
  }

  /**
   * Open a database.
   *
   * @param configuration The configuration
   *
   * @return A database
   *
   * @throws ARException On errors
   */

  public ARDBType open(
    final ARDBConfiguration configuration)
    throws ARException
  {
    Objects.requireNonNull(configuration, "configuration");

    final var queryMap = combineQueries(configuration);
    createOrUpgrade(
      configuration, _ -> {
      });
    return connect(configuration, queryMap);
  }

  @Override
  public String toString()
  {
    return "[ARDBFactory 0x%s]"
      .formatted(Long.toUnsignedString(this.hashCode(), 16));
  }
}
