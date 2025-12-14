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

import com.io7m.aradine.inventory.api.ARInventoryBlob;
import com.io7m.aradine.inventory.api.ARInventoryException;
import com.io7m.aradine.inventory.api.ARInventoryHash;
import com.io7m.aradine.inventory.api.ARInventoryHashAlgorithm;
import com.io7m.aradine.inventory.api.ARInventoryTransactionType;
import com.io7m.aradine.inventory.api.queries.ARQueryBlobGetType;
import com.io7m.mime2045.parser.MimeParsers;
import com.io7m.mime2045.parser.api.MimeParseException;

import java.sql.SQLException;
import java.util.Optional;

enum ARQueryBlobGet implements ARQueryBlobGetType
{
  INSTANCE;

  private static final MimeParsers MIME_PARSERS =
    new MimeParsers();

  private static final String QUERY_TEXT = """
    SELECT
      blobs.blob_size,
      blobs.blob_hash_algorithm,
      blobs.blob_hash_value,
      blobs.blob_type
    FROM blobs
      WHERE blobs.blob_hash_algorithm = $1
        AND blobs.blob_hash_value = $2
      LIMIT 1
    """;

  @Override
  public Optional<ARInventoryBlob> execute(
    final ARInventoryTransactionType transaction,
    final ARInventoryHash parameters)
    throws ARInventoryException
  {
    final var tr = (ARInventoryDB.ARInventoryDBTransaction) transaction;
    final var connection = tr.connection().connection();

    try (var st = connection.prepareStatement(QUERY_TEXT)) {
      st.setString(1, parameters.algorithm().name());
      st.setString(2, parameters.value());
      try (var rs = st.executeQuery()) {
        if (rs.next()) {
          return Optional.of(
            new ARInventoryBlob(
              rs.getLong(1),
              new ARInventoryHash(
                ARInventoryHashAlgorithm.valueOf(rs.getString(2)),
                rs.getString(3)
              ),
              MIME_PARSERS.parse(rs.getString(4))
            )
          );
        }
      }
      return Optional.empty();
    } catch (final SQLException | MimeParseException e) {
      throw ARInventoryExceptions.wrap(e);
    }
  }
}
