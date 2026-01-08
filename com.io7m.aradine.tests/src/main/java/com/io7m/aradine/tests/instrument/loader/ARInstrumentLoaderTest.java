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

package com.io7m.aradine.tests.instrument.loader;

import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.instrument.ARInstrumentExecutableType;
import com.io7m.aradine.api.instrument.ARInstrumentInstanceID;
import com.io7m.aradine.instrument.loader.ARInstrumentLoaders;
import com.io7m.aradine.instrument.loader.api.ARInstrumentLoaderType;
import com.io7m.aradine.tests.inventory.ARInventoryTest;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class ARInstrumentLoaderTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ARInventoryTest.class);

  private Path directory;
  private ARInstrumentLoaders loaders;
  private ARInstrumentContextConstructor serviceConstructor;
  private ARFakeInstrumentPortAssigner assigner;

  @BeforeEach
  public void setup()
    throws Exception
  {
    this.directory =
      Files.createTempDirectory("aradine");
    this.serviceConstructor =
      new ARInstrumentContextConstructor();

    this.assigner = new ARFakeInstrumentPortAssigner();
    this.loaders = new ARInstrumentLoaders();
    Files.createDirectories(this.directory);
  }

  @AfterEach
  public void tearDown()
    throws Exception
  {
    try {
      FileUtils.deleteDirectory(this.directory.toFile());
    } catch (final IOException e) {
      // Don't care
    }
  }

  @Test
  public void testNonexistent()
    throws Exception
  {
    final var file =
      this.directory.resolve("nonexistent.jar");
    final var ex =
      assertThrows(
        ARException.class,
        () -> {
          this.loaders.createLoader(
            this.serviceConstructor,
            file
          );
        });
    assertEquals("error-file-nonexistent", ex.errorCode());
  }

  @Test
  public void testBadExtraDependency()
    throws Exception
  {
    final var file =
      this.resourceOf("bad_extradep.jar");
    final var ex =
      assertThrows(
        ARException.class,
        () -> {
          this.loaders.createLoader(
            this.serviceConstructor,
            file
          );
        });
    assertEquals("error-module-disallowed", ex.errorCode());
  }

  @Test
  public void testBadUses()
    throws Exception
  {
    final var file =
      this.resourceOf("bad_uses.jar");
    final var ex =
      assertThrows(
        ARException.class,
        () -> {
          this.loaders.createLoader(
            this.serviceConstructor,
            file
          );
        });
    assertEquals("error-module-uses-disallowed", ex.errorCode());
  }

  @Test
  public void testBadNoProvides()
    throws Exception
  {
    final var file =
      this.resourceOf("bad_noprovides.jar");
    final var ex =
      assertThrows(
        ARException.class,
        () -> {
          this.loaders.createLoader(
            this.serviceConstructor,
            file
          );
        });
    assertEquals("error-module-instrument-service", ex.errorCode());
  }

  @Test
  public void testBadProvides()
    throws Exception
  {
    final var file =
      this.resourceOf("bad_provides.jar");
    final var ex =
      assertThrows(
        ARException.class,
        () -> {
          this.loaders.createLoader(
            this.serviceConstructor,
            file
          );
        });
    assertEquals("error-module-instrument-service-incorrect", ex.errorCode());
  }

  @Test
  public void testSamplerM0()
    throws Exception
  {
    final var file =
      this.resourceOf("sampler_m0.jar");
    final var loader =
      this.loaders.createLoader(this.serviceConstructor, file);

    try (var instrument = loader.execute(this.assigner, ARInstrumentInstanceID.random())) {
      assertFalse(instrument.isClosed());
    }
  }

  @Test
  public void testSamplerM0Multiple()
    throws Exception
  {
    final var file =
      this.resourceOf("sampler_m0.jar");

    final ARInstrumentLoaderType[] loaders =
      new ARInstrumentLoaderType[10];

    for (int index = 0; index < loaders.length; ++index) {
      loaders[index] = this.loaders.createLoader(this.serviceConstructor, file);
    }

    final ARInstrumentExecutableType[] instruments =
      new ARInstrumentExecutableType[loaders.length];

    for (int index = 0; index < loaders.length; ++index) {
      instruments[index] =
        loaders[index].execute(this.assigner, ARInstrumentInstanceID.random());
    }

    for (int index = 0; index < loaders.length; ++index) {
      LOG.debug("Instrument: {}", instruments[index]);
    }
  }

  private Path resourceOf(
    final String name)
    throws IOException
  {
    final var path =
      "/com/io7m/aradine/tests/%s".formatted(name);
    final var url =
      ARInventoryTest.class.getResource(path);

    Objects.requireNonNull(url, "URL");
    try (var stream = url.openStream()) {
      final var output = this.directory.resolve(name);
      Files.copy(stream, output, StandardCopyOption.REPLACE_EXISTING);
      return output;
    }
  }
}
