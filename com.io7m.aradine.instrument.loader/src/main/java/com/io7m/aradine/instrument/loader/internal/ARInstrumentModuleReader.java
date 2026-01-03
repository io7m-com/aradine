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

package com.io7m.aradine.instrument.loader.internal;

import java.io.IOException;
import java.io.InputStream;
import java.lang.module.ModuleReader;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;
import java.util.jar.JarFile;
import java.util.stream.Stream;

final class ARInstrumentModuleReader
  implements ModuleReader
{
  private final JarFile jarFile;

  ARInstrumentModuleReader(
    final Path inJarFile)
    throws IOException
  {
    this.jarFile =
      new JarFile(inJarFile.toFile());
  }

  @Override
  public Optional<URI> find(
    final String name)
    throws IOException
  {
    return Optional.empty();
  }

  @Override
  public Optional<InputStream> open(
    final String name)
    throws IOException
  {
    final var entry = this.jarFile.getJarEntry(name);
    if (entry == null) {
      return Optional.empty();
    }
    return Optional.of(this.jarFile.getInputStream(entry));
  }

  @Override
  public Stream<String> list()
    throws IOException
  {
    return Stream.empty();
  }

  @Override
  public void close()
    throws IOException
  {
    this.jarFile.close();
  }
}
