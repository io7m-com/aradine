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

package com.io7m.aradine.inventory.internal;

import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.instrument.ARInstrumentDataSummary;
import com.io7m.aradine.api.instrument.ARInstrumentID;
import com.io7m.aradine.database.api.ARDBQueryProviderType;
import com.io7m.aradine.database.api.ARDBQueryType;
import com.io7m.aradine.database.api.ARDBTransactionType;
import com.io7m.aradine.inventory.api.queries.ARQueryInstrumentListType;
import com.io7m.lanark.core.RDottedName;
import com.io7m.verona.core.Version;
import com.io7m.verona.core.VersionException;
import com.io7m.verona.core.VersionQualifier;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

enum ARQueryInstrumentList
  implements ARQueryInstrumentListType, ARDBQueryProviderType
{
  INSTANCE;

  private static final String QUERY_TEXT = """
    SELECT
      inventory_instruments.instrument_group,
      inventory_instruments.instrument_name,
      inventory_instruments.instrument_version_major,
      inventory_instruments.instrument_version_minor,
      inventory_instruments.instrument_version_patch,
      inventory_instruments.instrument_version_qualifier,
      inventory_instruments.instrument_title,
      inventory_instruments.instrument_description
    FROM inventory_instruments
    ORDER BY
      inventory_instruments.instrument_group,
      inventory_instruments.instrument_name,
      inventory_instruments.instrument_version_major,
      inventory_instruments.instrument_version_minor,
      inventory_instruments.instrument_version_patch,
      inventory_instruments.instrument_version_qualifier
    LIMIT $1
    """;

  private static final String QUERY_TEXT_WITH_OFFSET = """
    SELECT
      inventory_instruments.instrument_group,
      inventory_instruments.instrument_name,
      inventory_instruments.instrument_version_major,
      inventory_instruments.instrument_version_minor,
      inventory_instruments.instrument_version_patch,
      inventory_instruments.instrument_version_qualifier,
      inventory_instruments.instrument_title,
      inventory_instruments.instrument_description
    FROM inventory_instruments
    WHERE (
      inventory_instruments.instrument_group,
      inventory_instruments.instrument_name,
      inventory_instruments.instrument_version_major,
      inventory_instruments.instrument_version_minor,
      inventory_instruments.instrument_version_patch,
      inventory_instruments.instrument_version_qualifier
    ) > ($1, $2, $3, $4, $5, $6)
    ORDER BY
      inventory_instruments.instrument_group,
      inventory_instruments.instrument_name,
      inventory_instruments.instrument_version_major,
      inventory_instruments.instrument_version_minor,
      inventory_instruments.instrument_version_patch,
      inventory_instruments.instrument_version_qualifier
    LIMIT $7
    """;

  private static List<ARInstrumentDataSummary> parseSummaries(
    final ResultSet rs,
    final Parameters parameters)
    throws SQLException, VersionException
  {
    final var results =
      new ArrayList<ARInstrumentDataSummary>(parameters.limit());

    while (rs.next()) {
      results.add(parseSummary(rs));
    }
    return results;
  }

  private static ARInstrumentDataSummary parseSummary(
    final ResultSet rs)
    throws SQLException
  {
    return new ARInstrumentDataSummary(
      new ARInstrumentID(
        new RDottedName(rs.getString("instrument_group")),
        new RDottedName(rs.getString("instrument_name")),
        parseVersion(rs)
      ),
      rs.getString("instrument_title"),
      rs.getString("instrument_description")
    );
  }

  private static Version parseVersion(
    final ResultSet rs)
    throws SQLException
  {
    final var qualifierText =
      rs.getString("instrument_version_qualifier");

    final Optional<VersionQualifier> qualifier;
    if (Objects.equals(qualifierText, "")) {
      qualifier = Optional.empty();
    } else {
      qualifier = Optional.of(new VersionQualifier(qualifierText));
    }

    return new Version(
      rs.getInt("instrument_version_major"),
      rs.getInt("instrument_version_minor"),
      rs.getInt("instrument_version_patch"),
      qualifier
    );
  }

  private static List<ARInstrumentDataSummary> executeWithoutOffset(
    final ARDBTransactionType transaction,
    final Parameters parameters)
    throws SQLException, VersionException
  {
    final var connection =
      transaction.connection().connection();

    try (var st = connection.prepareStatement(QUERY_TEXT)) {
      st.setInt(1, parameters.limit());
      try (var rs = st.executeQuery()) {
        return parseSummaries(rs, parameters);
      }
    }
  }

  private static List<ARInstrumentDataSummary> executeWithOffset(
    final ARDBTransactionType transaction,
    final Parameters parameters)
    throws SQLException, VersionException
  {
    final var connection =
      transaction.connection().connection();
    final var start =
      parameters.start().orElseThrow();
    final var qualifier =
      start.version()
        .qualifier()
        .map(VersionQualifier::text)
        .orElse("");

    try (var st = connection.prepareStatement(QUERY_TEXT_WITH_OFFSET)) {
      st.setString(1, start.group().value());
      st.setString(2, start.name().value());
      st.setInt(3, start.version().major());
      st.setInt(4, start.version().minor());
      st.setInt(5, start.version().patch());
      st.setString(6, qualifier);
      st.setInt(7, parameters.limit());
      try (var rs = st.executeQuery()) {
        return parseSummaries(rs, parameters);
      }
    }
  }

  @Override
  public List<ARInstrumentDataSummary> execute(
    final ARDBTransactionType transaction,
    final Parameters parameters)
    throws ARException
  {
    try {
      if (parameters.start().isPresent()) {
        return executeWithOffset(transaction, parameters);
      } else {
        return executeWithoutOffset(transaction, parameters);
      }
    } catch (final Exception e) {
      throw ARInventoryExceptions.wrap(e);
    }
  }

  @Override
  public Class<?> queryInterface()
  {
    return ARQueryInstrumentListType.class;
  }

  @Override
  public ARDBQueryType<?, ?> queryInstance()
  {
    return this;
  }
}
