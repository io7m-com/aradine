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

package com.io7m.aradine.ensemble.internal.v1.commands;

import com.io7m.aradine.api.ARException;
import com.io7m.aradine.ensemble.internal.model.AREnsCommandUndoable;
import com.io7m.aradine.ensemble.internal.model.AREnsCommandUndoableType;
import com.io7m.aradine.ensemble.internal.model.AREnsModelCommandContextType;

/**
 * A no-op command.
 */

public enum AREnsModelCommandNoop
  implements AREnsModelCommand1Type<
  AREnsModelParametersUnused,
  AREnsModelCommandStateUnused>
{
  /**
   * A no-op command.
   */

  INSTANCE;

  @Override
  public AREnsCommandUndoableType<AREnsModelCommandStateUnused>
  execute(
    final AREnsModelCommandContextType context,
    final AREnsModelParametersUnused parameters)
    throws ARException
  {
    return new AREnsCommandUndoable<>(AREnsModelCommandStateUnused.UNUSED);
  }

  @Override
  public void undo(
    final AREnsModelCommandContextType context,
    final AREnsModelCommandStateUnused state)
    throws ARException
  {

  }

  @Override
  public void redo(
    final AREnsModelCommandContextType context,
    final AREnsModelCommandStateUnused state)
    throws ARException
  {

  }

  @Override
  public String description()
  {
    return "Nothing";
  }
}
