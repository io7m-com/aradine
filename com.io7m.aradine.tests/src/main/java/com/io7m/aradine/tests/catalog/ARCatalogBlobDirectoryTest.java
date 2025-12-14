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

package com.io7m.aradine.tests.catalog;

import com.io7m.aradine.catalog.api.ARCatalogHash;
import com.io7m.aradine.catalog.api.ARCatalogHashAlgorithm;
import com.io7m.aradine.catalog.internal.ARCatalogBlobDirectory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ARCatalogBlobDirectoryTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ARCatalogBlobDirectoryTest.class);

  private ARCatalogBlobDirectory blobDirectory;

  @BeforeEach
  public void setup(
    final @TempDir Path directory)
  {
    this.blobDirectory =
      new ARCatalogBlobDirectory(directory);
  }

  @Test
  public void testWriteSimple(
    final @TempDir Path sourceDirectory)
    throws Exception
  {
    final var sourceFile = sourceDirectory.resolve("hello.txt");
    Files.writeString(sourceFile, "HELLO");

    final var helloHash =
      new ARCatalogHash(
        ARCatalogHashAlgorithm.SHA_256,
        "3733cd977ff8eb18b987357e22ced99f46097f31ecb239e878ae63760e83e4d5"
      );

    final var file =
      this.blobDirectory.copyIn(
        helloHash,
        sourceFile,
        progress -> LOG.debug("Progress: {}", progress),
        () -> false
      );

    Assertions.assertTrue(Files.isRegularFile(file));
    Assertions.assertEquals("HELLO", Files.readString(file));
  }
}
