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
import com.io7m.aradine.api.data.ARHash;
import com.io7m.aradine.api.instrument.ARInstrumentID;
import com.io7m.aradine.api.sample_map.ARSampleMapID;
import com.io7m.aradine.database.api.ARDBQueryProviderType;
import com.io7m.aradine.database.api.ARDBQueryType;
import com.io7m.aradine.database.api.ARDBTransactionType;
import com.io7m.aradine.inventory.api.queries.ARQueryBlobReferencesType;
import com.io7m.lanark.core.RDottedName;
import com.io7m.verona.core.Version;
import com.io7m.verona.core.VersionQualifier;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

enum ARQueryBlobReferences
  implements ARQueryBlobReferencesType, ARDBQueryProviderType
{
  INSTANCE;

  private static final String QUERY_INSTRUMENTS_TEXT = """
    SELECT
      instrument_group,
      instrument_name,
      instrument_version_major,
      instrument_version_minor,
      instrument_version_patch,
      instrument_version_qualifier
    FROM inventory_instruments
      WHERE instrument_blob = $1
    """;

  private static final String QUERY_SAMPLE_MAP_TEXT = """
    SELECT
      sample_map_group,
      sample_map_name,
      sample_map_version_major,
      sample_map_version_minor,
      sample_map_version_patch,
      sample_map_version_qualifier
    FROM inventory_sample_maps
      WHERE sample_map_blob = $1
    """;

  private static final String QUERY_BLOB_TEXT = """
    SELECT
      blob_id
    FROM inventory_blobs
      WHERE blob_hash_algorithm = $1
        AND blob_hash_value     = $2
    """;

  @Override
  public References execute(
    final ARDBTransactionType transaction,
    final ARHash parameters)
    throws ARException
  {
    final var connection =
      transaction.connection().connection();

    try {
      final var blobIdOpt =
        findBlobID(connection, parameters);

      if (blobIdOpt.isPresent()) {
        final var blobId = blobIdOpt.get();
        return new References(
          instrumentReferences(connection, blobId),
          sampleMapReferences(connection, blobId)
        );
      }
      return new References(Set.of(), Set.of());
    } catch (final Exception e) {
      throw ARInventoryExceptions.wrap(e);
    }
  }

  private static Set<ARSampleMapID> sampleMapReferences(
    final Connection connection,
    final Long blobId)
    throws Exception
  {
    try (var st = connection.prepareStatement(QUERY_SAMPLE_MAP_TEXT)) {
      st.setLong(1, blobId.longValue());
      try (var rs = st.executeQuery()) {
        final var results = new HashSet<ARSampleMapID>();
        while (rs.next()) {
          final var qTest =
            rs.getString("sample_map_version_qualifier");

          final Optional<VersionQualifier> qualifier;
          if (!qTest.isEmpty()) {
            qualifier = Optional.of(new VersionQualifier(qTest));
          } else {
            qualifier = Optional.empty();
          }

          results.add(
            new ARSampleMapID(
              new RDottedName(rs.getString("sample_map_group")),
              new RDottedName(rs.getString("sample_map_name")),
              new Version(
                rs.getInt("sample_map_version_major"),
                rs.getInt("sample_map_version_minor"),
                rs.getInt("sample_map_version_patch"),
                qualifier
              )
            )
          );
        }
        return results;
      }
    }
  }

  private static Set<ARInstrumentID> instrumentReferences(
    final Connection connection,
    final Long blobId)
    throws Exception
  {
    try (var st = connection.prepareStatement(QUERY_INSTRUMENTS_TEXT)) {
      st.setLong(1, blobId.longValue());
      try (var rs = st.executeQuery()) {
        final var results = new HashSet<ARInstrumentID>();
        while (rs.next()) {
          final var qTest =
            rs.getString("instrument_version_qualifier");

          final Optional<VersionQualifier> qualifier;
          if (!qTest.isEmpty()) {
            qualifier = Optional.of(new VersionQualifier(qTest));
          } else {
            qualifier = Optional.empty();
          }

          results.add(
            new ARInstrumentID(
              new RDottedName(rs.getString("instrument_group")),
              new RDottedName(rs.getString("instrument_name")),
              new Version(
                rs.getInt("instrument_version_major"),
                rs.getInt("instrument_version_minor"),
                rs.getInt("instrument_version_patch"),
                qualifier
              )
            )
          );
        }
        return results;
      }
    }
  }

  private static Optional<Long> findBlobID(
    final Connection connection,
    final ARHash parameters)
    throws Exception
  {
    try (var st = connection.prepareStatement(QUERY_BLOB_TEXT)) {
      st.setString(1, parameters.algorithm().name());
      st.setString(2, parameters.value());
      try (var rs = st.executeQuery()) {
        if (rs.next()) {
          return Optional.of(Long.valueOf(rs.getLong(1)));
        } else {
          return Optional.empty();
        }
      }
    }
  }

  @Override
  public Class<?> queryInterface()
  {
    return ARQueryBlobReferencesType.class;
  }

  @Override
  public ARDBQueryType<?, ?> queryInstance()
  {
    return this;
  }
}
