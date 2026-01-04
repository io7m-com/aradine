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

package com.io7m.aradine.cmdline;

import com.io7m.aradine.api.ARVersion;
import com.io7m.aradine.cmdline.internal.ARCmdInfo;
import com.io7m.aradine.cmdline.internal.ARCmdInsCheck;
import com.io7m.aradine.cmdline.internal.ARCmdInsCodegen;
import com.io7m.aradine.cmdline.internal.ARCmdInvInstallInstrument;
import com.io7m.aradine.cmdline.internal.ARCmdInvInstallSampleMap;
import com.io7m.aradine.cmdline.internal.ARCmdInvListInstruments;
import com.io7m.aradine.cmdline.internal.ARCmdInvListSampleMaps;
import com.io7m.aradine.cmdline.internal.ARCmdInvUninstallInstrument;
import com.io7m.aradine.cmdline.internal.ARCmdInvUninstallSampleMap;
import com.io7m.aradine.cmdline.internal.ARDottedNameConverter;
import com.io7m.aradine.cmdline.internal.ARInstrumentIDConverter;
import com.io7m.aradine.cmdline.internal.ARSampleMapIDConverter;
import com.io7m.quarrel.core.QApplication;
import com.io7m.quarrel.core.QApplicationMetadata;
import com.io7m.quarrel.core.QApplicationType;
import com.io7m.quarrel.core.QCommandMetadata;
import com.io7m.quarrel.core.QValueConverterDirectory;
import com.io7m.seltzer.api.SStructuredErrorType;
import com.io7m.seltzer.slf4j.SSLogging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static com.io7m.quarrel.core.QStringType.QConstant;

/**
 * Main command line entry point.
 */

public final class ARCMain implements Runnable
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ARCMain.class);

  private final List<String> args;
  private final QApplicationType application;
  private int exitCode;

  /**
   * The main entry point.
   *
   * @param inArgs Command-line arguments
   */

  public ARCMain(
    final String[] inArgs)
  {
    this.args =
      Objects.requireNonNull(List.of(inArgs), "Command line arguments");

    final var metadata =
      new QApplicationMetadata(
        "aradine-cli",
        "com.io7m.aradine.cmdline",
        ARVersion.MAIN_VERSION,
        ARVersion.MAIN_BUILD,
        "Modular programmable synthesis (Command-line).",
        Optional.of(URI.create("https://www.io7m.com/software/aradine"))
      );

    final var converters =
      QValueConverterDirectory.core()
        .with(new ARInstrumentIDConverter())
        .with(new ARSampleMapIDConverter())
        .with(new ARDottedNameConverter());

    final var builder = QApplication.builder(metadata);

    {
      final var g = builder.createCommandGroup(
        new QCommandMetadata(
          "instrument",
          new QConstant("Instrument commands."),
          Optional.empty()
        )
      );
      g.addCommand(new ARCmdInsCheck());
      g.addCommand(new ARCmdInsCodegen());
    }

    {
      final var g = builder.createCommandGroup(
        new QCommandMetadata(
          "inventory",
          new QConstant("Inventory commands."),
          Optional.empty()
        )
      );
      g.addCommand(new ARCmdInvInstallInstrument());
      g.addCommand(new ARCmdInvInstallSampleMap());
      g.addCommand(new ARCmdInvListInstruments());
      g.addCommand(new ARCmdInvListSampleMaps());
      g.addCommand(new ARCmdInvUninstallInstrument());
      g.addCommand(new ARCmdInvUninstallSampleMap());
    }

    builder.addCommand(new ARCmdInfo());
    builder.setValueConverters(converters);
    builder.allowAtSyntax(true);

    this.application = builder.build();
    this.exitCode = 0;
  }

  /**
   * The main entry point.
   *
   * @param args Command line arguments
   */

  static void main(
    final String[] args)
  {
    System.exit(mainExitless(args));
  }

  /**
   * The main (exitless) entry point.
   *
   * @param args Command line arguments
   *
   * @return The exit code
   */

  public static int mainExitless(
    final String[] args)
  {
    final var cm = new ARCMain(args);
    cm.run();
    return cm.exitCode();
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

    for (final var x : e.getSuppressed()) {
      logException(log, x);
    }
  }

  /**
   * @return The program exit code
   */

  public int exitCode()
  {
    return this.exitCode;
  }

  @Override
  public void run()
  {
    try {
      final var parsed =
        this.application.parse(this.args);
      final var result =
        parsed.execute();

      this.exitCode = switch (result) {
        case SUCCESS -> 0;
        case FAILURE -> 1;
      };
    } catch (final Exception e) {
      logException(LOG, e);
      this.exitCode = 1;
    }
  }

  /**
   * @return The application instance
   */

  public QApplicationType application()
  {
    return this.application;
  }

  @Override
  public String toString()
  {
    return String.format(
      "[ARCMain 0x%s]",
      Long.toUnsignedString(System.identityHashCode(this), 16)
    );
  }
}
