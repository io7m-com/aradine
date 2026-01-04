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

import com.io7m.aradine.instrument.codegen.ARI1CodeGeneratorParameters;
import com.io7m.aradine.instrument.codegen.ARI1CodeGenerators;
import com.io7m.aradine.instrument.spi1.json_data.ARI1InstrumentParsers;
import com.io7m.aradine.instrument.spi1.json_data.ARI1InstrumentSerializers;
import com.io7m.lanark.core.RDottedName;
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
 * The instrument codegen command.
 */

public final class ARCmdInsCodegen extends ARCmdAbstract
{
  private static final QParameterNamed1<Path> INSTRUMENT_FILE =
    new QParameterNamed1<>(
      "--file",
      List.of(),
      new QConstant("The instrument description file."),
      Optional.empty(),
      Path.class
    );

  private static final QParameterNamed1<Path> OUTPUT_SOURCE_DIRECTORY =
    new QParameterNamed1<>(
      "--output-source-directory",
      List.of(),
      new QConstant("The output Java source directory."),
      Optional.empty(),
      Path.class
    );

  private static final QParameterNamed1<Path> OUTPUT_RESOURCE_DIRECTORY =
    new QParameterNamed1<>(
      "--output-resource-directory",
      List.of(),
      new QConstant("The output resource directory."),
      Optional.empty(),
      Path.class
    );

  private static final QParameterNamed1<RDottedName> PACKAGE_NAME =
    new QParameterNamed1<>(
      "--package-name",
      List.of(),
      new QConstant("The Java package name."),
      Optional.empty(),
      RDottedName.class
    );

  /**
   * The instrument codegen command.
   */

  public ARCmdInsCodegen()
  {
    super(new QCommandMetadata(
      "codegen",
      new QConstant("Generate code for an instrument file."),
      Optional.empty()
    ));
  }

  @Override
  protected Logger logger()
  {
    return LoggerFactory.getLogger(ARCmdInsCodegen.class);
  }

  @Override
  protected QCommandStatus onExecuteActual(
    final QCommandContextType context)
    throws Exception
  {
    final var sourceFile =
      context.parameterValue(INSTRUMENT_FILE);
    final var packageName =
      context.parameterValue(PACKAGE_NAME);
    final var outputSource =
      context.parameterValue(OUTPUT_SOURCE_DIRECTORY);
    final var outputResource =
      context.parameterValue(OUTPUT_RESOURCE_DIRECTORY);

    final var parsers =
      new ARI1InstrumentParsers();
    final var serializers =
      new ARI1InstrumentSerializers();
    final var codeGenerators =
      new ARI1CodeGenerators();

    final var instrumentDescription =
      parsers.parseFile(sourceFile);

    final var generator = codeGenerators.createCodeGenerator(
      new ARI1CodeGeneratorParameters(
        instrumentDescription.group(),
        instrumentDescription.identifier(),
        instrumentDescription.version(),
        packageName.value(),
        sourceFile,
        outputSource,
        outputResource,
        parsers,
        serializers
      )
    );

    final var r = generator.execute();
    final var logger = this.logger();
    for (final var clazz : r.javaClasses()) {
      logger.info("Generated Java source: {}", clazz);
    }
    logger.info("Generated resource: {}", r.resourceFile());
    return QCommandStatus.SUCCESS;
  }

  @Override
  protected List<QParameterNamedType<?>> onListNamedParametersActual()
  {
    return List.of(
      INSTRUMENT_FILE,
      OUTPUT_SOURCE_DIRECTORY,
      OUTPUT_RESOURCE_DIRECTORY,
      PACKAGE_NAME
    );
  }
}
