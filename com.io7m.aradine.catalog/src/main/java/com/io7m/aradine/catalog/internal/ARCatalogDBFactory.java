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

package com.io7m.aradine.catalog.internal;

import com.io7m.aradine.catalog.api.ARCatalogException;
import com.io7m.seltzer.api.SStructuredErrorType;
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

import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static com.io7m.trasco.api.TrExecutorUpgrade.PERFORM_UPGRADES;
import static java.math.BigInteger.valueOf;

/**
 * The SQLite database implementation.
 */

public final class ARCatalogDBFactory
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ARCatalogDBFactory.class);

  private static final String DATABASE_APPLICATION_ID =
    "com.io7m.aradine.catalog";
  private static final int APPLICATION_ID =
    0x64826784;

  /**
   * The SQLite database implementation.
   */

  public ARCatalogDBFactory()
  {

  }

  private static void schemaVersionSet(
    final BigInteger version,
    final Connection connection)
    throws SQLException
  {
    final String statementText;
    if (Objects.equals(version, BigInteger.ZERO)) {
      statementText = "insert into schema_version (version_application_id, version_number) values (?, ?)";
      try (var statement =
             connection.prepareStatement(statementText)) {
        statement.setString(1, DATABASE_APPLICATION_ID);
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

          if (!Objects.equals(applicationCA, DATABASE_APPLICATION_ID)) {
            throw new SQLException(
              String.format(
                "Database application ID is %s but should be %s",
                applicationCA,
                DATABASE_APPLICATION_ID
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

  private static ARCatalogDB connect(
    final Path file)
  {
    final var url = new StringBuilder(128);
    url.append("jdbc:sqlite:");
    url.append(file);

    final var config = new SQLiteConfig();
    config.setApplicationId(APPLICATION_ID);
    config.enforceForeignKeys(true);

    final var dataSource = new SQLiteDataSource(config);
    dataSource.setUrl(url.toString());
    return new ARCatalogDB(dataSource);
  }

  private static void createOrUpgrade(
    final Path file,
    final Consumer<String> startupMessages)
    throws ARCatalogException
  {
    final var resources =
      ARCloseables.create();
    final var arguments =
      new TrArguments(Map.of());

    try (var ignored1 = resources) {
      final var url = new StringBuilder(128);
      url.append("jdbc:sqlite:");
      url.append(file);

      final var config = new SQLiteConfig();
      config.setApplicationId(APPLICATION_ID);
      config.enforceForeignKeys(true);

      final var dataSource = new SQLiteDataSource(config);
      dataSource.setUrl(url.toString());

      final var parsers = new TrSchemaRevisionSetParsers();
      final TrSchemaRevisionSet revisions;
      try (var stream = ARCatalogDBFactory.class.getResourceAsStream(
        "/com/io7m/aradine/catalog/internal/Database.xml")) {
        revisions = parsers.parse(URI.create("urn:source"), stream);
      }

      try (var connection = dataSource.getConnection()) {
        connection.setAutoCommit(false);

        new TrExecutors().create(
          new TrExecutorConfiguration(
            ARCatalogDBFactory::schemaVersionGet,
            ARCatalogDBFactory::schemaVersionSet,
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
      throw wrapException(e);
    }
  }

  private static ARCatalogException wrapException(
    final Exception e)
  {
    if (e instanceof final SStructuredErrorType<?> se) {
      return new ARCatalogException(
        se.message(),
        e,
        se.errorCode().toString(),
        se.attributes(),
        se.remediatingAction()
      );
    }

    return new ARCatalogException(
      Objects.requireNonNullElse(e.getMessage(), e.getClass().getSimpleName()),
      e,
      "error-exception",
      Map.of(),
      Optional.empty()
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

  /**
   * Open a database.
   *
   * @param file The file
   *
   * @return A database
   *
   * @throws ARCatalogException On errors
   */

  public ARCatalogDB open(
    final Path file)
    throws ARCatalogException
  {
    Objects.requireNonNull(file, "file");
    createOrUpgrade(
      file, message -> {

      });
    return connect(file);
  }

  @Override
  public String toString()
  {
    return "[ARCatalogDBFactory 0x%s]"
      .formatted(Long.toUnsignedString(this.hashCode(), 16));
  }
}
