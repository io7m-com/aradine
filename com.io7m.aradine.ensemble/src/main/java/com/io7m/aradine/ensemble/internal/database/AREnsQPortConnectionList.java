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
import com.io7m.aradine.api.ports.ARPortConnection;
import com.io7m.aradine.api.ports.ARPortID;
import com.io7m.aradine.database.api.ARDBQueryProviderType;
import com.io7m.aradine.database.api.ARDBTransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * List port connections.
 */

public enum AREnsQPortConnectionList
  implements AREnsQPortConnectionListType, ARDBQueryProviderType
{
  /**
   * List port connections.
   */

  INSTANCE;

  private static final String QUERY_TEXT = """
    SELECT
      port_connections.port_connection_source,
      port_connections.port_connection_target
    FROM port_connections
    ORDER BY
      port_connections.port_connection_source,
      port_connections.port_connection_target
    ASC
    LIMIT $1
    """;

  private static final String QUERY_TEXT_WITH_START = """
    SELECT
      port_connections.port_connection_source,
      port_connections.port_connection_target
    FROM port_connections
    WHERE (
      port_connections.port_connection_source,
      port_connections.port_connection_target
    ) > ($1, $2)
    ORDER BY
      port_connections.port_connection_source,
      port_connections.port_connection_target
    ASC
    LIMIT $3
    """;

  private static List<ARPortConnection> readResults(
    final PreparedStatement st)
    throws SQLException
  {
    final var results = new ArrayList<ARPortConnection>();
    try (var rs = st.executeQuery()) {
      while (rs.next()) {
        results.add(
          new ARPortConnection(
            new ARPortID(UUID.fromString(
              rs.getString("port_connection_source")
            )),
            new ARPortID(UUID.fromString(
              rs.getString("port_connection_target")
            ))
          )
        );
      }
    }
    return List.copyOf(results);
  }

  private static List<ARPortConnection> executeWithoutStart(
    final Connection connection,
    final Parameters parameters)
    throws ARException
  {
    try (var st = connection.prepareStatement(QUERY_TEXT)) {
      st.setInt(1, parameters.limit());
      return readResults(st);
    } catch (final Exception e) {
      throw AREnsDBExceptions.wrap(e);
    }
  }

  private static List<ARPortConnection> executeWithStart(
    final Connection connection,
    final Parameters parameters,
    final ARPortConnection connectionStart)
    throws ARException
  {
    try (var st = connection.prepareStatement(QUERY_TEXT_WITH_START)) {
      st.setString(1, connectionStart.portSource().toString());
      st.setString(2, connectionStart.portTarget().toString());
      st.setInt(3, parameters.limit());
      return readResults(st);
    } catch (final Exception e) {
      throw AREnsDBExceptions.wrap(e);
    }
  }

  @Override
  public Class<?> queryInterface()
  {
    return AREnsQPortConnectionListType.class;
  }

  @Override
  public AREnsQPortConnectionListType queryInstance()
  {
    return this;
  }

  @Override
  public List<ARPortConnection> execute(
    final ARDBTransactionType transaction,
    final Parameters parameters)
    throws ARException
  {
    final var connection =
      transaction.connection().connection();

    final var startOpt = parameters.start();
    if (startOpt.isPresent()) {
      return executeWithStart(connection, parameters, startOpt.get());
    } else {
      return executeWithoutStart(connection, parameters);
    }
  }
}
