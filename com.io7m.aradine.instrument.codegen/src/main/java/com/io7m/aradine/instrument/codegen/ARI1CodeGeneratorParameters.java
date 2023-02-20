/*
 * Copyright © 2022 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

package com.io7m.aradine.instrument.codegen;

import com.io7m.aradine.instrument.spi1.ARI1Version;
import com.io7m.aradine.instrument.spi1.xml.ARI1InstrumentParserFactoryType;
import com.io7m.aradine.instrument.spi1.xml.ARI1InstrumentSerializerFactoryType;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Parameters for code generation.
 *
 * @param symbolicName            The symbolic name of the bundle holding the
 *                                instrument
 * @param version                 The version of the bundle holding the
 *                                instrument
 * @param packageName             The output package name
 * @param sourceFile              The input source file
 * @param outputSourceDirectory   The output source directory
 * @param outputResourceDirectory The output resource directory
 * @param parsers                 The instrument parsers
 * @param serializers             The instrument serializers
 */

public record ARI1CodeGeneratorParameters(
  String symbolicName,
  ARI1Version version,
  String packageName,
  Path sourceFile,
  Path outputSourceDirectory,
  Path outputResourceDirectory,
  ARI1InstrumentParserFactoryType parsers,
  ARI1InstrumentSerializerFactoryType serializers)
{
  /**
   * Parameters for code generation.
   *
   * @param symbolicName            The symbolic name of the bundle holding the
   *                                instrument
   * @param version                 The version of the bundle holding the
   *                                instrument
   * @param packageName             The output package name
   * @param sourceFile              The input source file
   * @param outputSourceDirectory   The output source directory
   * @param outputResourceDirectory The output resource directory
   * @param parsers                 The instrument parsers
   * @param serializers             The instrument serializers
   */

  public ARI1CodeGeneratorParameters
  {
    Objects.requireNonNull(symbolicName, "symbolicName");
    Objects.requireNonNull(version, "version");
    Objects.requireNonNull(packageName, "packageName");
    Objects.requireNonNull(sourceFile, "sourceFile");
    Objects.requireNonNull(outputSourceDirectory, "outputSourceDirectory");
    Objects.requireNonNull(outputResourceDirectory, "outputResourceDirectory");
    Objects.requireNonNull(parsers, "parsers");
    Objects.requireNonNull(serializers, "serializers");
  }
}
