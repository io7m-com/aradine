/*
 * Copyright © 2024 Mark Raynsford <code@io7m.com> https://www.io7m.com
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


package com.io7m.aradine.ensemble.internal.v1.commands;

import com.io7m.aradine.ensemble.internal.model.AREnsModelCommandDirectoryType;
import com.io7m.aradine.ensemble.internal.model.AREnsModelCommandParametersType;
import com.io7m.aradine.ensemble.internal.model.AREnsModelCommandRecord;
import com.io7m.aradine.ensemble.internal.model.AREnsModelCommandStateType;
import com.io7m.aradine.ensemble.internal.model.AREnsModelCommandType;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The v1 command directory.
 */

public enum AREnsModelCommands1
  implements AREnsModelCommandDirectoryType
{
  /**
   * The v1 command directory.
   */

  INSTANCE;

  private static final Map<String, AREnsModelCommandType<?, ?>> COMMANDS =
    Map.copyOf(
      Stream.of(
          AREnsModelCommandNoop.INSTANCE,
          AREnsModelCommandInstrumentLoad.INSTANCE
        )
        .map(AREnsModelCommands1::commandEntry)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
    );

  private static Map.Entry<String, AREnsModelCommandType<?, ?>> commandEntry(
    final AREnsModelCommandType<?, ?> c)
  {
    return Map.entry(c.getClass().getSimpleName(), c);
  }

  @Override
  public <P extends AREnsModelCommandParametersType,
    S extends AREnsModelCommandStateType> Optional<AREnsModelCommandType<P, S>>
  lookupByCommandRecord(
    final AREnsModelCommandRecord record)
  {
    final var c = COMMANDS.get(record.commandClass());
    final var o = (Object) c;
    final var r = (AREnsModelCommandType<P, S>) o;
    return Optional.ofNullable(r);
  }
}
