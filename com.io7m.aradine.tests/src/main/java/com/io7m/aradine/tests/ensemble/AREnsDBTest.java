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

package com.io7m.aradine.tests.ensemble;

import com.io7m.aradine.api.ports.ARPortConnection;
import com.io7m.aradine.api.ports.ARPortDirection;
import com.io7m.aradine.api.ports.ARPortEnsemble;
import com.io7m.aradine.api.ports.ARPortID;
import com.io7m.aradine.api.ports.ARPortKind;
import com.io7m.aradine.api.ports.ARPortNumber;
import com.io7m.aradine.api.ports.ARPortType;
import com.io7m.aradine.database.api.ARDBException;
import com.io7m.aradine.ensemble.internal.database.AREnsDB;
import com.io7m.aradine.ensemble.internal.database.AREnsQPortConnectType;
import com.io7m.aradine.ensemble.internal.database.AREnsQPortConnectionListType;
import com.io7m.aradine.ensemble.internal.database.AREnsQPortDisconnectType;
import com.io7m.aradine.ensemble.internal.database.AREnsQPortListType;
import com.io7m.aradine.ensemble.internal.database.AREnsQPortPutType;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class AREnsDBTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(AREnsDBTest.class);

  private Path directory;

  @BeforeEach
  public void setup()
    throws Exception
  {
    this.directory =
      Files.createTempDirectory("aradine");
  }

  @AfterEach
  public void tearDown()
    throws Exception
  {
    FileUtils.deleteDirectory(this.directory.toFile());
  }

  @Test
  public void testOpen()
    throws Exception
  {
    final var file =
      this.directory.resolve("test.aens");

    try (var _ = AREnsDB.createDatabase(file)) {
      // Do nothing.
    }
  }

  @Test
  public void testPortPutGetEnsemble()
    throws Exception
  {
    final var file =
      this.directory.resolve("test.aens");

    final var portsWritten =
      new ArrayList<ARPortType>();
    final var portsRead =
      new ArrayList<ARPortType>();

    int number = 0;
    for (final var kind : ARPortKind.values()) {
      for (final var direction : ARPortDirection.values()) {
        for (int index = 0; index < 3; ++index) {
          portsWritten.add(
            new ARPortEnsemble(
              new ARPortID(UUID.randomUUID()),
              kind,
              direction,
              new ARPortNumber(number),
              "Label " + number + " " + index,
              Set.of("x","y","z")
            )
          );
          ++number;
        }
      }
    }
    assertEquals(12, portsWritten.size());
    portsWritten.sort(Comparator.comparing(o -> o.id().toString()));

    try (var db = AREnsDB.createDatabase(file)) {
      try (var t = db.openTransaction()) {
        for (final var port : portsWritten) {
          t.execute(AREnsQPortPutType.class, port);
        }
        t.commit();
      }

      try (var t = db.openTransaction()) {
        AREnsQPortListType.Parameters parameters =
          new AREnsQPortListType.Parameters(
          Optional.empty(),
          5
        );

        while (true) {
          final var r = t.execute(AREnsQPortListType.class, parameters);
          if (r.isEmpty()) {
            break;
          }
          portsRead.addAll(r);
          parameters = new AREnsQPortListType.Parameters(
            Optional.of(r.getLast()),
            5
          );
        }
      }
    }

    assertEquals(portsWritten.size(), portsRead.size());

    for (int index = 0; index < portsWritten.size(); ++index) {
      final var portWrote =
        portsWritten.get(index);
      final var portRead =
        portsRead.get(index);

      LOG.debug("[{}] {} ?= {}", index, portWrote, portRead);
    }

    for (int index = 0; index < portsWritten.size(); ++index) {
      final var portWrote =
        portsWritten.get(index);
      final var portRead =
        portsRead.get(index);

      final int finalIndex = index;
      assertEquals(
        portWrote,
        portRead,
        () -> {
          return String.format("[%d] %s = %s", finalIndex, portWrote, portRead);
        }
      );
    }

    assertEquals(portsWritten, portsRead);
  }

  @Test
  public void testPortConnectDisconnect()
    throws Exception
  {
    final var file =
      this.directory.resolve("test.aens");

    final var port0 =
      new ARPortEnsemble(
        ARPortID.ofString("4c3100c1-e37a-4253-a60f-9d6c04a6bfc3"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_SOURCE,
        new ARPortNumber(0),
        "AudioIn",
        Set.of()
      );

    final var port1 =
      new ARPortEnsemble(
        ARPortID.ofString("e4a68329-5935-4193-9837-150adfba6381"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_TARGET,
        new ARPortNumber(1),
        "AudioOut",
        Set.of()
      );

    try (var db = AREnsDB.createDatabase(file)) {
      try (var t = db.openTransaction()) {
        t.execute(AREnsQPortPutType.class, port0);
        t.execute(AREnsQPortPutType.class, port1);
        t.commit();
      }

      try (var t = db.openTransaction()) {
        t.execute(
          AREnsQPortConnectType.class,
          new ARPortConnection(port0.id(), port1.id())
        );
        t.commit();
      }

      try (var t = db.openTransaction()) {
        final var conns = 
          t.execute(
          AREnsQPortConnectionListType.class,
          new AREnsQPortConnectionListType.Parameters(Optional.empty(), 100)
        );
        assertEquals(
          List.of(
            new ARPortConnection(port0.id(), port1.id())
          ),
          conns
        );
      }

      try (var t = db.openTransaction()) {
        t.execute(
          AREnsQPortDisconnectType.class,
          new ARPortConnection(port0.id(), port1.id())
        );
        t.execute(
          AREnsQPortDisconnectType.class,
          new ARPortConnection(port0.id(), port1.id())
        );
        t.commit();

        final var conns =
          t.execute(
            AREnsQPortConnectionListType.class,
            new AREnsQPortConnectionListType.Parameters(Optional.empty(), 100)
          );

        assertEquals(
          List.of(),
          conns
        );
      }
    }
  }

  @Test
  public void testPortConnectBadSource()
    throws Exception
  {
    final var file =
      this.directory.resolve("test.aens");

    final var port0 =
      new ARPortEnsemble(
        ARPortID.ofString("4c3100c1-e37a-4253-a60f-9d6c04a6bfc3"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_SOURCE,
        new ARPortNumber(0),
        "AudioIn",
        Set.of()
      );

    final var port1 =
      new ARPortEnsemble(
        ARPortID.ofString("e4a68329-5935-4193-9837-150adfba6381"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_SOURCE,
        new ARPortNumber(1),
        "AudioOut",
        Set.of()
      );

    try (var db = AREnsDB.createDatabase(file)) {
      try (var t = db.openTransaction()) {
        t.execute(AREnsQPortPutType.class, port0);
        t.execute(AREnsQPortPutType.class, port1);
        t.commit();
      }

      try (var t = db.openTransaction()) {
        final var ex = assertThrows(ARDBException.class, () -> {
          t.execute(
            AREnsQPortConnectType.class,
            new ARPortConnection(port1.id(), port0.id())
          );
        });
        assertEquals("error-target-port-target", ex.errorCode());
      }
    }
  }

  @Test
  public void testPortConnectBadTarget()
    throws Exception
  {
    final var file =
      this.directory.resolve("test.aens");

    final var port0 =
      new ARPortEnsemble(
        ARPortID.ofString("4c3100c1-e37a-4253-a60f-9d6c04a6bfc3"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_TARGET,
        new ARPortNumber(0),
        "AudioIn",
        Set.of()
      );

    final var port1 =
      new ARPortEnsemble(
        ARPortID.ofString("e4a68329-5935-4193-9837-150adfba6381"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_TARGET,
        new ARPortNumber(1),
        "AudioOut",
        Set.of()
      );

    try (var db = AREnsDB.createDatabase(file)) {
      try (var t = db.openTransaction()) {
        t.execute(AREnsQPortPutType.class, port0);
        t.execute(AREnsQPortPutType.class, port1);
        t.commit();
      }

      try (var t = db.openTransaction()) {
        final var ex = assertThrows(ARDBException.class, () -> {
          t.execute(
            AREnsQPortConnectType.class,
            new ARPortConnection(port1.id(), port0.id())
          );
        });
        assertEquals("error-source-port-source", ex.errorCode());
      }
    }
  }

  @Test
  public void testPortConnectDuplicateTarget()
    throws Exception
  {
    final var file =
      this.directory.resolve("test.aens");

    final var port0 =
      new ARPortEnsemble(
        ARPortID.ofString("4c3100c1-e37a-4253-a60f-9d6c04a6bfc3"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_SOURCE,
        new ARPortNumber(0),
        "AudioIn",
        Set.of()
      );

    final var port1 =
      new ARPortEnsemble(
        ARPortID.ofString("e4a68329-5935-4193-9837-150adfba6381"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_TARGET,
        new ARPortNumber(1),
        "AudioOut",
        Set.of()
      );

    final var port2 =
      new ARPortEnsemble(
        ARPortID.ofString("019f798d-a28b-4795-94d8-874d0d5ec069"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_SOURCE,
        new ARPortNumber(2),
        "AudioIn2",
        Set.of()
      );

    try (var db = AREnsDB.createDatabase(file)) {
      try (var t = db.openTransaction()) {
        t.execute(AREnsQPortPutType.class, port0);
        t.execute(AREnsQPortPutType.class, port1);
        t.execute(AREnsQPortPutType.class, port2);
        t.commit();
      }

      try (var t = db.openTransaction()) {
        t.execute(
          AREnsQPortConnectType.class,
          new ARPortConnection(port0.id(), port1.id())
        );
        t.commit();

        final var ex = assertThrows(ARDBException.class, () -> {
          t.execute(
            AREnsQPortConnectType.class,
            new ARPortConnection(port2.id(), port1.id())
          );
        });
        assertEquals("error-target-port-connected", ex.errorCode());
      }
    }
  }
}
