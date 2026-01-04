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

package com.io7m.aradine.tests.cmdline;

import com.io7m.aradine.cmdline.ARCMain;
import com.io7m.aradine.tests.inventory.ARInventoryTest;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class ARCmdlineTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(ARCmdlineTest.class);

  private Path directory;
  private PrintStream errThen;
  private PrintStream outThen;
  private ByteArrayOutputStream errByte;
  private ByteArrayOutputStream outByte;
  private PrintStream errPrint;
  private PrintStream outPrint;

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.directory = Files.createTempDirectory("aradine");
    System.setProperty(
      "com.io7m.aradine.override",
      this.directory.toAbsolutePath().toString()
    );

    this.errThen = System.err;
    this.outThen = System.out;
    this.errByte = new ByteArrayOutputStream();
    this.outByte = new ByteArrayOutputStream();
    this.errPrint = new PrintStream(this.errByte);
    this.outPrint = new PrintStream(this.outByte);
    System.setOut(this.outPrint);
    System.setErr(this.errPrint);
  }

  @AfterEach
  public void tearDown(
    final TestInfo info)
  {
    System.err.flush();
    System.out.flush();
    System.setOut(this.outThen);
    System.setErr(this.errThen);

    LOG.info("{}: stderr: {}", info.getDisplayName(), this.errByte.toString());
    LOG.info("{}: stdout: {}", info.getDisplayName(), this.outByte.toString());

    try {
      FileUtils.deleteDirectory(this.directory.toFile());
    } catch (final IOException e) {
      // Nothing
    }

    System.clearProperty("com.io7m.aradine.override");
  }

  @Test
  public void testInfo()
  {
    final int r = ARCMain.mainExitless(
      new String[]{
        "info"
      }
    );

    assertEquals(0, r);
    final var data = JsonMapper.shared().readTree(this.outByte.toByteArray());
    assertInstanceOf(ObjectNode.class, data);
  }

  @Test
  public void testInventoryListInstruments()
  {
    final int r = ARCMain.mainExitless(
      new String[]{
        "inventory",
        "list-instruments"
      }
    );

    assertEquals(0, r);
    final var data = JsonMapper.shared().readTree(this.outByte.toByteArray());
    assertInstanceOf(ArrayNode.class, data);
  }

  @Test
  public void testInventoryInstrumentInstallUninstall()
    throws Exception
  {
    final var file =
      this.resourceOf("sampler_m0.jar");

    int r = ARCMain.mainExitless(
      new String[]{
        "inventory",
        "install-instrument",
        "--file",
        file.toString()
      }
    );
    assertEquals(0, r);

    r = ARCMain.mainExitless(
      new String[]{
        "inventory",
        "list-instruments"
      }
    );
    assertEquals(0, r);

    r = ARCMain.mainExitless(
      new String[]{
        "inventory",
        "uninstall-instrument",
        "--id",
        "com.io7m.aradine:com.io7m.aradine.instrument.sampler_m0:0.0.2-SNAPSHOT"
      }
    );
    assertEquals(0, r);
  }


  @Test
  public void testInventorySampleMapInstallUninstall()
    throws Exception
  {
    final var file =
      this.resourceOf("sample.aam");

    int r = ARCMain.mainExitless(
      new String[]{
        "inventory",
        "install-sample-map",
        "--file",
        file.toString()
      }
    );
    assertEquals(0, r);

    r = ARCMain.mainExitless(
      new String[]{
        "inventory",
        "list-sample-maps"
      }
    );
    assertEquals(0, r);

    r = ARCMain.mainExitless(
      new String[]{
        "inventory",
        "uninstall-sample-map",
        "--id",
        "com.io7m.example_group:com.io7m.example:1.0.0"
      }
    );
    assertEquals(0, r);
  }


  @Test
  public void testInventoryUninstallUnparseable()
    throws Exception
  {
    final int r = ARCMain.mainExitless(
      new String[]{
        "inventory",
        "uninstall-instrument",
        "--id",
        "what?"
      }
    );
    assertNotEquals(0, r);
  }

  @Test
  public void testInventoryInstallNonexistent()
    throws Exception
  {
    final var file = Files.createTempFile("aradine", ".txt");
    Files.deleteIfExists(file);

    final int r = ARCMain.mainExitless(
      new String[]{
        "inventory",
        "install-instrument",
        "--file",
        file.toAbsolutePath().toString()
      }
    );
    assertNotEquals(0, r);
  }

  @Test
  public void testInventoryCheckInstrument0()
    throws IOException
  {
    final var file =
      this.resourceOf("sampler_m0_non-snapshot.jar");

    final int r = ARCMain.mainExitless(
      new String[]{
        "instrument",
        "check",
        "--file",
        file.toString()
      }
    );

    assertEquals(0, r);
  }

  @Test
  public void testInventoryCheckInstrument1()
    throws IOException
  {
    final var file =
      this.resourceOf("sampler_m0.jar");

    final int r = ARCMain.mainExitless(
      new String[]{
        "instrument",
        "check",
        "--file",
        file.toString()
      }
    );

    assertEquals(0, r);
  }

  @Test
  public void testInventoryCheckInstrument2()
    throws IOException
  {
    final var file =
      this.resourceOf("sampler_m0-corrupt_json.jar");

    final int r = ARCMain.mainExitless(
      new String[]{
        "instrument",
        "check",
        "--file",
        file.toString()
      }
    );

    assertEquals(1, r);
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
