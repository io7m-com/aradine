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

import com.io7m.aradine.api.ports.ARPortConnection;
import com.io7m.aradine.database.api.ARDBException;
import com.io7m.aradine.database.api.ARDBQueryProviderType;
import com.io7m.aradine.database.api.ARDBTransactionType;
import com.io7m.aradine.database.api.ARDBUnit;

/**
 * Connect a port.
 */

public enum AREnsQPortConnect
  implements AREnsQPortConnectType, ARDBQueryProviderType
{
  /**
   * Connect a port.
   */

  INSTANCE;

  private static final String QUERY_TEXT = """
    INSERT INTO port_connections (
      port_connection_source,
      port_connection_target
    ) VALUES (
      $1,
      $2
    )
    """;

  @Override
  public Class<?> queryInterface()
  {
    return AREnsQPortConnectType.class;
  }

  @Override
  public AREnsQPortConnectType queryInstance()
  {
    return this;
  }

  @Override
  public ARDBUnit execute(
    final ARDBTransactionType transaction,
    final ARPortConnection portConnection)
    throws ARDBException
  {
    final var connection =
      transaction.connection().connection();

    try (var st = connection.prepareStatement(QUERY_TEXT)) {
      st.setString(1, portConnection.portSource().toString());
      st.setString(2, portConnection.portTarget().toString());
      st.execute();
      return ARDBUnit.UNIT;
    } catch (final Exception e) {
      throw AREnsDBExceptions.wrap(e);
    }
  }
}
