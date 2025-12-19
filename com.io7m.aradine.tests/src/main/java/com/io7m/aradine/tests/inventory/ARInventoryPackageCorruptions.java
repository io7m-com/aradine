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

package com.io7m.aradine.tests.inventory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class ARInventoryPackageCorruptions
{
  private ARInventoryPackageCorruptions()
  {

  }

  public static void main(
    final String[] args)
    throws Exception
  {
    final var outputDirectory = Paths.get(args[0]);
    Files.createDirectories(outputDirectory);

    final var sourceFile =
      resourceOf(outputDirectory, "sampler_m0.jar");

    corruptRemoveManifest(
      outputDirectory,
      sourceFile,
      "sampler_m0-no_manifest.jar"
    );
    corruptReplaceManifestWithNoAradine(
      outputDirectory,
      sourceFile,
      "sampler_m0-no_manifest_aradine.jar"
    );
    corruptMissingInstrumentFile(
      outputDirectory,
      sourceFile,
      "sampler_m0-missing_instrument.jar"
    );
    corruptInvalidInstrumentFile(
      outputDirectory,
      sourceFile,
      "sampler_m0-invalid_instrument.jar"
    );
    corruptMaliciousInstrumentFile(
      outputDirectory,
      sourceFile,
      "sampler_m0-malicious_instrument.jar"
    );
    corruptUnsupportedInstrumentFile(
      outputDirectory,
      sourceFile,
      "sampler_m0-unsupported_instrument.jar"
    );
    corruptBadJSON(
      outputDirectory,
      sourceFile,
      "sampler_m0-corrupt_json.jar"
    );
  }

  private static void corruptMaliciousInstrumentFile(
    final Path outputDirectory,
    final Path sourceFile,
    final String name)
    throws IOException
  {
    replaceEntry(
      sourceFile,
      outputDirectory.resolve(name),
      "com/io7m/aradine/instrument/sampler_m0/internal/instrument.json",
      """
        {
          "%Schema": "urn:com.io7m.aradine.instrument:1.0"
        }
        """
    );
  }

  private static void corruptBadJSON(
    final Path outputDirectory,
    final Path sourceFile,
    final String name)
    throws IOException
  {
    replaceEntry(
      sourceFile,
      outputDirectory.resolve(name),
      "com/io7m/aradine/instrument/sampler_m0/internal/instrument.json",
      "{"
    );
  }

  private static void corruptInvalidInstrumentFile(
    final Path outputDirectory,
    final Path sourceFile,
    final String name)
    throws IOException
  {
    replaceEntry(
      sourceFile,
      outputDirectory.resolve(name),
      "com/io7m/aradine/instrument/sampler_m0/internal/instrument.json",
      "{}"
    );
  }

  private static void corruptUnsupportedInstrumentFile(
    final Path outputDirectory,
    final Path sourceFile,
    final String name)
    throws IOException
  {
    replaceEntry(
      sourceFile,
      outputDirectory.resolve(name),
      "com/io7m/aradine/instrument/sampler_m0/internal/instrument.json",
      """
        {
          "%Schema": "urn:what"
        }
        """
    );
  }

  private static void corruptMissingInstrumentFile(
    final Path outputDirectory,
    final Path sourceFile,
    final String name)
    throws IOException
  {
    removeEntry(
      sourceFile,
      outputDirectory.resolve(name),
      "com/io7m/aradine/instrument/sampler_m0/internal/instrument.json"
    );
  }

  private static void corruptRemoveManifest(
    final Path outputDirectory,
    final Path sourceFile,
    final String name)
    throws IOException
  {
    removeEntry(
      sourceFile,
      outputDirectory.resolve(name),
      "META-INF/MANIFEST.MF"
    );
  }

  private static void corruptReplaceManifestWithNoAradine(
    final Path outputDirectory,
    final Path sourceFile,
    final String name)
    throws IOException
  {
    replaceEntry(
      sourceFile,
      outputDirectory.resolve(name),
      "META-INF/MANIFEST.MF",
      "Manifest-Version: 1.0\n\n"
    );
  }

  private static Path resourceOf(
    final Path directory,
    final String name)
    throws IOException
  {
    final var path =
      "/com/io7m/aradine/tests/%s".formatted(name);
    final var url =
      ARInventoryTest.class.getResource(path);

    Objects.requireNonNull(url, "URL");
    try (var stream = url.openStream()) {
      final var output = directory.resolve(name);
      Files.copy(stream, output, StandardCopyOption.REPLACE_EXISTING);
      return output;
    }
  }

  private static void removeEntry(
    final Path source,
    final Path target,
    final String entryName)
    throws IOException
  {
    try (var zis = new ZipInputStream(Files.newInputStream(source));
         var zos = new ZipOutputStream(Files.newOutputStream(target))) {
      while (true) {
        final var entry = zis.getNextEntry();
        if (entry == null) {
          break;
        }
        if (entry.getName().equals(entryName)) {
          continue;
        }
        zos.putNextEntry(new ZipEntry(entry.getName()));
        zis.transferTo(zos);
        zos.closeEntry();
      }
    }
  }

  private static void replaceEntry(
    final Path source,
    final Path target,
    final String entryName,
    final String text)
    throws IOException
  {
    try (var zis = new ZipInputStream(Files.newInputStream(source));
         var zos = new ZipOutputStream(Files.newOutputStream(target))) {
      while (true) {
        final var entry = zis.getNextEntry();
        if (entry == null) {
          break;
        }
        zos.putNextEntry(new ZipEntry(entry.getName()));
        if (entry.getName().equals(entryName)) {
          zos.write(text.getBytes(StandardCharsets.UTF_8));
        } else {
          zis.transferTo(zos);
        }
        zos.closeEntry();
      }
    }
  }
}
