/*
 * Copyright © 2026 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

package com.io7m.aradine.ensemble.internal.ops_v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.io7m.aradine.api.ARException;
import com.io7m.aradine.ensemble.internal.model.AREnsModelOpContextType;
import com.io7m.lanark.core.RDottedName;
import com.io7m.verona.core.Version;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.util.Objects;
import java.util.UUID;

/**
 * Instrument load op.
 *
 * @param instrumentInstanceID The instrument instance ID
 * @param instrumentGroup      The instrument group
 * @param instrumentName       The instrument name
 * @param instrumentVersion    The instrument version
 */

@JsonSerialize
@JsonDeserialize
public record AREnsOP1InstrumentLoad(
  @JsonProperty(value = "InstrumentInstanceID", required = true)
  UUID instrumentInstanceID,
  @JsonProperty(value = "InstrumentGroup", required = true)
  RDottedName instrumentGroup,
  @JsonProperty(value = "InstrumentName", required = true)
  RDottedName instrumentName,
  @JsonProperty(value = "InstrumentVersion", required = true)
  Version instrumentVersion)
  implements AREnsOp1Type
{
  /**
   * Instrument load op.
   *
   * @param instrumentInstanceID The instrument instance ID
   * @param instrumentGroup      The instrument group
   * @param instrumentName       The instrument name
   * @param instrumentVersion    The instrument version
   */

  public AREnsOP1InstrumentLoad
  {
    Objects.requireNonNull(instrumentInstanceID, "instrumentInstanceID");
    Objects.requireNonNull(instrumentGroup, "instrumentGroup");
    Objects.requireNonNull(instrumentName, "instrumentName");
    Objects.requireNonNull(instrumentVersion, "instrumentVersion");
  }

  @Override
  public void onExecute(
    final AREnsModelOpContextType context)
    throws ARException
  {

  }

  @Override
  public void onUndo(
    final AREnsModelOpContextType context)
    throws ARException
  {

  }
}
