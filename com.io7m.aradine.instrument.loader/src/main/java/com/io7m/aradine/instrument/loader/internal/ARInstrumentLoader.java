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
import com.io7m.aradine.api.instrument.ARInstrumentException;
import com.io7m.aradine.api.instrument.ARInstrumentInstanceID;
import com.io7m.aradine.api.instrument.ARInstrumentType;
import com.io7m.aradine.instrument.loader.api.ARInstrumentLoaderServicesConstructorType;
import com.io7m.aradine.instrument.loader.api.ARInstrumentLoaderType;
import com.io7m.aradine.instrument.loader.api.ARInstrumentPortAssignerType;
import com.io7m.aradine.instrument.loader.api.ARInstrumentReadResultType;
import com.io7m.aradine.instrument.loader.api.ARInstrumentReadV1;
import com.io7m.aradine.instrument.loader.api.ARInstrumentReaderFactoryType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentContextType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentDescription;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentFactoryType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentType;
import com.io7m.aradine.instrument.spi1.ARI1VersionQualifier;
import com.io7m.verona.core.VersionQualifier;

import java.io.IOException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.HashMap;
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
   * Create a new instrument loader.
   *
   * @param readers            The reader factory
   * @param serviceConstructor The service constructor
   * @param file               The instrument file
   *
   * @return A loader
   *
   * @throws ARInstrumentException On errors
   */

  public static ARInstrumentLoaderType create(
    final ARInstrumentReaderFactoryType readers,
    final ARInstrumentLoaderServicesConstructorType serviceConstructor,
    final Path file)
    throws ARInstrumentException
  {
    Objects.requireNonNull(serviceConstructor, "Service Constructor");
    Objects.requireNonNull(readers, "Readers");
    Objects.requireNonNull(file, "File");

    try {
      final ARInstrumentReadResultType instrumentDescription;
      try (var instrumentReader = readers.create(file)) {
        instrumentDescription = instrumentReader.executeAndParse();
      }

      final ModuleDescriptor moduleDescriptor =
        readModuleDescriptorFromJar(file);

      checkModule(file, moduleDescriptor);

      final var instrumentModuleReference =
        new ARInstrumentModuleReference(moduleDescriptor, file);
      final var instrumentModuleFinder =
        new ARInstrumentModuleFinder(instrumentModuleReference);
      final var moduleName =
        moduleDescriptor.name();

      final var instrumentClassLoader =
        new URLClassLoader(
          new URL[]{file.toUri().toURL()},
          null
        );

      final var instrumentConfiguration =
        BASE_LAYER.configuration()
          .resolve(
            ModuleFinder.of(),
            instrumentModuleFinder,
            Set.of(moduleName)
          );

      final var instrumentLayer =
        BASE_LAYER.defineModulesWithOneLoader(
          instrumentConfiguration,
          instrumentClassLoader
        );

      return createV1(
        serviceConstructor,
        instrumentLayer,
        instrumentDescription,
        instrumentClassLoader
      );
    } catch (final Exception e) {
      throw wrap(e);
    }
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
    final ARInstrumentLoaderServicesConstructorType serviceConstructor,
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
    throws ARInstrumentException
  {
    final var moduleName = moduleDescriptor.name();
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

  private static ModuleReference findInstrumentModuleReference(
    final Path file,
    final ModuleFinder instrumentModuleFinder)
    throws ARInstrumentException
  {
    final var moduleReferences = instrumentModuleFinder.findAll();
    if (moduleReferences.isEmpty()) {
      throw errorModuleNonexistent(file);
    }
    if (moduleReferences.size() > 1) {
      throw errorModuleTooMany(file, moduleReferences);
    }
    return moduleReferences.iterator().next();
  }

  private static ARInstrumentException errorModuleTooMany(
    final Path file,
    final Set<ModuleReference> moduleReferences)
  {
    final var names =
      new HashMap<String, String>(moduleReferences.size() + 1);
    var index = 0;
    for (final var ref : moduleReferences) {
      names.put("Module " + index, ref.descriptor().name());
      ++index;
    }
    names.put("File", file.toAbsolutePath().toString());

    return new ARInstrumentException(
      "Multiple modules detected.",
      "error-module-multiple",
      Map.copyOf(names),
      Optional.empty()
    );
  }

  private static ARInstrumentException errorModuleDisallowed(
    final Path file,
    final String source,
    final String target)
  {
    return new ARInstrumentException(
      "Module 'requires' a disallowed module.",
      "error-module-disallowed",
      Map.ofEntries(
        Map.entry("File", file.toAbsolutePath().toString()),
        Map.entry("Source", source),
        Map.entry("Target", target)
      ),
      Optional.empty()
    );
  }

  private static ARInstrumentException errorModuleUses(
    final Path file,
    final String moduleName)
  {
    return new ARInstrumentException(
      "Module 'uses' one or more services.",
      "error-module-uses-disallowed",
      Map.ofEntries(
        Map.entry("File", file.toAbsolutePath().toString()),
        Map.entry("Source", moduleName)
      ),
      Optional.empty()
    );
  }

  private static ARInstrumentException errorModuleProvideExactlyOnce(
    final Path file,
    final String moduleName)
  {
    return new ARInstrumentException(
      "Module must 'provide' exactly one instrument service.",
      "error-module-instrument-service",
      Map.ofEntries(
        Map.entry("File", file.toAbsolutePath().toString()),
        Map.entry("Source", moduleName),
        Map.entry(
          "Service Class",
          ARI1InstrumentFactoryType.class.getCanonicalName()
        )
      ),
      Optional.empty()
    );
  }

  private static ARInstrumentException errorModuleProvideWrong(
    final Path file,
    final String moduleName,
    final String providesService)
  {
    return new ARInstrumentException(
      "Module exports an instrument service of the wrong type.",
      "error-module-instrument-service-incorrect",
      Map.ofEntries(
        Map.entry("File", file.toAbsolutePath().toString()),
        Map.entry("Source", moduleName),
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

  private static ARInstrumentException errorModuleNonexistent(
    final Path file)
  {
    return new ARInstrumentException(
      "Module does not exist.",
      "error-module-nonexistent",
      Map.ofEntries(
        Map.entry("File", file.toAbsolutePath().toString())
      ),
      Optional.empty()
    );
  }

  private static ARInstrumentException wrap(
    final Exception e)
  {
    if (e instanceof final ARInstrumentException ex) {
      return ex;
    }

    return new ARInstrumentException(
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

  private static final class ARInstrument1
    implements ARInstrumentType
  {
    private final AtomicBoolean closed;
    private final ARInstrumentLoader1 loader;
    private final ARI1InstrumentType instrument;
    private final ARI1InstrumentContextType services;
    private final ARInstrumentDescription description;

    private ARInstrument1(
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
      throws ARInstrumentException
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
    private final ARInstrumentLoaderServicesConstructorType serviceConstructor;

    private ARInstrumentLoader1(
      final ARInstrumentLoaderServicesConstructorType inServiceConstructor,
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
    public ARInstrumentType execute(
      final ARInstrumentPortAssignerType assigner,
      final ARInstrumentInstanceID instanceID)
      throws ARInstrumentException
    {
      Objects.requireNonNull(assigner, "assigner");
      Objects.requireNonNull(instanceID, "InstanceID");

      final var services =
        this.serviceConstructor.createServicesV1(this.instrumentDescription);
      final var instrument =
        this.instrumentFactory.createInstrument(services);

      final var description =
        ARInstrumentDescriptionsV1.fromV1(
          assigner,
          instanceID,
          this.instrumentDescription
        );

      return new ARInstrument1(this, services, instrument, description);
    }

    private static VersionQualifier qualifierOf(
      final ARI1VersionQualifier x)
    {
      return new VersionQualifier(x.text());
    }

    @Override
    public void close()
      throws ARInstrumentException
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
}
