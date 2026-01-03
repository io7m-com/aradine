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
import com.io7m.aradine.api.instrument.ARInstrumentID;
import com.io7m.aradine.database.api.ARDBQueryProviderType;
import com.io7m.aradine.database.api.ARDBQueryType;
import com.io7m.aradine.database.api.ARDBTransactionType;
import com.io7m.aradine.database.api.ARDBUnit;
import com.io7m.aradine.inventory.api.queries.ARQueryInstrumentDeleteType;
import com.io7m.verona.core.VersionQualifier;

enum ARQueryInstrumentDelete
  implements ARQueryInstrumentDeleteType, ARDBQueryProviderType
{
  INSTANCE;

  private static final String QUERY_TEXT = """
    DELETE FROM inventory_instruments
      WHERE inventory_instruments.instrument_group             = $1
        AND inventory_instruments.instrument_name              = $2
        AND inventory_instruments.instrument_version_major     = $3
        AND inventory_instruments.instrument_version_minor     = $4
        AND inventory_instruments.instrument_version_patch     = $5
        AND inventory_instruments.instrument_version_qualifier = $6
    """;

  @Override
  public ARDBUnit execute(
    final ARDBTransactionType transaction,
    final ARInstrumentID parameters)
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
      st.execute();
      return ARDBUnit.UNIT;
    } catch (final Exception e) {
      throw ARInventoryExceptions.wrap(e);
    }
  }

  @Override
  public Class<?> queryInterface()
  {
    return ARQueryInstrumentDeleteType.class;
  }

  @Override
  public ARDBQueryType<?, ?> queryInstance()
  {
    return this;
  }
}
