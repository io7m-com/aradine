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

import com.io7m.aradine.api.ARCloseables;
import com.io7m.aradine.api.directories.ARApplicationDirectories;
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
 * The inventory install command.
 */

public final class ARCmdInvInstall extends ARCmdAbstract
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
   * The inventory install command.
   */

  public ARCmdInvInstall()
  {
    super(new QCommandMetadata(
      "install",
      new QConstant("Install instruments into the local inventory."),
      Optional.empty()
    ));
  }

  @Override
  protected Logger logger()
  {
    return LoggerFactory.getLogger(ARCmdInvInstall.class);
  }

  @Override
  protected QCommandStatus onExecuteActual(
    final QCommandContextType context)
    throws Exception
  {
    final var logger = this.logger();
    final var directories = ARApplicationDirectories.directories();
    try (var resources = ARCloseables.create()) {
      final var inventory =
        ARCInventories.openInventory(directories, resources);

      inventory.instrumentInstall(
        context.parameterValue(INSTRUMENT_FILE),
        progress -> {
          logger.info(
            "{} ({}%): {} ({}%)",
            progress.task(),
            Integer.valueOf((int) (progress.taskProgress() * 100.0)),
            progress.subTask(),
            Integer.valueOf((int) (progress.subTaskProgress() * 100.0))
          );
        }
      ).get();

      return QCommandStatus.SUCCESS;
    }
  }

  @Override
  protected List<QParameterNamedType<?>> onListNamedParametersActual()
  {
    return List.of(INSTRUMENT_FILE);
  }
}
