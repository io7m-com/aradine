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

package com.io7m.aradine.ensemble.internal.model;

import com.io7m.aradine.api.ARException;

/**
 * A model command.
 *
 * @param <P> The type of command parameters
 * @param <S> The type of command state
 */

public interface AREnsModelCommandType<
  P extends AREnsModelCommandParametersType,
  S extends AREnsModelCommandStateType>
{
  /**
   * Execute the command.
   *
   * @param context    The command context
   * @param parameters The command parameters
   *
   * @return The undoable value with any associated command state
   *
   * @throws ARException On errors
   */

  AREnsCommandUndoableType<S> execute(
    AREnsModelCommandContextType context,
    P parameters)
    throws ARException;

  /**
   * Undo the command from the given state.
   *
   * @param context The command context
   * @param state   The command state
   *
   * @throws ARException On errors
   */

  void undo(
    AREnsModelCommandContextType context,
    S state)
    throws ARException;

  /**
   * Redo the command from the given state.
   *
   * @param context The command context
   * @param state   The command state
   *
   * @throws ARException On errors
   */

  void redo(
    AREnsModelCommandContextType context,
    S state)
    throws ARException;

  /**
   * @return A humanly-readable description of the operation
   */

  String description();
}
