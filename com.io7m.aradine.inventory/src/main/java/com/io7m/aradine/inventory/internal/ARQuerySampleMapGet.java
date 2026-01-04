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

import com.io7m.aradine.api.ARBlob;
import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.ARHash;
import com.io7m.aradine.api.ARHashAlgorithm;
import com.io7m.aradine.api.sample_map.ARSampleMapDataSummary;
import com.io7m.aradine.api.sample_map.ARSampleMapID;
import com.io7m.aradine.database.api.ARDBQueryProviderType;
import com.io7m.aradine.database.api.ARDBQueryType;
import com.io7m.aradine.database.api.ARDBTransactionType;
import com.io7m.aradine.inventory.api.queries.ARQuerySampleMapGetType;
import com.io7m.mime2045.parser.MimeParsers;
import com.io7m.verona.core.VersionQualifier;

import java.util.Optional;

enum ARQuerySampleMapGet
  implements ARQuerySampleMapGetType, ARDBQueryProviderType
{
  INSTANCE;

  private static final MimeParsers MIME_PARSERS =
    new MimeParsers();

  private static final String QUERY_TEXT = """
    SELECT
      inventory_sample_maps.sample_map_group,
      inventory_sample_maps.sample_map_name,
      inventory_sample_maps.sample_map_version_major,
      inventory_sample_maps.sample_map_version_minor,
      inventory_sample_maps.sample_map_version_patch,
      inventory_sample_maps.sample_map_version_qualifier,
      inventory_sample_maps.sample_map_title,
      inventory_sample_maps.sample_map_description,
      inventory_blobs.blob_size,
      inventory_blobs.blob_hash_algorithm,
      inventory_blobs.blob_hash_value,
      inventory_blobs.blob_type
    FROM inventory_sample_maps
    JOIN inventory_blobs ON inventory_sample_maps.sample_map_blob = inventory_blobs.blob_id
    WHERE inventory_sample_maps.sample_map_group             = $1
      AND inventory_sample_maps.sample_map_name              = $2
      AND inventory_sample_maps.sample_map_version_major     = $3
      AND inventory_sample_maps.sample_map_version_minor     = $4
      AND inventory_sample_maps.sample_map_version_patch     = $5
      AND inventory_sample_maps.sample_map_version_qualifier = $6
      LIMIT 1
    """;

  @Override
  public Optional<ARSampleMapDataSummary> execute(
    final ARDBTransactionType transaction,
    final ARSampleMapID parameters)
    throws ARException
  {
    final var connection =
      transaction.connection().connection();

    final var version = parameters.version();
    final var qualifier =
      version.qualifier()
        .map(VersionQualifier::text)
        .orElse("");

    try (var st = connection.prepareStatement(QUERY_TEXT)) {
      st.setString(1, parameters.group().value());
      st.setString(2, parameters.name().value());
      st.setInt(3, version.major());
      st.setInt(4, version.minor());
      st.setInt(5, version.patch());
      st.setString(6, qualifier);

      try (var rs = st.executeQuery()) {
        if (rs.next()) {
          final var hash =
            new ARHash(
              ARHashAlgorithm.valueOf(
                rs.getString("blob_hash_algorithm")
              ),
              rs.getString("blob_hash_value")
            );

          final var blob =
            new ARBlob(
              rs.getLong("blob_size"),
              hash,
              MIME_PARSERS.parse(rs.getString("blob_type"))
            );

          return Optional.of(
            new ARSampleMapDataSummary(
              parameters,
              rs.getString("sample_map_title"),
              rs.getString("sample_map_description"),
              blob
            )
          );
        }
      }
      return Optional.empty();
    } catch (final Exception e) {
      throw ARInventoryExceptions.wrap(e);
    }
  }

  @Override
  public Class<?> queryInterface()
  {
    return ARQuerySampleMapGetType.class;
  }

  @Override
  public ARDBQueryType<?, ?> queryInstance()
  {
    return this;
  }
}
