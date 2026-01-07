/*
 * Copyright © 2025 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

package com.io7m.aradine.instrument.loader.internal;

import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.instrument.ARInstrumentDescription;
import com.io7m.aradine.api.instrument.ARInstrumentInstanceID;
import com.io7m.aradine.api.instrument.ARInstrumentExecutableType;
import com.io7m.aradine.instrument.loader.api.ARInstrumentContextConstructorType;
import com.io7m.aradine.instrument.loader.api.ARInstrumentLoaderType;
import com.io7m.aradine.instrument.loader.api.ARInstrumentPortAssignerType;
import com.io7m.aradine.instrument.loader.api.ARInstrumentReadResultType;
import com.io7m.aradine.instrument.loader.api.ARInstrumentReadV1;
import com.io7m.aradine.instrument.loader.api.ARInstrumentReaderFactoryType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentContextType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentDescription;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentFactoryType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarFile;

/**
 * The instrument loader.
 */

public final class ARInstrumentLoader
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ARInstrumentLoader.class);

  private static final String INSTRUMENT_SPI1_MODULE_NAME =
    ARI1InstrumentFactoryType.class
      .getModule()
      .getName();

  private static final String INSTRUMENT_SPI1_SERVICE_TYPE =
    ARI1InstrumentFactoryType.class
      .getCanonicalName();

  private static final String JAVA_BASE =
    "java.base";
  private static final String ORG_OSGI_ANNOTATION_BUNDLE =
    "org.osgi.annotation.bundle";
  private static final String ORG_OSGI_ANNOTATION_VERSIONING =
    "org.osgi.annotation.versioning";

  private static final Set<String> ALLOWED_MODULES =
    Set.of(
      JAVA_BASE,
      INSTRUMENT_SPI1_MODULE_NAME,
      ORG_OSGI_ANNOTATION_BUNDLE,
      ORG_OSGI_ANNOTATION_VERSIONING
    );

  private static final ModuleLayer BASE_LAYER =
    ARInstrumentLoader.class.getModule()
      .getLayer();

  static {
    if (BASE_LAYER == null) {
      throw new IllegalStateException(
        "Instrument loader must be on the module path."
      );
    }
  }

  private ARInstrumentLoader()
  {

  }

  /**
   * Create a new instrument no-op loader.
   *
   * @param readers The reader factory
   * @param file    The instrument file
   *
   * @return A loader
   *
   * @throws ARException On errors
   */

  public static ARInstrumentLoaderType createNoOp(
    final ARInstrumentReaderFactoryType readers,
    final Path file)
    throws ARException
  {
    return createInternal(
      readers,
      file,
      (_, _, _) -> new UnsupportedLoader()
    );
  }

  private static ARInstrumentLoaderType createInternal(
    final ARInstrumentReaderFactoryType readers,
    final Path file,
    final LoadCompletionType completion)
    throws ARException
  {
    try {
      LOG.trace("Reading instrument file {}", file);
      final ARInstrumentReadResultType instrumentDescription;
      try (var instrumentReader = readers.create(file)) {
        instrumentDescription = instrumentReader.executeAndParse();
      }

      LOG.trace("Reading module descriptor from file.");
      final ModuleDescriptor moduleDescriptor =
        readModuleDescriptorFromJar(file);

      LOG.trace("Checking module descriptor.");
      checkModule(file, moduleDescriptor);

      final var instrumentModuleReference =
        new ARInstrumentModuleReference(moduleDescriptor, file);
      final var instrumentModuleFinder =
        new ARInstrumentModuleFinder(instrumentModuleReference);
      final var moduleName =
        moduleDescriptor.name();

      LOG.trace("Creating module classloader.");
      final var instrumentClassLoader =
        new URLClassLoader(
          new URL[]{file.toUri().toURL()},
          null
        );

      LOG.trace("Resolving module layer.");
      final var instrumentConfiguration =
        BASE_LAYER.configuration()
          .resolve(
            ModuleFinder.of(),
            instrumentModuleFinder,
            Set.of(moduleName)
          );

      LOG.trace("Creating instrument module layer.");
      final var instrumentLayer =
        BASE_LAYER.defineModulesWithOneLoader(
          instrumentConfiguration,
          instrumentClassLoader
        );

      LOG.trace("Completing instrument creation.");
      return completion.complete(
        instrumentLayer,
        instrumentDescription,
        instrumentClassLoader
      );
    } catch (final Exception e) {
      LOG.trace("", e);
      throw wrap(e);
    }
  }

  /**
   * Create a new instrument loader.
   *
   * @param readers            The reader factory
   * @param serviceConstructor The service constructor
   * @param file               The instrument file
   *
   * @return A loader
   *
   * @throws ARException On errors
   */

  public static ARInstrumentLoaderType create(
    final ARInstrumentReaderFactoryType readers,
    final ARInstrumentContextConstructorType serviceConstructor,
    final Path file)
    throws ARException
  {
    Objects.requireNonNull(serviceConstructor, "Service Constructor");
    Objects.requireNonNull(readers, "Readers");
    Objects.requireNonNull(file, "File");

    return createInternal(
      readers,
      file,
      (instrumentLayer, instrumentDescription, classLoader) -> {
        return createV1(
          serviceConstructor,
          instrumentLayer,
          instrumentDescription,
          classLoader
        );
      });
  }

  private static ModuleDescriptor readModuleDescriptorFromJar(
    final Path file)
    throws ARException, IOException
  {
    try (var jarFile = new JarFile(file.toFile())) {
      final var moduleEntry = jarFile.getEntry("module-info.class");
      if (moduleEntry == null) {
        throw errorModuleMissingDescriptor(file);
      }
      try (var stream = jarFile.getInputStream(moduleEntry)) {
        return ModuleDescriptor.read(stream);
      }
    }
  }

  private static ARException errorModuleMissingDescriptor(
    final Path file)
  {
    return new ARException(
      "Module jar file does not contain a module descriptor.",
      "error-module-no-descriptor",
      Map.ofEntries(
        Map.entry("File", file.toAbsolutePath().toString())
      ),
      Optional.empty()
    );
  }

  private static ARInstrumentLoader1 createV1(
    final ARInstrumentContextConstructorType serviceConstructor,
    final ModuleLayer instrumentLayer,
    final ARInstrumentReadResultType instrumentDescription,
    final URLClassLoader instrumentClassLoader)
  {
    final var v1 = switch (instrumentDescription) {
      case final ARInstrumentReadV1 rv1 -> {
        yield rv1.description();
      }
    };

    final var loader =
      ServiceLoader.load(instrumentLayer, ARI1InstrumentFactoryType.class);
    final var serviceIterator =
      loader.iterator();

    while (serviceIterator.hasNext()) {
      final var instrumentFactory =
        serviceIterator.next();

      return new ARInstrumentLoader1(
        serviceConstructor,
        v1,
        instrumentClassLoader,
        instrumentLayer,
        instrumentFactory
      );
    }

    throw new IllegalStateException();
  }

  ///
  /// Check all invariants for the module `m`.
  ///
  /// 1. `m` only requires modules in the allowed set.
  /// 2. `m` does not `use` any services.
  /// 3. `m` provides a single instrument factory service.
  ///

  private static void checkModule(
    final Path file,
    final ModuleDescriptor moduleDescriptor)
    throws ARException
  {
    final var moduleName = moduleDescriptor.name();
    LOG.trace("Module name: {}", moduleName);

    for (final var requires : moduleDescriptor.requires()) {
      if (!ALLOWED_MODULES.contains(requires.name())) {
        throw errorModuleDisallowed(file, moduleName, requires.name());
      }
    }

    final var usesAll = moduleDescriptor.uses();
    if (!usesAll.isEmpty()) {
      throw errorModuleUses(file, moduleName);
    }

    final var providesAll = moduleDescriptor.provides();
    if (providesAll.size() != 1) {
      throw errorModuleProvideExactlyOnce(file, moduleName);
    }

    final var provides = providesAll.iterator().next();
    final var providesService = provides.service();
    if (!Objects.equals(providesService, INSTRUMENT_SPI1_SERVICE_TYPE)) {
      throw errorModuleProvideWrong(file, moduleName, providesService);
    }
  }

  private static ARException errorModuleDisallowed(
    final Path file,
    final String source,
    final String target)
  {
    return new ARException(
      "Module 'requires' a disallowed module.",
      "error-module-disallowed",
      Map.ofEntries(
        Map.entry("File", file.toAbsolutePath().toString()),
        Map.entry("Module Source", source),
        Map.entry("Module Disallowed", target)
      ),
      Optional.empty()
    );
  }

  private static ARException errorModuleUses(
    final Path file,
    final String moduleName)
  {
    return new ARException(
      "Module 'uses' one or more services.",
      "error-module-uses-disallowed",
      Map.ofEntries(
        Map.entry("File", file.toAbsolutePath().toString()),
        Map.entry("Module Source", moduleName)
      ),
      Optional.empty()
    );
  }

  private static ARException errorModuleProvideExactlyOnce(
    final Path file,
    final String moduleName)
  {
    return new ARException(
      "Module must 'provide' exactly one instrument service.",
      "error-module-instrument-service",
      Map.ofEntries(
        Map.entry("File", file.toAbsolutePath().toString()),
        Map.entry("Module Source", moduleName),
        Map.entry(
          "Service Class",
          ARI1InstrumentFactoryType.class.getCanonicalName()
        )
      ),
      Optional.empty()
    );
  }

  private static ARException errorModuleProvideWrong(
    final Path file,
    final String moduleName,
    final String providesService)
  {
    return new ARException(
      "Module exports an instrument service of the wrong type.",
      "error-module-instrument-service-incorrect",
      Map.ofEntries(
        Map.entry("File", file.toAbsolutePath().toString()),
        Map.entry("Module Source", moduleName),
        Map.entry(
          "Service Class (Required)",
          ARI1InstrumentFactoryType.class.getCanonicalName()
        ),
        Map.entry(
          "Service Class (Actual)",
          providesService
        )
      ),
      Optional.empty()
    );
  }

  private static ARException wrap(
    final Exception e)
  {
    if (e instanceof final ARException ex) {
      return ex;
    }

    return new ARException(
      Objects.requireNonNullElse(
        e.getMessage(),
        e.getClass().getSimpleName()
      ),
      e,
      "error-exception",
      Map.of(),
      Optional.empty()
    );
  }

  private interface LoadCompletionType
  {
    ARInstrumentLoaderType complete(
      ModuleLayer instrumentLayer,
      ARInstrumentReadResultType instrumentDescription,
      URLClassLoader classLoader
    );
  }

  private static final class ARInstrumentExecutable1
    implements ARInstrumentExecutableType
  {
    private final AtomicBoolean closed;
    private final ARInstrumentLoader1 loader;
    private final ARI1InstrumentType instrument;
    private final ARI1InstrumentContextType services;
    private final ARInstrumentDescription description;

    private ARInstrumentExecutable1(
      final ARInstrumentLoader1 inLoader,
      final ARI1InstrumentContextType inServices,
      final ARI1InstrumentType inInstrument,
      final ARInstrumentDescription inDescription)
    {
      this.loader =
        Objects.requireNonNull(inLoader, "Loader");
      this.services =
        Objects.requireNonNull(inServices, "Services");
      this.instrument =
        Objects.requireNonNull(inInstrument, "Instrument");
      this.description =
        Objects.requireNonNull(inDescription, "Description");
      this.closed =
        new AtomicBoolean(false);
    }

    @Override
    public ARInstrumentDescription description()
    {
      return this.description;
    }

    @Override
    public void close()
      throws ARException
    {
      if (this.closed.compareAndSet(false, true)) {
        this.loader.close();
      }
    }

    @Override
    public boolean isClosed()
    {
      return this.closed.get();
    }
  }

  private static final class ARInstrumentLoader1
    implements ARInstrumentLoaderType
  {
    private final ARI1InstrumentDescription instrumentDescription;
    private final URLClassLoader classLoader;
    private final ModuleLayer moduleLayer;
    private final ARI1InstrumentFactoryType instrumentFactory;
    private final AtomicBoolean closed;
    private final ARInstrumentContextConstructorType serviceConstructor;

    private ARInstrumentLoader1(
      final ARInstrumentContextConstructorType inServiceConstructor,
      final ARI1InstrumentDescription inInstrumentDescription,
      final URLClassLoader inClassLoader,
      final ModuleLayer inModuleLayer,
      final ARI1InstrumentFactoryType inInstrument)
    {
      this.serviceConstructor =
        Objects.requireNonNull(inServiceConstructor, "Service Constructor");
      this.instrumentDescription =
        Objects.requireNonNull(inInstrumentDescription, "Description");
      this.classLoader =
        Objects.requireNonNull(inClassLoader, "ClassLoader");
      this.moduleLayer =
        Objects.requireNonNull(inModuleLayer, "ModuleLayer");
      this.instrumentFactory =
        Objects.requireNonNull(inInstrument, "Instrument");
      this.closed =
        new AtomicBoolean(false);
    }

    @Override
    public ARInstrumentExecutableType execute(
      final ARInstrumentPortAssignerType assigner,
      final ARInstrumentInstanceID instanceID)
      throws ARException
    {
      Objects.requireNonNull(assigner, "assigner");
      Objects.requireNonNull(instanceID, "InstanceID");

      final var services =
        this.serviceConstructor.createContextV1(this.instrumentDescription);
      final var instrument =
        this.instrumentFactory.createInstrument(services);

      final var description =
        ARInstrumentDescriptionsV1.fromV1(
          assigner,
          instanceID,
          this.instrumentDescription
        );

      return new ARInstrumentExecutable1(this, services, instrument, description);
    }

    @Override
    public void close()
      throws ARException
    {
      if (this.closed.compareAndSet(false, true)) {
        try {
          this.classLoader.close();
        } catch (final Exception e) {
          throw wrap(e);
        }
      }
    }

    @Override
    public boolean isClosed()
    {
      return this.closed.get();
    }
  }

  private static final class UnsupportedLoader
    implements ARInstrumentLoaderType
  {
    UnsupportedLoader()
    {

    }

    @Override
    public ARInstrumentExecutableType execute(
      final ARInstrumentPortAssignerType assigner,
      final ARInstrumentInstanceID instanceID)
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public void close()
    {
      // Nothing required.
    }

    @Override
    public boolean isClosed()
    {
      return false;
    }
  }
}
