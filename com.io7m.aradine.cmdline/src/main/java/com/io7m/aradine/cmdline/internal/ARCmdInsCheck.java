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

import com.io7m.aradine.api.ARException;
import com.io7m.aradine.instrument.loader.ARInstrumentLoaders;
import com.io7m.quarrel.core.QCommandContextType;
import com.io7m.quarrel.core.QCommandMetadata;
import com.io7m.quarrel.core.QCommandStatus;
import com.io7m.quarrel.core.QParameterNamed1;
import com.io7m.quarrel.core.QParameterNamedType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static com.io7m.quarrel.core.QStringType.QConstant;

/**
 * The instrument check command.
 */

public final class ARCmdInsCheck extends ARCmdAbstract
{
  private static final QParameterNamed1<Path> INSTRUMENT_FILE =
    new QParameterNamed1<>(
      "--file",
      List.of(),
      new QConstant("The instrument jar file."),
      Optional.empty(),
      Path.class
    );

  /**
   * The instrument check command.
   */

  public ARCmdInsCheck()
  {
    super(new QCommandMetadata(
      "check",
      new QConstant("Check an instrument file."),
      Optional.empty()
    ));
  }

  @Override
  protected Logger logger()
  {
    return LoggerFactory.getLogger(ARCmdInsCheck.class);
  }

  @Override
  protected QCommandStatus onExecuteActual(
    final QCommandContextType context)
    throws ARException
  {
    final var file =
      context.parameterValue(INSTRUMENT_FILE);
    final var loaders =
      new ARInstrumentLoaders();

    try (var _ = loaders.createNoOpLoader(file)) {
      // Nothing required.
    }

    this.logger().info("Instrument passed all checks.");
    return QCommandStatus.SUCCESS;
  }

  @Override
  protected List<QParameterNamedType<?>> onListNamedParametersActual()
  {
    return List.of(INSTRUMENT_FILE);
  }
}
