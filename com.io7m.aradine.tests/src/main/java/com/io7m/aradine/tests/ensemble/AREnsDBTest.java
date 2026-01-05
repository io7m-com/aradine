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

import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.instrument.ARInstrumentID;
import com.io7m.aradine.api.instrument.ARInstrumentInstanceID;
import com.io7m.aradine.api.instrument.ARInstrumentReference;
import com.io7m.aradine.api.ports.ARPort;
import com.io7m.aradine.api.ports.ARPortConnection;
import com.io7m.aradine.api.ports.ARPortDirection;
import com.io7m.aradine.api.ports.ARPortID;
import com.io7m.aradine.api.ports.ARPortKind;
import com.io7m.aradine.api.ports.ARPortNumber;
import com.io7m.aradine.ensemble.internal.database.AREnsDB;
import com.io7m.aradine.ensemble.internal.database.AREnsQCommandIDNextType;
import com.io7m.aradine.ensemble.internal.database.AREnsQInstrumentPutType;
import com.io7m.aradine.ensemble.internal.database.AREnsQPortConnectType;
import com.io7m.aradine.ensemble.internal.database.AREnsQPortConnectionListType;
import com.io7m.aradine.ensemble.internal.database.AREnsQPortDisconnectType;
import com.io7m.aradine.ensemble.internal.database.AREnsQPortListType;
import com.io7m.aradine.ensemble.internal.database.AREnsQPortPutType;
import com.io7m.aradine.ensemble.internal.database.AREnsQRedoClearType;
import com.io7m.aradine.ensemble.internal.database.AREnsQRedoListType;
import com.io7m.aradine.ensemble.internal.database.AREnsQRedoPeekType;
import com.io7m.aradine.ensemble.internal.database.AREnsQRedoPopType;
import com.io7m.aradine.ensemble.internal.database.AREnsQRedoPushType;
import com.io7m.aradine.ensemble.internal.database.AREnsQUndoClearType;
import com.io7m.aradine.ensemble.internal.database.AREnsQUndoListType;
import com.io7m.aradine.ensemble.internal.database.AREnsQUndoPeekType;
import com.io7m.aradine.ensemble.internal.database.AREnsQUndoPopType;
import com.io7m.aradine.ensemble.internal.database.AREnsQUndoPushType;
import com.io7m.aradine.ensemble.internal.model.AREnsModelCommandRecord;
import com.io7m.lanark.core.RDottedName;
import com.io7m.verona.core.Version;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.io7m.aradine.database.api.ARDBUnit.UNIT;
import static com.io7m.aradine.ensemble.internal.v1.commands.AREnsModelCommandStateUnused.UNUSED;
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
  public void testOpenGarbage()
    throws Exception
  {
    final var file =
      this.directory.resolve("test.aens");

    final var rng = SecureRandom.getInstanceStrong();
    final var data = new byte[65536];
    rng.nextBytes(data);
    Files.write(file, data);

    final var ex = assertThrows(
      ARException.class, () -> {
        AREnsDB.createDatabase(file);
      });
    assertEquals("error-file-not-database", ex.errorCode());
  }

  @Test
  public void testPortPutGet()
    throws Exception
  {
    final var file =
      this.directory.resolve("test.aens");

    final var sourceId =
      ARInstrumentInstanceID.random();
    final var targetId =
      ARInstrumentInstanceID.random();

    final var portsWritten =
      new ArrayList<ARPort>();
    final var portsRead =
      new ArrayList<ARPort>();

    {
      var number = 0;
      for (final var kind : ARPortKind.values()) {
        for (var index = 0; index < 3; ++index) {
          portsWritten.add(
            new ARPort(
              sourceId,
              new ARPortID(UUID.randomUUID()),
              kind,
              ARPortDirection.AR_SOURCE,
              new ARPortNumber(number),
              "Label " + number + " " + index,
              Set.of("x", "y", "z")
            )
          );
          ++number;
        }
      }
    }

    {
      var number = 0;
      for (final var kind : ARPortKind.values()) {
        for (var index = 0; index < 3; ++index) {
          portsWritten.add(
            new ARPort(
              targetId,
              new ARPortID(UUID.randomUUID()),
              kind,
              ARPortDirection.AR_TARGET,
              new ARPortNumber(number),
              "Label " + number + " " + index,
              Set.of("x", "y", "z")
            )
          );
          ++number;
        }
      }
    }

    assertEquals(12, portsWritten.size());
    portsWritten.sort(Comparator.comparing(o -> o.id().toString()));

    final var instrument0 =
      new ARInstrumentReference(
        sourceId,
        new ARInstrumentID(
          new RDottedName("com.io7m.aradine"),
          new RDottedName("com.io7m.aradine.ensemble_source"),
          Version.of(1, 0, 0)
        )
      );

    final var instrument1 =
      new ARInstrumentReference(
        targetId,
        new ARInstrumentID(
          new RDottedName("com.io7m.aradine"),
          new RDottedName("com.io7m.aradine.ensemble_target"),
          Version.of(1, 0, 0)
        )
      );

    try (var db = AREnsDB.createDatabase(file)) {
      try (var t = db.openTransaction()) {
        t.execute(AREnsQInstrumentPutType.class, instrument0);
        t.execute(AREnsQInstrumentPutType.class, instrument1);
        for (final var port : portsWritten) {
          t.execute(AREnsQPortPutType.class, port);
        }
        t.commit();
      }

      try (var t = db.openTransaction()) {
        var parameters =
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

    for (var index = 0; index < portsWritten.size(); ++index) {
      final var portWrote =
        portsWritten.get(index);
      final var portRead =
        portsRead.get(index);

      LOG.debug("[{}] {} ?= {}", Integer.valueOf(index), portWrote, portRead);
    }

    for (var index = 0; index < portsWritten.size(); ++index) {
      final var portWrote =
        portsWritten.get(index);
      final var portRead =
        portsRead.get(index);

      final var finalIndex = index;
      assertEquals(
        portWrote,
        portRead,
        () -> {
          return String.format(
            "[%d] %s = %s",
            Integer.valueOf(finalIndex), portWrote, portRead);
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

    final var sourceId =
      ARInstrumentInstanceID.random();
    final var targetId =
      ARInstrumentInstanceID.random();

    final var instrument0 =
      new ARInstrumentReference(
        sourceId,
        new ARInstrumentID(
          new RDottedName("com.io7m.aradine"),
          new RDottedName("com.io7m.aradine.ensemble_source"),
          Version.of(1, 0, 0)
        )
      );

    final var instrument1 =
      new ARInstrumentReference(
        targetId,
        new ARInstrumentID(
          new RDottedName("com.io7m.aradine"),
          new RDottedName("com.io7m.aradine.ensemble_target"),
          Version.of(1, 0, 0)
        )
      );

    final var port0 =
      new ARPort(
        sourceId,
        ARPortID.ofString("4c3100c1-e37a-4253-a60f-9d6c04a6bfc3"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_SOURCE,
        new ARPortNumber(0),
        "AudioIn",
        Set.of()
      );

    final var port1 =
      new ARPort(
        targetId,
        ARPortID.ofString("e4a68329-5935-4193-9837-150adfba6381"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_TARGET,
        new ARPortNumber(1),
        "AudioOut",
        Set.of()
      );

    try (var db = AREnsDB.createDatabase(file)) {
      try (var t = db.openTransaction()) {
        t.execute(AREnsQInstrumentPutType.class, instrument0);
        t.execute(AREnsQInstrumentPutType.class, instrument1);
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

    final var sourceId =
      ARInstrumentInstanceID.random();
    final var targetId =
      ARInstrumentInstanceID.random();

    final var instrument0 =
      new ARInstrumentReference(
        sourceId,
        new ARInstrumentID(
          new RDottedName("com.io7m.aradine"),
          new RDottedName("com.io7m.aradine.ensemble_source"),
          Version.of(1, 0, 0)
        )
      );

    final var instrument1 =
      new ARInstrumentReference(
        targetId,
        new ARInstrumentID(
          new RDottedName("com.io7m.aradine"),
          new RDottedName("com.io7m.aradine.ensemble_target"),
          Version.of(1, 0, 0)
        )
      );

    final var port0 =
      new ARPort(
        sourceId,
        ARPortID.ofString("4c3100c1-e37a-4253-a60f-9d6c04a6bfc3"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_SOURCE,
        new ARPortNumber(0),
        "AudioIn",
        Set.of()
      );

    final var port1 =
      new ARPort(
        targetId,
        ARPortID.ofString("e4a68329-5935-4193-9837-150adfba6381"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_SOURCE,
        new ARPortNumber(1),
        "AudioOut",
        Set.of()
      );

    try (var db = AREnsDB.createDatabase(file)) {
      try (var t = db.openTransaction()) {
        t.execute(AREnsQInstrumentPutType.class, instrument0);
        t.execute(AREnsQInstrumentPutType.class, instrument1);
        t.execute(AREnsQPortPutType.class, port0);
        t.execute(AREnsQPortPutType.class, port1);
        t.commit();
      }

      try (var t = db.openTransaction()) {
        final var ex = assertThrows(
          ARException.class, () -> {
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

    final var sourceId =
      ARInstrumentInstanceID.random();
    final var targetId =
      ARInstrumentInstanceID.random();

    final var instrument0 =
      new ARInstrumentReference(
        sourceId,
        new ARInstrumentID(
          new RDottedName("com.io7m.aradine"),
          new RDottedName("com.io7m.aradine.ensemble_source"),
          Version.of(1, 0, 0)
        )
      );

    final var instrument1 =
      new ARInstrumentReference(
        targetId,
        new ARInstrumentID(
          new RDottedName("com.io7m.aradine"),
          new RDottedName("com.io7m.aradine.ensemble_target"),
          Version.of(1, 0, 0)
        )
      );

    final var port0 =
      new ARPort(
        sourceId,
        ARPortID.ofString("4c3100c1-e37a-4253-a60f-9d6c04a6bfc3"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_TARGET,
        new ARPortNumber(0),
        "AudioIn",
        Set.of()
      );

    final var port1 =
      new ARPort(
        targetId,
        ARPortID.ofString("e4a68329-5935-4193-9837-150adfba6381"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_TARGET,
        new ARPortNumber(1),
        "AudioOut",
        Set.of()
      );

    try (var db = AREnsDB.createDatabase(file)) {
      try (var t = db.openTransaction()) {
        t.execute(AREnsQInstrumentPutType.class, instrument0);
        t.execute(AREnsQInstrumentPutType.class, instrument1);
        t.execute(AREnsQPortPutType.class, port0);
        t.execute(AREnsQPortPutType.class, port1);
        t.commit();
      }

      try (var t = db.openTransaction()) {
        final var ex = assertThrows(
          ARException.class, () -> {
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

    final var sourceId =
      ARInstrumentInstanceID.random();
    final var targetId =
      ARInstrumentInstanceID.random();

    final var instrument0 =
      new ARInstrumentReference(
        sourceId,
        new ARInstrumentID(
          new RDottedName("com.io7m.aradine"),
          new RDottedName("com.io7m.aradine.ensemble_source"),
          Version.of(1, 0, 0)
        )
      );

    final var instrument1 =
      new ARInstrumentReference(
        targetId,
        new ARInstrumentID(
          new RDottedName("com.io7m.aradine"),
          new RDottedName("com.io7m.aradine.ensemble_target"),
          Version.of(1, 0, 0)
        )
      );

    final var port0 =
      new ARPort(
        sourceId,
        ARPortID.ofString("4c3100c1-e37a-4253-a60f-9d6c04a6bfc3"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_SOURCE,
        new ARPortNumber(0),
        "AudioSource0",
        Set.of()
      );

    final var port1 =
      new ARPort(
        sourceId,
        ARPortID.ofString("e4a68329-5935-4193-9837-150adfba6381"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_SOURCE,
        new ARPortNumber(1),
        "AudioSource1",
        Set.of()
      );

    final var port2 =
      new ARPort(
        targetId,
        ARPortID.ofString("019f798d-a28b-4795-94d8-874d0d5ec069"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_TARGET,
        new ARPortNumber(2),
        "AudioIn2",
        Set.of()
      );

    try (var db = AREnsDB.createDatabase(file)) {
      try (var t = db.openTransaction()) {
        t.execute(AREnsQInstrumentPutType.class, instrument0);
        t.execute(AREnsQInstrumentPutType.class, instrument1);
        t.execute(AREnsQPortPutType.class, port0);
        t.execute(AREnsQPortPutType.class, port1);
        t.execute(AREnsQPortPutType.class, port2);
        t.commit();
      }

      try (var t = db.openTransaction()) {
        t.execute(
          AREnsQPortConnectType.class,
          new ARPortConnection(port0.id(), port2.id())
        );
        t.commit();

        final var ex = assertThrows(
          ARException.class, () -> {
            t.execute(
              AREnsQPortConnectType.class,
              new ARPortConnection(port1.id(), port2.id())
            );
          });
        assertEquals("error-target-port-connected", ex.errorCode());
      }
    }
  }

  @Test
  public void testPortConnectSelfInstrument()
    throws Exception
  {
    final var file =
      this.directory.resolve("test.aens");

    final var sourceId =
      ARInstrumentInstanceID.random();

    final var instrument0 =
      new ARInstrumentReference(
        sourceId,
        new ARInstrumentID(
          new RDottedName("com.io7m.aradine"),
          new RDottedName("com.io7m.aradine.ensemble_source"),
          Version.of(1, 0, 0)
        )
      );

    final var port0 =
      new ARPort(
        sourceId,
        ARPortID.ofString("4c3100c1-e37a-4253-a60f-9d6c04a6bfc3"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_SOURCE,
        new ARPortNumber(0),
        "AudioSource0",
        Set.of()
      );

    final var port1 =
      new ARPort(
        sourceId,
        ARPortID.ofString("019f798d-a28b-4795-94d8-874d0d5ec069"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_TARGET,
        new ARPortNumber(1),
        "AudioTarget1",
        Set.of()
      );

    try (var db = AREnsDB.createDatabase(file)) {
      try (var t = db.openTransaction()) {
        t.execute(AREnsQInstrumentPutType.class, instrument0);
        t.execute(AREnsQPortPutType.class, port0);
        t.execute(AREnsQPortPutType.class, port1);
        t.commit();
      }

      try (var t = db.openTransaction()) {
        final var ex = assertThrows(
          ARException.class, () -> {
            t.execute(
              AREnsQPortConnectType.class,
              new ARPortConnection(port0.id(), port1.id())
            );
          });
        assertEquals(
          "error-source-target-port-instrument-self",
          ex.errorCode());
      }
    }
  }

  @Test
  public void testPortConnectListLarge()
    throws Exception
  {
    final var file =
      this.directory.resolve("test.aens");

    final var sourceId =
      ARInstrumentInstanceID.random();
    final var targetId =
      ARInstrumentInstanceID.random();

    final var instrument0 =
      new ARInstrumentReference(
        sourceId,
        new ARInstrumentID(
          new RDottedName("com.io7m.aradine"),
          new RDottedName("com.io7m.aradine.ensemble_source"),
          Version.of(1, 0, 0)
        )
      );

    final var instrument1 =
      new ARInstrumentReference(
        targetId,
        new ARInstrumentID(
          new RDottedName("com.io7m.aradine"),
          new RDottedName("com.io7m.aradine.ensemble_target"),
          Version.of(1, 0, 0)
        )
      );

    final var portSources = new ArrayList<ARPort>();
    for (var index = 0; index < 100; ++index) {
      final var p = new ARPort(
        sourceId,
        new ARPortID(UUID.randomUUID()),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_SOURCE,
        new ARPortNumber(index),
        "AudioSource" + index,
        Set.of()
      );
      portSources.add(p);
    }

    final var portTargets = new ArrayList<ARPort>();
    for (var index = 0; index < 100; ++index) {
      final var p = new ARPort(
        targetId,
        new ARPortID(UUID.randomUUID()),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_TARGET,
        new ARPortNumber(index),
        "AudioTarget" + index,
        Set.of()
      );
      portTargets.add(p);
    }

    final var connectionsWritten =
      new HashSet<ARPortConnection>();
    final var connectionsRead =
      new HashSet<ARPortConnection>();

    try (var db = AREnsDB.createDatabase(file)) {
      try (var t = db.openTransaction()) {
        t.execute(AREnsQInstrumentPutType.class, instrument0);
        t.execute(AREnsQInstrumentPutType.class, instrument1);
        for (final var p : portSources) {
          t.execute(AREnsQPortPutType.class, p);
        }
        for (final var p : portTargets) {
          t.execute(AREnsQPortPutType.class, p);
        }
        t.commit();
      }

      try (var t = db.openTransaction()) {
        for (var index = 0; index < portSources.size(); ++index) {
          final var pSource =
            portSources.get(index);
          final var pTarget =
            portTargets.get(index);
          final var connection =
            new ARPortConnection(pSource.id(), pTarget.id());

          connectionsWritten.add(connection);
          t.execute(AREnsQPortConnectType.class, connection);
        }
        t.commit();
      }

      try (var t = db.openTransaction()) {
        var parameters =
          new AREnsQPortConnectionListType.Parameters(
            Optional.empty(),
            17
          );

        while (true) {
          final var conns =
            t.execute(AREnsQPortConnectionListType.class, parameters);
          if (conns.isEmpty()) {
            break;
          }
          parameters = new AREnsQPortConnectionListType.Parameters(
            Optional.of(conns.getLast()),
            17
          );
          connectionsRead.addAll(conns);
        }

        assertEquals(connectionsWritten, connectionsRead);
      }
    }
  }

  @Test
  public void testUndoPushPop()
    throws Exception
  {
    final var file =
      this.directory.resolve("test.aens");

    final var cmd0 =
      new AREnsModelCommandRecord(
        0L,
        OffsetDateTime.parse("2000-01-01T00:00:00+00:00"),
        "Command 0",
        "C",
        UNUSED
      );

    final var cmd1 =
      new AREnsModelCommandRecord(
        1L,
        OffsetDateTime.parse("2000-01-01T00:00:01+00:00"),
        "Command 1",
        "C",
        UNUSED
      );

    final var cmd2 =
      new AREnsModelCommandRecord(
        2L,
        OffsetDateTime.parse("2000-01-01T00:00:02+00:00"),
        "Command 2",
        "C",
        UNUSED
      );

    try (var db = AREnsDB.createDatabase(file)) {
      try (var t = db.openTransaction()) {
        t.execute(AREnsQUndoPushType.class, cmd0);
        assertEquals(
          Optional.of(cmd0),
          t.execute(AREnsQUndoPeekType.class, UNIT)
        );
        t.execute(AREnsQUndoPushType.class, cmd1);
        assertEquals(
          Optional.of(cmd1),
          t.execute(AREnsQUndoPeekType.class, UNIT)
        );
        t.execute(AREnsQUndoPushType.class, cmd2);
        assertEquals(
          Optional.of(cmd2),
          t.execute(AREnsQUndoPeekType.class, UNIT)
        );

        assertEquals(
          Optional.of(cmd2),
          t.execute(AREnsQUndoPopType.class, UNIT)
        );
        assertEquals(
          Optional.of(cmd1),
          t.execute(AREnsQUndoPopType.class, UNIT)
        );
        assertEquals(
          Optional.of(cmd0),
          t.execute(AREnsQUndoPopType.class, UNIT)
        );
        assertEquals(
          Optional.empty(),
          t.execute(AREnsQUndoPopType.class, UNIT)
        );
      }
    }
  }

  @Test
  public void testRedoPushPop()
    throws Exception
  {
    final var file =
      this.directory.resolve("test.aens");

    final var cmd0 =
      new AREnsModelCommandRecord(
        0L,
        OffsetDateTime.parse("2000-01-01T00:00:00+00:00"),
        "Command 0",
        "C",
        UNUSED
      );

    final var cmd1 =
      new AREnsModelCommandRecord(
        1L,
        OffsetDateTime.parse("2000-01-01T00:00:01+00:00"),
        "Command 1",
        "C",
        UNUSED
      );

    final var cmd2 =
      new AREnsModelCommandRecord(
        2L,
        OffsetDateTime.parse("2000-01-01T00:00:02+00:00"),
        "Command 2",
        "C",
        UNUSED
      );

    try (var db = AREnsDB.createDatabase(file)) {
      try (var t = db.openTransaction()) {
        t.execute(AREnsQRedoPushType.class, cmd0);
        assertEquals(
          Optional.of(cmd0),
          t.execute(AREnsQRedoPeekType.class, UNIT)
        );
        t.execute(AREnsQRedoPushType.class, cmd1);
        assertEquals(
          Optional.of(cmd1),
          t.execute(AREnsQRedoPeekType.class, UNIT)
        );
        t.execute(AREnsQRedoPushType.class, cmd2);
        assertEquals(
          Optional.of(cmd2),
          t.execute(AREnsQRedoPeekType.class, UNIT)
        );

        assertEquals(
          Optional.of(cmd2),
          t.execute(AREnsQRedoPopType.class, UNIT)
        );
        assertEquals(
          Optional.of(cmd1),
          t.execute(AREnsQRedoPopType.class, UNIT)
        );
        assertEquals(
          Optional.of(cmd0),
          t.execute(AREnsQRedoPopType.class, UNIT)
        );
        assertEquals(
          Optional.empty(),
          t.execute(AREnsQRedoPopType.class, UNIT)
        );
      }
    }
  }

  @Test
  public void testUndoList()
    throws Exception
  {
    final var file =
      this.directory.resolve("test.aens");

    final var commandsRead =
      new ArrayList<AREnsModelCommandRecord>();
    final var commandsWritten =
      new ArrayList<AREnsModelCommandRecord>();

    for (var index = 1; index <= 13; ++index) {
      commandsWritten.add(
        new AREnsModelCommandRecord(
          index,
          OffsetDateTime.parse("2000-01-01T00:00:00+00:00").plusSeconds(index),
          "Command " + index,
          "C",
          UNUSED
        )
      );
    }

    try (var db = AREnsDB.createDatabase(file)) {
      try (var t = db.openTransaction()) {
        for (final var cmd : commandsWritten) {
          final var expected = t.execute(AREnsQCommandIDNextType.class, UNIT);
          t.execute(AREnsQUndoPushType.class, cmd);
          assertEquals(expected, cmd.id());
        }
        t.commit();

        var parameters =
          new AREnsQUndoListType.Parameters(Optional.empty(), 5);

        while (true) {
          final var r = t.execute(AREnsQUndoListType.class, parameters);
          if (r.isEmpty()) {
            break;
          }
          commandsRead.addAll(r);
          parameters =
            new AREnsQUndoListType.Parameters(
              Optional.of(Long.valueOf(r.getLast().id())),
              5
            );
        }
        assertEquals(commandsWritten, commandsRead);
      }

      try (var t = db.openTransaction()) {
        assertEquals(
          13,
          t.execute(
            AREnsQUndoListType.class,
            new AREnsQUndoListType.Parameters(
              Optional.empty(),
              Integer.MAX_VALUE)
          ).size()
        );

        t.execute(AREnsQUndoClearType.class, UNIT);
        t.commit();

        assertEquals(
          0,
          t.execute(
            AREnsQUndoListType.class,
            new AREnsQUndoListType.Parameters(
              Optional.empty(),
              Integer.MAX_VALUE)
          ).size()
        );
      }
    }
  }

  @Test
  public void testRedoList()
    throws Exception
  {
    final var file =
      this.directory.resolve("test.aens");

    final var commandsRead =
      new ArrayList<AREnsModelCommandRecord>();
    final var commandsWritten =
      new ArrayList<AREnsModelCommandRecord>();

    for (var index = 1; index <= 13; ++index) {
      commandsWritten.add(
        new AREnsModelCommandRecord(
          index,
          OffsetDateTime.parse("2000-01-01T00:00:00+00:00").plusSeconds(index),
          "Command " + index,
          "C",
          UNUSED
        )
      );
    }

    try (var db = AREnsDB.createDatabase(file)) {
      try (var t = db.openTransaction()) {
        for (final var cmd : commandsWritten) {
          final var expected = t.execute(AREnsQCommandIDNextType.class, UNIT);
          t.execute(AREnsQRedoPushType.class, cmd);
          assertEquals(expected, cmd.id());
        }
        t.commit();

        var parameters =
          new AREnsQRedoListType.Parameters(Optional.empty(), 5);

        while (true) {
          final var r = t.execute(AREnsQRedoListType.class, parameters);
          if (r.isEmpty()) {
            break;
          }
          commandsRead.addAll(r);
          parameters =
            new AREnsQRedoListType.Parameters(
              Optional.of(Long.valueOf(r.getLast().id())),
              5
            );
        }
        assertEquals(commandsWritten, commandsRead);
      }

      try (var t = db.openTransaction()) {
        assertEquals(
          13,
          t.execute(
            AREnsQRedoListType.class,
            new AREnsQRedoListType.Parameters(
              Optional.empty(),
              Integer.MAX_VALUE)
          ).size()
        );

        t.execute(AREnsQRedoClearType.class, UNIT);
        t.commit();

        assertEquals(
          0,
          t.execute(
            AREnsQRedoListType.class,
            new AREnsQRedoListType.Parameters(
              Optional.empty(),
              Integer.MAX_VALUE)
          ).size()
        );
      }
    }
  }

  @Test
  public void testUndoPeekCorrupt0()
    throws Exception
  {
    final var file =
      this.directory.resolve("test.aens");

    final var corrupt = """
        INSERT INTO undo VALUES ($1,$2,$3,$4,$5)
      """;

    try (var db = AREnsDB.createDatabase(file)) {
      try (var t = db.openTransaction()) {
        final var c = t.connection().connection();
        try (var st = c.prepareStatement(corrupt)) {
          st.setLong(1, 0L);
          st.setString(2, "Corrupted!");
          st.setLong(3, 0L);
          st.setString(4, "Unrecognized");
          st.setBytes(5, new byte[3]);
          st.execute();
        }

        final var ex =
          assertThrows(
            ARException.class, () -> {
              t.execute(AREnsQUndoPeekType.class, UNIT);
            });
        assertEquals("error-json-parse-exception", ex.errorCode());
      }
    }
  }

  @Test
  public void testUndoPeekCorrupt1()
    throws Exception
  {
    final var file =
      this.directory.resolve("test.aens");

    final var corrupt = """
        INSERT INTO undo VALUES ($1,$2,$3,$4,$5)
      """;

    try (var db = AREnsDB.createDatabase(file)) {
      try (var t = db.openTransaction()) {
        final var c = t.connection().connection();
        try (var st = c.prepareStatement(corrupt)) {
          st.setLong(1, 0L);
          st.setString(2, "Corrupted!");
          st.setLong(3, 0L);
          st.setString(4, "AREnsModelCommandRecord");
          st.setBytes(5, new byte[3]);
          st.execute();
        }

        final var ex =
          assertThrows(
            ARException.class, () -> {
              t.execute(AREnsQUndoPeekType.class, UNIT);
            });
        assertEquals("error-json-parse-exception", ex.errorCode());
      }
    }
  }

  @Test
  public void testRedoPeekCorrupt0()
    throws Exception
  {
    final var file =
      this.directory.resolve("test.aens");

    final var corrupt = """
        INSERT INTO redo VALUES ($1,$2,$3,$4,$5)
      """;

    try (var db = AREnsDB.createDatabase(file)) {
      try (var t = db.openTransaction()) {
        final var c = t.connection().connection();
        try (var st = c.prepareStatement(corrupt)) {
          st.setLong(1, 0L);
          st.setString(2, "Corrupted!");
          st.setLong(3, 0L);
          st.setString(4, "Unrecognized");
          st.setBytes(5, new byte[3]);
          st.execute();
        }

        final var ex =
          assertThrows(
            ARException.class, () -> {
              t.execute(AREnsQRedoPeekType.class, UNIT);
            });
        assertEquals("error-json-parse-exception", ex.errorCode());
      }
    }
  }

  @Test
  public void testRedoPeekCorrupt1()
    throws Exception
  {
    final var file =
      this.directory.resolve("test.aens");

    final var corrupt = """
        INSERT INTO redo VALUES ($1,$2,$3,$4,$5)
      """;

    try (var db = AREnsDB.createDatabase(file)) {
      try (var t = db.openTransaction()) {
        final var c = t.connection().connection();
        try (var st = c.prepareStatement(corrupt)) {
          st.setLong(1, 0L);
          st.setString(2, "Corrupted!");
          st.setLong(3, 0L);
          st.setString(4, "AREnsModelCommandRecord");
          st.setBytes(5, new byte[3]);
          st.execute();
        }

        final var ex =
          assertThrows(
            ARException.class, () -> {
              t.execute(AREnsQRedoPeekType.class, UNIT);
            });
        assertEquals("error-json-parse-exception", ex.errorCode());
      }
    }
  }
}
