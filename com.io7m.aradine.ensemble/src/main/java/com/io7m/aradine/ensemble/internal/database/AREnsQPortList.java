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
import com.io7m.aradine.api.instrument.ARInstrumentInstanceID;
import com.io7m.aradine.api.ports.ARPort;
import com.io7m.aradine.api.ports.ARPortDirection;
import com.io7m.aradine.api.ports.ARPortID;
import com.io7m.aradine.api.ports.ARPortKind;
import com.io7m.aradine.api.ports.ARPortNumber;
import com.io7m.aradine.database.api.ARDBQueryProviderType;
import com.io7m.aradine.database.api.ARDBTransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * List ports.
 */

public enum AREnsQPortList
  implements AREnsQPortListType, ARDBQueryProviderType
{
  /**
   * List ports.
   */

  INSTANCE;

  private static final String QUERY_TEXT = """
    SELECT
      ports.port_id,
      ports.port_instrument_instance,
      ports.port_kind,
      ports.port_direction,
      ports.port_number,
      ports.port_label,
      ports.port_semantics
    FROM ports
    ORDER BY ports.port_id ASC
    LIMIT $1
    """;

  private static final String QUERY_TEXT_WITH_START = """
    SELECT
      ports.port_id,
      ports.port_instrument_instance,
      ports.port_kind,
      ports.port_direction,
      ports.port_number,
      ports.port_label,
      ports.port_semantics
    FROM ports
    WHERE ports.port_id > $1
    ORDER BY ports.port_id ASC
    LIMIT $2
    """;

  private static List<ARPort> readResults(
    final PreparedStatement st)
    throws SQLException
  {
    final var results = new ArrayList<ARPort>();
    try (var rs = st.executeQuery()) {
      while (rs.next()) {
        final var portId =
          new ARPortID(UUID.fromString(rs.getString("port_id")));
        final var kind =
          ARPortKind.valueOf(rs.getString("port_kind"));
        final var direction =
          ARPortDirection.valueOf(rs.getString("port_direction"));
        final var number =
          new ARPortNumber(rs.getLong("port_number"));
        final var label =
          rs.getString("port_label");
        final Set<String> semantics =
          semanticsOf(rs.getString("port_semantics"));

        results.add(
          new ARPort(
            new ARInstrumentInstanceID(
              UUID.fromString(rs.getString("port_instrument_instance"))
            ),
            portId,
            kind,
            direction,
            number,
            label,
            semantics
          )
        );
      }
    }
    return List.copyOf(results);
  }

  private static Set<String> semanticsOf(
    final String text)
  {
    return Stream.of(text.split(","))
      .filter(s -> !s.isEmpty())
      .collect(Collectors.toSet());
  }

  private static List<ARPort> executeWithoutStart(
    final Connection connection,
    final AREnsQPortListType.Parameters parameters)
    throws ARException
  {
    try (var st = connection.prepareStatement(QUERY_TEXT)) {
      st.setInt(1, parameters.limit());
      return readResults(st);
    } catch (final Exception e) {
      throw AREnsDBExceptions.wrap(e);
    }
  }

  private static List<ARPort> executeWithStart(
    final Connection connection,
    final AREnsQPortListType.Parameters parameters,
    final ARPort portStart)
    throws ARException
  {
    try (var st = connection.prepareStatement(QUERY_TEXT_WITH_START)) {
      st.setString(1, portStart.id().toString());
      st.setInt(2, parameters.limit());
      return readResults(st);
    } catch (final Exception e) {
      throw AREnsDBExceptions.wrap(e);
    }
  }

  @Override
  public Class<?> queryInterface()
  {
    return AREnsQPortListType.class;
  }

  @Override
  public AREnsQPortListType queryInstance()
  {
    return this;
  }

  @Override
  public List<ARPort> execute(
    final ARDBTransactionType transaction,
    final AREnsQPortListType.Parameters parameters)
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
