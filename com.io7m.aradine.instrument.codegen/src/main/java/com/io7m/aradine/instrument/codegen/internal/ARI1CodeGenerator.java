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


package com.io7m.aradine.instrument.codegen.internal;

import com.io7m.anethum.common.SerializeException;
import com.io7m.aradine.instrument.codegen.ARI1CodeGenerationException;
import com.io7m.aradine.instrument.codegen.ARI1CodeGeneratorParameters;
import com.io7m.aradine.instrument.codegen.ARI1CodeGeneratorResult;
import com.io7m.aradine.instrument.codegen.ARI1CodeGeneratorType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentDescriptionType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentServicesType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionIntegerType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionRealType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionSampleMapType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterId;
import com.io7m.aradine.instrument.spi1.ARI1ParameterIntegerType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterRealType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterSampleMapType;
import com.io7m.aradine.instrument.spi1.ARI1PortDescriptionOutputSampledType;
import com.io7m.aradine.instrument.spi1.ARI1PortDescriptionType;
import com.io7m.aradine.instrument.spi1.ARI1PortId;
import com.io7m.aradine.instrument.spi1.ARI1PortOutputSampledType;
import com.io7m.jodist.ClassName;
import com.io7m.jodist.FieldSpec;
import com.io7m.jodist.JavaFile;
import com.io7m.jodist.MethodSpec;
import com.io7m.jodist.TypeSpec;
import org.apache.commons.text.CaseUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * The code generator implementation.
 */

public final class ARI1CodeGenerator implements ARI1CodeGeneratorType
{
  private final ARI1CodeGeneratorParameters parameters;

  /**
   * The code generator implementation.
   *
   * @param inParameters The parameters
   */

  public ARI1CodeGenerator(
    final ARI1CodeGeneratorParameters inParameters)
  {
    this.parameters =
      Objects.requireNonNull(inParameters, "parameters");
  }

  private static MethodSpec generatePortConstructor(
    final List<ARI1PortId> ids,
    final Map<ARI1PortId, ARI1PortDescriptionType> instrumentPorts)
  {
    final var builder = MethodSpec.constructorBuilder();
    builder.addModifiers(PUBLIC);
    builder.addParameter(
      ARI1InstrumentServicesType.class,
      "$services",
      FINAL
    );

    for (final var id : ids) {
      final var port =
        instrumentPorts.get(id);
      final var name =
        generatePortFieldName(port);

      builder.addCode(
        "this.$L = $L.declaredPort(new $T($L), $T.class);\n",
        name,
        "$services",
        ARI1PortId.class,
        Integer.valueOf(port.id().value()),
        generatePortFieldType(port)
      );

      builder.addCode(
        "$T.requireNonNull(this.$L, $S);\n",
        Objects.class,
        name,
        "Port %d (%s) must be non-null".formatted(
          Integer.valueOf(id.value()),
          port.label()
        )
      );
    }

    return builder.build();
  }

  private static Class<?> generatePortFieldType(
    final ARI1PortDescriptionType description)
  {
    if (description instanceof ARI1PortDescriptionOutputSampledType p) {
      return ARI1PortOutputSampledType.class;
    }

    throw new IllegalStateException();
  }

  private static FieldSpec generatePortField(
    final ARI1PortDescriptionType description)
  {
    return FieldSpec.builder(
      ClassName.get(generatePortFieldType(description)),
      generatePortFieldName(description),
      FINAL
    ).build();
  }

  private static String generatePortFieldName(
    final ARI1PortDescriptionType description)
  {
    return CaseUtils.toCamelCase(
      description.label(),
      false,
      ARI1Labels.labelDelimiters()
    ) + description.id().value();
  }

  private static MethodSpec generateParameterConstructor(
    final List<ARI1ParameterId> ids,
    final Map<ARI1ParameterId, ARI1ParameterDescriptionType> instrumentParameters)
  {
    final var builder = MethodSpec.constructorBuilder();
    builder.addModifiers(PUBLIC);
    builder.addParameter(
      ARI1InstrumentServicesType.class,
      "$services",
      FINAL
    );

    for (final var id : ids) {
      final var param =
        instrumentParameters.get(id);
      final var name =
        generateParameterFieldName(param);

      builder.addCode(
        "this.$L = $L.declaredParameter(new $T($L), $T.class);\n",
        name,
        "$services",
        ARI1ParameterId.class,
        Integer.valueOf(param.id().value()),
        generateParameterFieldType(param)
      );

      builder.addCode(
        "$T.requireNonNull(this.$L, $S);\n",
        Objects.class,
        name,
        "Parameter %d (%s) must be non-null".formatted(
          Integer.valueOf(id.value()),
          param.label()
        )
      );
    }

    return builder.build();
  }

  private static Class<?> generateParameterFieldType(
    final ARI1ParameterDescriptionType description)
  {
    if (description instanceof ARI1ParameterDescriptionSampleMapType p) {
      return ARI1ParameterSampleMapType.class;
    }

    if (description instanceof ARI1ParameterDescriptionRealType p) {
      return ARI1ParameterRealType.class;
    }

    if (description instanceof ARI1ParameterDescriptionIntegerType p) {
      return ARI1ParameterIntegerType.class;
    }

    throw new IllegalStateException();
  }

  private static FieldSpec generateParameterField(
    final ARI1ParameterDescriptionType description)
  {
    return FieldSpec.builder(
      ClassName.get(generateParameterFieldType(description)),
      generateParameterFieldName(description),
      FINAL
    ).build();
  }

  private static String generateParameterFieldName(
    final ARI1ParameterDescriptionType description)
  {
    return CaseUtils.toCamelCase(
      description.label(),
      false,
      ARI1Labels.labelDelimiters()
    ) + description.id().value();
  }

  @Override
  public ARI1CodeGeneratorResult execute()
    throws ARI1CodeGenerationException
  {
    try {
      final var instrument =
        this.parameters.parsers()
          .parseFile(this.parameters.sourceFile());

      final var javaClasses = new HashSet<Path>();
      javaClasses.add(this.generateParametersClass(instrument));
      javaClasses.add(this.generatePortsClass(instrument));

      final var instFile = this.generateInstrumentFile(instrument);
      return new ARI1CodeGeneratorResult(Set.copyOf(javaClasses), instFile);
    } catch (final Exception e) {
      throw new ARI1CodeGenerationException(e);
    }
  }

  private Path generateInstrumentFile(
    final ARI1InstrumentDescriptionType instrument)
    throws SerializeException, IOException
  {
    final var fixed =
      new ARI1InstrumentDescription(
        this.parameters.symbolicName(),
        this.parameters.version(),
        instrument.metadata(),
        instrument.parameters(),
        instrument.ports()
      );

    var output =
      this.parameters.outputResourceDirectory();
    final var packageElements =
      Arrays.stream(this.parameters.packageName().split("\\."))
        .toList();

    for (final var element : packageElements) {
      output = output.resolve(element);
    }

    Files.createDirectories(output);
    output = output.resolve("instrument.xml");

    this.parameters.serializers()
      .serializeFile(output, fixed);

    return output;
  }

  private Path generatePortsClass(
    final ARI1InstrumentDescriptionType instrument)
    throws IOException
  {
    final var className =
      ClassName.get(this.parameters.packageName(), "Ports");
    final var classBuilder =
      TypeSpec.classBuilder(className);

    classBuilder.addModifiers(PUBLIC);
    classBuilder.addModifiers(FINAL);

    final var instrumentPorts =
      instrument.ports();

    final var ids =
      instrumentPorts
        .keySet()
        .stream()
        .sorted()
        .toList();

    for (final var id : ids) {
      classBuilder.addField(
        generatePortField(instrumentPorts.get(id))
      );
    }

    classBuilder.addMethod(
      generatePortConstructor(ids, instrumentPorts)
    );

    final var file =
      JavaFile.builder(this.parameters.packageName(), classBuilder.build())
        .build();

    return file.writeToPath(
      this.parameters.outputSourceDirectory(),
      StandardCharsets.UTF_8
    );
  }

  private Path generateParametersClass(
    final ARI1InstrumentDescriptionType instrument)
    throws IOException
  {
    final var className =
      ClassName.get(this.parameters.packageName(), "Parameters");
    final var classBuilder =
      TypeSpec.classBuilder(className);

    classBuilder.addModifiers(PUBLIC);
    classBuilder.addModifiers(FINAL);

    final var instrumentParameters =
      instrument.parameters();

    final var ids =
      instrumentParameters
        .keySet()
        .stream()
        .sorted()
        .toList();

    for (final var id : ids) {
      classBuilder.addField(
        generateParameterField(instrumentParameters.get(id))
      );
    }

    classBuilder.addMethod(
      generateParameterConstructor(ids, instrumentParameters)
    );

    final var file =
      JavaFile.builder(this.parameters.packageName(), classBuilder.build())
        .build();

    return file.writeToPath(
      this.parameters.outputSourceDirectory(),
      StandardCharsets.UTF_8
    );
  }
}
