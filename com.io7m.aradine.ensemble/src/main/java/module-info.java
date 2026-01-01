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

/**
 * Modular programmable synthesis (Ensemble)
 */

module com.io7m.aradine.ensemble
{
  requires static org.osgi.annotation.bundle;
  requires static org.osgi.annotation.versioning;

  requires com.io7m.aradine.api;
  requires com.io7m.aradine.database.api;
  requires com.io7m.aradine.inventory.api;

  requires com.fasterxml.jackson.annotation;
  requires com.io7m.anethum.api;
  requires com.io7m.dixmont.core;
  requires com.io7m.jaffirm.core;
  requires com.io7m.jmulticlose.core;
  requires com.io7m.jxe.core;
  requires com.io7m.lanark.core;
  requires com.io7m.trasco.api;
  requires com.io7m.trasco.vanilla;
  requires com.io7m.verona.core;
  requires java.sql;
  requires org.jgrapht.core;
  requires org.slf4j;
  requires org.xerial.sqlitejdbc;
  requires tools.jackson.core;
  requires tools.jackson.databind;
  requires com.io7m.jattribute.core;

  opens com.io7m.aradine.ensemble.internal.model
    to tools.jackson.databind;

  exports com.io7m.aradine.ensemble.internal.database
    to com.io7m.aradine.tests;
  exports com.io7m.aradine.ensemble.internal.graph
    to com.io7m.aradine.tests;
  exports com.io7m.aradine.ensemble.internal.json_v1
    to com.io7m.aradine.tests;
  exports com.io7m.aradine.ensemble.internal.model
    to com.io7m.aradine.tests;
  exports com.io7m.aradine.ensemble.internal.ops_v1
    to com.io7m.aradine.tests;
}
