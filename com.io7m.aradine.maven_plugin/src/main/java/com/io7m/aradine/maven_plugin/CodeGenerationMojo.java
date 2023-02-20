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

package com.io7m.aradine.maven_plugin;

import com.io7m.aradine.instrument.codegen.ARI1CodeGeneratorParameters;
import com.io7m.aradine.instrument.codegen.ARI1CodeGenerators;
import com.io7m.aradine.instrument.spi1.ARI1Version;
import com.io7m.aradine.instrument.spi1.ARI1VersionQualifier;
import com.io7m.aradine.instrument.spi1.xml.ARI1InstrumentParsers;
import com.io7m.aradine.instrument.spi1.xml.ARI1InstrumentSerializers;
import com.io7m.verona.core.VersionParser;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

import java.nio.file.Paths;

/**
 * The "generate sources" mojo.
 */

@Mojo(
  name = "generateSources1",
  defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public final class CodeGenerationMojo extends AbstractMojo
{
  /**
   * The package name used for generated code.
   */

  @Parameter(
    name = "packageName",
    required = true)
  private String packageName;

  /**
   * The current Maven settings.
   */

  @Parameter(
    defaultValue = "${settings}",
    readonly = true,
    required = true)
  private Settings settings;

  /**
   * The source file.
   */

  @Parameter(
    name = "sourceFile",
    defaultValue = "${project.basedir}/src/main/instrument/instrument.xml",
    required = false)
  private String sourceFile;

  /**
   * The output source directory.
   */

  @Parameter(
    name = "outputSourceDirectory",
    defaultValue = "${project.build.directory}/generated-sources/aradine",
    required = false)
  private String outputSourceDirectory;

  /**
   * The output resource directory.
   */

  @Parameter(
    name = "outputResourceDirectory",
    defaultValue = "${project.build.directory}/classes",
    required = false)
  private String outputResourceDirectory;

  /**
   * The Maven project.
   */

  @Parameter(readonly = true, defaultValue = "${project}")
  private MavenProject project;

  /**
   * Instantiate the mojo.
   */

  public CodeGenerationMojo()
  {

  }

  @Override
  public void execute()
    throws MojoExecutionException
  {
    try {
      final var generators = new ARI1CodeGenerators();

      final var version =
        VersionParser.parse(this.project.getVersion());

      final var parameters =
        new ARI1CodeGeneratorParameters(
          this.project.getArtifactId(),
          new ARI1Version(
            version.major(),
            version.minor(),
            version.patch(),
            version.qualifier()
              .map(q -> new ARI1VersionQualifier(q.text()))
          ),
          this.packageName,
          Paths.get(this.sourceFile),
          Paths.get(this.outputSourceDirectory),
          Paths.get(this.outputResourceDirectory),
          new ARI1InstrumentParsers(),
          new ARI1InstrumentSerializers()
        );

      final var generator =
        generators.createCodeGenerator(parameters);
      final var result =
        generator.execute();

      final var resource = new Resource();
      resource.setDirectory(this.outputResourceDirectory);
      resource.setFiltering(false);

      this.project.addCompileSourceRoot(this.outputSourceDirectory);
      this.project.addResource(resource);

    } catch (final Exception e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }
}
