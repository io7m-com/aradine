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

package com.io7m.aradine.tests.sample_map.aradine;

import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.progress.ARProgress;
import com.io7m.aradine.api.sample_map.ARSampleMapLoadConfiguration;
import com.io7m.aradine.sample_map.aurantium.ARASampleMaps;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class ARRSampleMapTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ARRSampleMapTest.class);

  private Path directory;
  private ARASampleMaps parsers;

  @BeforeEach
  public void setup()
    throws Exception
  {
    this.directory =
      Files.createTempDirectory("aradine");
    this.parsers =
      new ARASampleMaps();
  }

  @AfterEach
  public void tearDown()
  {
    try {
      FileUtils.deleteDirectory(this.directory.toFile());
    } catch (final Throwable e) {
      // Don't care
    }
  }

  @Test
  public void testOpenNonexistent()
  {
    final var file =
      this.directory.resolve("nonexistent.aam");

    final var ex =
      assertThrows(
        ARException.class, () -> {
          try (var _ = this.parsers.open(file, ARRSampleMapTest::logProgress)) {
            // Nothing.
          }
        });

    assertEquals("error-io", ex.errorCode());
  }

  @Test
  public void testOpenUnsupported()
    throws Exception
  {
    final var file =
      this.resourceOf("sample-9999.aam");

    final var ex =
      assertThrows(
        ARException.class, () -> {
          try (var _ = this.parsers.open(file, ARRSampleMapTest::logProgress)) {
            // Nothing.
          }
        });

    assertEquals("error-sample-map-format-version", ex.errorCode());
  }

  @Test
  public void testOpenCorrupt()
    throws Exception
  {
    final var file =
      this.resourceOf("end-missing.aam");

    final var ex =
      assertThrows(
        ARException.class, () -> {
          try (var _ = this.parsers.open(file, ARRSampleMapTest::logProgress)) {
            // Nothing.
          }
        });

    assertEquals("error-file-end-missing", ex.errorCode());
  }

  @Test
  public void testOpenSimple()
    throws Exception
  {
    final var file =
      this.resourceOf("sample.aam");

    try (var sampleFile =
           this.parsers.open(file, ARRSampleMapTest::logProgress)) {
      try (var sampleMap = sampleFile.load(
        ARSampleMapLoadConfiguration.builder()
          .setProgressConsumer(ARRSampleMapTest::logProgress)
          .setSampleRate(22050.0)
          .build()
      )) {

      }
    }
  }

  private static void logProgress(
    final ARProgress progress)
  {
    LOG.debug("Progress: {}", progress);
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
