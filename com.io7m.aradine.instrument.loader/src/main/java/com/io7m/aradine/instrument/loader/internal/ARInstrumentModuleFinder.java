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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

final class ARInstrumentModuleFinder
  implements ModuleFinder
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ARInstrumentModuleFinder.class);

  private final ARInstrumentModuleReference reference;

  ARInstrumentModuleFinder(
    final ARInstrumentModuleReference inReference)
  {
    this.reference =
      Objects.requireNonNull(inReference, "Reference");
  }

  @Override
  public Optional<ModuleReference> find(
    final String name)
  {
    LOG.trace("Finding module reference {}.", name);

    final var moduleDescriptor =
      this.reference.descriptor();
    final var descriptorName =
      moduleDescriptor.name();

    if (Objects.equals(descriptorName, name)) {
      return Optional.of(this.reference);
    }
    return Optional.empty();
  }

  @Override
  public Set<ModuleReference> findAll()
  {
    LOG.trace("Finding all module references ({}).", this.reference);
    return Set.of(this.reference);
  }
}
