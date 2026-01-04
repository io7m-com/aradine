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

package com.io7m.aradine.cmdline.internal;

import com.io7m.quarrel.core.QCommandContextType;
import com.io7m.quarrel.core.QCommandMetadata;
import com.io7m.quarrel.core.QCommandStatus;
import com.io7m.quarrel.core.QCommandType;
import com.io7m.quarrel.core.QParameterNamedType;
import com.io7m.quarrel.ext.logback.QLogback;
import com.io7m.seltzer.api.SStructuredErrorType;
import com.io7m.seltzer.slf4j.SSLogging;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * Abstract base class for command implementations.
 */

public abstract class ARCmdAbstract
  implements QCommandType
{
  protected abstract Logger logger();

  private final QCommandMetadata metadata;

  protected ARCmdAbstract(
    final QCommandMetadata inMetadata)
  {
    this.metadata =
      Objects.requireNonNull(inMetadata, "Metadata");
  }

  @Override
  public final QCommandMetadata metadata()
  {
    return this.metadata;
  }

  @Override
  public final QCommandStatus onExecute(
    final QCommandContextType context)
  {
    QLogback.configure(context);

    System.setProperty("org.jooq.no-logo", "true");
    System.setProperty("org.jooq.no-tips", "true");

    try {
      return this.onExecuteActual(context);
    } catch (final Exception e) {
      logException(this.logger(), e);
      return QCommandStatus.FAILURE;
    }
  }

  private static void logException(
    final Logger log,
    final Throwable e)
  {
    switch (e) {
      case final ExecutionException x -> {
        logException(log, x.getCause());
      }
      case final SStructuredErrorType<?> x -> {
        SSLogging.logMDCWithStyle(
          log,
          Level.ERROR,
          SSLogging.MessageStyle.STYLE_MESSAGE_ONLY,
          x
        );
      }
      case final Throwable x -> {
        log.error("", e);
      }
    }
  }

  protected abstract QCommandStatus onExecuteActual(
    QCommandContextType context)
    throws Exception;

  @Override
  public final List<QParameterNamedType<?>> onListNamedParameters()
  {
    return QLogback.plusParameters(this.onListNamedParametersActual());
  }

  protected abstract List<QParameterNamedType<?>> onListNamedParametersActual();
}
