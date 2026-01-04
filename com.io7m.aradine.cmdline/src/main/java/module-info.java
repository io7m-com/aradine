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

/**
 * Modular programmable synthesis (Command-line tools)
 */

module com.io7m.aradine.cmdline
{
  requires static org.osgi.annotation.bundle;
  requires static org.osgi.annotation.versioning;

  requires com.io7m.aradine.api;
  requires com.io7m.aradine.database.api;
  requires com.io7m.aradine.instrument.loader.api;
  requires com.io7m.aradine.instrument.loader;
  requires com.io7m.aradine.inventory.api;
  requires com.io7m.aradine.inventory;
  requires com.io7m.aradine.sample_map.aurantium;

  requires com.io7m.jade.api;
  requires com.io7m.jmulticlose.core;
  requires com.io7m.lanark.core;
  requires com.io7m.mime2045.core;
  requires com.io7m.quarrel.core;
  requires com.io7m.quarrel.ext.logback;
  requires com.io7m.seltzer.api;
  requires com.io7m.seltzer.slf4j;
  requires com.io7m.verona.core;
  requires org.slf4j;
  requires tools.jackson.core;
  requires tools.jackson.databind;
  requires com.io7m.aradine.instrument.codegen;
  requires com.io7m.aradine.instrument.spi1.json_data;
  requires com.io7m.aradine.instrument.spi1;

  exports com.io7m.aradine.cmdline;
}
