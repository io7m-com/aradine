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

import com.io7m.aradine.api.instrument.ARInstrumentID;
import com.io7m.aradine.api.instrument.ARInstrumentReference;
import com.io7m.aradine.database.api.ARDBException;
import com.io7m.aradine.database.api.ARDBQueryProviderType;
import com.io7m.aradine.database.api.ARDBTransactionType;
import com.io7m.aradine.database.api.ARDBUnit;
import com.io7m.verona.core.VersionQualifier;

/**
 * Put/update an instrument.
 */

public enum AREnsQInstrumentPut
  implements AREnsQInstrumentPutType, ARDBQueryProviderType
{
  /**
   * Put/update an instrument.
   */

  INSTANCE;

  private static final String QUERY_TEXT = """
    INSERT INTO instruments (
      instrument_instance_id,
      instrument_group,
      instrument_name,
      instrument_version_major,
      instrument_version_minor,
      instrument_version_patch,
      instrument_qualifier
    ) VALUES (
      $1,
      $2,
      $3,
      $4,
      $5,
      $6,
      $7
    ) ON CONFLICT DO UPDATE SET
      instrument_group              = $2
      instrument_name               = $3,
      instrument_version_major      = $4,
      instrument_version_minor      = $5,
      instrument_version_patch      = $6,
      instrument_qualifier          = $7
    """;

  @Override
  public Class<?> queryInterface()
  {
    return AREnsQInstrumentPutType.class;
  }

  @Override
  public AREnsQInstrumentPutType queryInstance()
  {
    return this;
  }

  @Override
  public ARDBUnit execute(
    final ARDBTransactionType transaction,
    final ARInstrumentReference parameters)
    throws ARDBException
  {
    final var connection =
      transaction.connection().connection();

    final var instanceId =
      parameters.instanceID();
    final var instrumentId =
      parameters.identifier();

    try (var st = connection.prepareStatement(QUERY_TEXT)) {
      st.setString(1, instanceId.toString());
      st.setString(2, instrumentId.group().value());
      st.setString(3, instrumentId.name().value());
      st.setInt(4, instrumentId.version().major());
      st.setInt(5, instrumentId.version().minor());
      st.setInt(6, instrumentId.version().patch());
      st.setString(7, qualifierOf(instrumentId));
      st.execute();
      return ARDBUnit.UNIT;
    } catch (final Exception e) {
      throw AREnsDBExceptions.wrap(e);
    }
  }

  private static String qualifierOf(
    final ARInstrumentID instrumentId)
  {
    return instrumentId.version()
      .qualifier()
      .map(VersionQualifier::text)
      .orElse("");
  }
}
