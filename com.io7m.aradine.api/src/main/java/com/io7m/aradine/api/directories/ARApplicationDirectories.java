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

package com.io7m.aradine.api.directories;

import com.io7m.jade.api.ApplicationDirectories;
import com.io7m.jade.api.ApplicationDirectoriesType;
import com.io7m.jade.api.ApplicationDirectoryConfiguration;

import java.nio.file.Path;

/**
 * The aradine application directories.
 */

public final class ARApplicationDirectories
{
  private ARApplicationDirectories()
  {

  }

  /**
   * @return The aradine application directories.
   */

  public static ApplicationDirectoriesType directories()
  {
    final var directoryConfiguration =
      ApplicationDirectoryConfiguration.builder()
        .setApplicationName("com.io7m.aradine")
        .setPortablePropertyName("com.io7m.aradine.portable")
        .setOverridePropertyName("com.io7m.aradine.override")
        .build();

    return ApplicationDirectories.get(directoryConfiguration);
  }

  /**
   * @param directories The application directories
   *
   * @return The inventory blob directory
   */

  public static Path inventoryBlobs(
    final ApplicationDirectoriesType directories)
  {
    return directories.dataDirectory()
      .resolve("inventory")
      .resolve("blobs");
  }

  /**
   * @param directories The application directories
   *
   * @return The inventory database file
   */

  public static Path inventoryDatabase(
    final ApplicationDirectoriesType directories)
  {
    return directories.dataDirectory()
      .resolve("inventory")
      .resolve("inventory.db");
  }
}
