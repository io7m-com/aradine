/*
 * Copyright © 2023 Mark Raynsford <code@io7m.com> https://www.io7m.com
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
 * Modular programmable synthesis (Test suite)
 */

open module com.io7m.aradine.tests
{
  requires com.io7m.aradine.annotations;
  requires com.io7m.aradine.api;
  requires com.io7m.aradine.database.api;
  requires com.io7m.aradine.database.sqlite3;
  requires com.io7m.aradine.ensemble;
  requires com.io7m.aradine.envelope.table1;
  requires com.io7m.aradine.filter.biquad1;
  requires com.io7m.aradine.filter.recursive1;
  requires com.io7m.aradine.filter.statevar1;
  requires com.io7m.aradine.instrument.codegen;
  requires com.io7m.aradine.instrument.grain_sampler_m0;
  requires com.io7m.aradine.instrument.loader.api;
  requires com.io7m.aradine.instrument.loader;
  requires com.io7m.aradine.instrument.sampler_m0;
  requires com.io7m.aradine.instrument.sampler_p0;
  requires com.io7m.aradine.instrument.sampler_xp0;
  requires com.io7m.aradine.instrument.spi1.json_data;
  requires com.io7m.aradine.instrument.spi1;
  requires com.io7m.aradine.inventory.api;
  requires com.io7m.aradine.inventory;
  requires com.io7m.aradine.sample_map.aurantium;
  requires com.io7m.aradine.tests.arbitraries;

  requires com.io7m.anethum.api;
  requires com.io7m.jaffirm.core;
  requires com.io7m.jattribute.core;
  requires com.io7m.jmulticlose.core;
  requires com.io7m.jmurmur.core;
  requires com.io7m.jsamplebuffer.api;
  requires com.io7m.jsamplebuffer.vanilla;
  requires com.io7m.jsamplebuffer.xmedia;
  requires com.io7m.junreachable.core;
  requires com.io7m.lanark.core;
  requires com.io7m.mime2045.core;
  requires com.io7m.verona.core;
  requires it.unimi.dsi.fastutil.core;
  requires java.desktop;
  requires java.sql;
  requires jnajack;
  requires net.bytebuddy.agent;
  requires net.bytebuddy;
  requires net.jqwik.api;
  requires org.apache.commons.io;
  requires org.apache.commons.math4.core;
  requires org.apache.commons.math4.legacy.core;
  requires org.apache.commons.math4.legacy;
  requires org.apache.commons.math4.transform;
  requires org.apache.commons.numbers.complex;
  requires org.junit.jupiter.params;
  requires org.knowm.xchart;
  requires org.mockito.junit.jupiter;
  requires org.mockito;
  requires org.slf4j;
  requires tools.jackson.core;
  requires tools.jackson.databind;

  requires org.junit.jupiter.api;
  requires org.junit.jupiter.engine;
  requires org.junit.platform.commons;
  requires org.junit.platform.engine;
  requires org.junit.platform.launcher;
}
