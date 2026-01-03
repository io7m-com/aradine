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
import com.io7m.aradine.api.instrument.ARInstrumentData;
import com.io7m.aradine.database.api.ARDBQueryProviderType;
import com.io7m.aradine.database.api.ARDBQueryType;
import com.io7m.aradine.database.api.ARDBTransactionType;
import com.io7m.aradine.inventory.api.queries.ARInventoryUnit;
import com.io7m.aradine.inventory.api.queries.ARQueryInstrumentPutType;
import com.io7m.verona.core.VersionQualifier;

enum ARQueryInstrumentPut
  implements ARQueryInstrumentPutType, ARDBQueryProviderType
{
  INSTANCE;

  private static final String QUERY_TEXT = """
    INSERT INTO inventory_instruments (
      instrument_group,
      instrument_name,
      instrument_version_major,
      instrument_version_minor,
      instrument_version_patch,
      instrument_version_qualifier,
      instrument_meta_format,
      instrument_meta,
      instrument_title,
      instrument_description,
      instrument_blob
    ) VALUES (
      $1,
      $2,
      $3,
      $4,
      $5,
      $6,
      $7,
      $8,
      $9,
      $10,
      (
        SELECT blob_id
          FROM inventory_blobs
          WHERE blob_hash_algorithm = $11
            AND blob_hash_value     = $12
              LIMIT 1
      )
    ) ON CONFLICT DO UPDATE SET
      instrument_meta_format = $7,
      instrument_meta        = $8,
      instrument_title       = $9,
      instrument_description = $10,
      instrument_blob        =
        (
          SELECT blob_id
            FROM inventory_blobs
            WHERE blob_hash_algorithm = $11
              AND blob_hash_value     = $12
                LIMIT 1
        )
    """;

  @Override
  public ARInventoryUnit execute(
    final ARDBTransactionType transaction,
    final ARInstrumentData parameters)
    throws ARException
  {
    final var connection =
      transaction.connection().connection();

    final var identifier = parameters.identifier();
    final var blob = parameters.blob();
    final var hash = blob.hash();
    final var version = identifier.version();
    final var qualifier =
      version.qualifier()
        .map(VersionQualifier::text)
        .orElse("");

    try (var st = connection.prepareStatement(QUERY_TEXT)) {
      st.setString(1, identifier.group().value());
      st.setString(2, identifier.name().value());
      st.setInt(3, version.major());
      st.setInt(4, version.minor());
      st.setInt(5, version.patch());
      st.setString(6, qualifier);
      st.setString(7, parameters.metadataFormat().value());
      st.setBytes(8, parameters.metadataText().data());
      st.setString(9, parameters.title());
      st.setString(10, parameters.description());
      st.setString(11, hash.algorithm().name());
      st.setString(12, hash.value());
      st.execute();
      return ARInventoryUnit.UNIT;
    } catch (final Exception e) {
      throw ARInventoryExceptions.wrap(e);
    }
  }

  @Override
  public Class<?> queryInterface()
  {
    return ARQueryInstrumentPutType.class;
  }

  @Override
  public ARDBQueryType<?, ?> queryInstance()
  {
    return this;
  }
}
