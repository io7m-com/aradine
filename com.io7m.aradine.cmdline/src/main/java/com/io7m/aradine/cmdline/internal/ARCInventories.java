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

import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.directories.ARApplicationDirectories;
import com.io7m.aradine.instrument.loader.ARInstrumentReaders;
import com.io7m.aradine.inventory.ARInventories;
import com.io7m.aradine.inventory.api.ARInventoryConfiguration;
import com.io7m.aradine.inventory.api.ARInventoryType;
import com.io7m.jade.api.ApplicationDirectoriesType;
import com.io7m.jmulticlose.core.CloseableCollectionType;

import java.io.IOException;
import java.nio.file.Files;

/**
 * Functions over inventories.
 */

public final class ARCInventories
{
  private ARCInventories()
  {

  }

  /**
   * Open an inventory.
   *
   * @param directories The application directories
   * @param resources   The resources
   *
   * @return An inventory
   *
   * @throws IOException On errors
   * @throws ARException On errors
   */

  public static ARInventoryType openInventory(
    final ApplicationDirectoriesType directories,
    final CloseableCollectionType<ARException> resources)
    throws IOException, ARException
  {
    final var instrumentReaders =
      new ARInstrumentReaders();

    final var blobDirectory =
      ARApplicationDirectories.inventoryBlobs(directories);
    Files.createDirectories(blobDirectory);

    final var inventoryDatabase =
      ARApplicationDirectories.inventoryDatabase(directories);
    Files.createDirectories(inventoryDatabase.getParent());

    final var inventoryConfiguration =
      ARInventoryConfiguration.builder()
        .setDataDirectory(blobDirectory)
        .setDatabaseFile(inventoryDatabase)
        .setReaders(instrumentReaders)
        .build();

    return resources.add(ARInventories.open(inventoryConfiguration));
  }
}
