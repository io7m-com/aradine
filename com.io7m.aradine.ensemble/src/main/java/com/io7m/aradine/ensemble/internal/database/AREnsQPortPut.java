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
import com.io7m.aradine.api.ports.ARPortDescription;
import com.io7m.aradine.database.api.ARDBQueryProviderType;
import com.io7m.aradine.database.api.ARDBTransactionType;
import com.io7m.aradine.database.api.ARDBUnit;

/**
 * Put/update a port.
 */

public enum AREnsQPortPut
  implements AREnsQPortPutType, ARDBQueryProviderType
{
  /**
   * Put/update a port.
   */

  INSTANCE;

  private static final String QUERY_TEXT = """
    INSERT INTO ports (
      port_id,
      port_instrument_instance,
      port_kind,
      port_direction,
      port_number,
      port_label,
      port_semantics
    ) VALUES (
      $1,
      $2,
      $3,
      $4,
      $5,
      $6,
      $7
    )
    """;

  @Override
  public Class<?> queryInterface()
  {
    return AREnsQPortPutType.class;
  }

  @Override
  public AREnsQPortPutType queryInstance()
  {
    return this;
  }

  @Override
  public ARDBUnit execute(
    final ARDBTransactionType transaction,
    final ARPortDescription port)
    throws ARException
  {
    final var connection =
      transaction.connection().connection();

    try (var st = connection.prepareStatement(QUERY_TEXT)) {
      final var portId =
        port.id().toString();
      final var portInstrumentInstance =
        port.instrumentInstance().toString();
      final var portKind =
        port.kind().name();
      final var portDirection =
        port.direction().name();
      final var portNumber =
        port.number().value();
      final var portLabel =
        port.label();
      final var portSemantics =
        String.join(",", port.semantics());

      st.setString(1, portId);
      st.setString(2, portInstrumentInstance);
      st.setString(3, portKind);
      st.setString(4, portDirection);
      st.setLong(5, portNumber);
      st.setString(6, portLabel);
      st.setString(7, portSemantics);
      st.execute();
      return ARDBUnit.UNIT;
    } catch (final Exception e) {
      throw AREnsDBExceptions.wrap(e);
    }
  }
}
