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
import com.io7m.aradine.ensemble.internal.events.AREnsEventType;
import com.io7m.jmulticlose.core.CloseableType;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

/**
 * The model type.
 */

public interface AREnsModelType
  extends CloseableType
{
  /**
   * @return A stream of ensemble events
   */

  Flow.Publisher<AREnsEventType> events();

  /**
   * @return The loading operation in progress
   */

  CompletableFuture<?> loading();

  /**
   * @return Undo the operation at the tip of the undo stack
   */

  CompletableFuture<?> undo();

  /**
   * @return Redo the operation at the tip of the redo stack
   */

  CompletableFuture<?> redo();

  /**
   * Execute a model command.
   *
   * @param command    The command
   * @param parameters The parameters
   * @param <P>        The type of parameters
   * @param <S>        The type of state
   *
   * @return The operation in progress
   */

  <P extends AREnsModelCommandParametersType, S extends AREnsModelCommandStateType>
  CompletableFuture<?> executeCommand(
    AREnsModelCommandType<P, S> command,
    P parameters
  );

  @Override
  void close()
    throws ARException;
}
