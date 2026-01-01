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

package com.io7m.aradine.tests.ensemble;

import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.instrument.ARInstrumentID;
import com.io7m.aradine.api.instrument.ARInstrumentInstanceID;
import com.io7m.aradine.api.instrument.ARInstrumentReference;
import com.io7m.aradine.api.ports.ARPort;
import com.io7m.aradine.api.ports.ARPortDirection;
import com.io7m.aradine.api.ports.ARPortID;
import com.io7m.aradine.api.ports.ARPortKind;
import com.io7m.aradine.api.ports.ARPortNumber;
import com.io7m.aradine.ensemble.internal.model.AREnsGraph;
import com.io7m.lanark.core.RDottedName;
import com.io7m.verona.core.Version;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class AREnsGraphTest
{
  private static final ARInstrumentReference INSTRUMENT_0 =
    new ARInstrumentReference(
      ARInstrumentInstanceID.random(),
      new ARInstrumentID(
        new RDottedName("com.io7m.aradine"),
        new RDottedName("com.io7m.aradine.example0"),
        Version.of(1, 0, 0)
      )
    );

  private static final ARInstrumentReference INSTRUMENT_1 =
    new ARInstrumentReference(
      ARInstrumentInstanceID.random(),
      new ARInstrumentID(
        new RDottedName("com.io7m.aradine"),
        new RDottedName("com.io7m.aradine.example1"),
        Version.of(1, 0, 0)
      )
    );

  @Test
  public void testCreate()
  {
    final var graph = AREnsGraph.create();
    assertEquals(graph, graph.copy());
  }

  @Test
  public void testPortRegisterNonexistentInstrument()
  {
    final var graph = AREnsGraph.create();

    final var ex =
      Assertions.assertThrows(
        ARException.class, () -> {
          graph.portRegister(new ARPort(
            ARInstrumentInstanceID.random(),
            ARPortID.ofString("1ef64b40-cfac-456a-9a20-b168c8e579e9"),
            ARPortKind.AR_AUDIO,
            ARPortDirection.AR_SOURCE,
            new ARPortNumber(0),
            "Source0",
            Set.of()
          ));
        });

    assertEquals("error-instrument-nonexistent", ex.errorCode());
  }

  @Test
  public void testPortRegisterConnect()
    throws ARException
  {
    final var graph = AREnsGraph.create();

    final var port0 =
      new ARPort(
        INSTRUMENT_0.instanceID(),
        ARPortID.ofString("1ef64b40-cfac-456a-9a20-b168c8e579e9"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_SOURCE,
        new ARPortNumber(0),
        "Source0",
        Set.of()
      );

    final var port1 =
      new ARPort(
        INSTRUMENT_1.instanceID(),
        ARPortID.ofString("7f5ab52c-4cad-491d-844d-700b24ac4841"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_TARGET,
        new ARPortNumber(0),
        "Source1",
        Set.of()
      );

    graph.instrumentRegister(INSTRUMENT_0);
    assertTrue(graph.instrumentIsRegistered(INSTRUMENT_0.instanceID()));
    graph.instrumentRegister(INSTRUMENT_1);
    assertTrue(graph.instrumentIsRegistered(INSTRUMENT_1.instanceID()));

    graph.portRegister(port0);
    graph.portRegister(port1);
    graph.portConnect(port0.id(), port1.id());
  }

  @Test
  public void testPortRegisterConnectNotSource()
    throws ARException
  {
    final var graph = AREnsGraph.create();

    final var port0 =
      new ARPort(
        INSTRUMENT_0.instanceID(),
        ARPortID.ofString("1ef64b40-cfac-456a-9a20-b168c8e579e9"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_SOURCE,
        new ARPortNumber(0),
        "Source0",
        Set.of()
      );

    final var port1 =
      new ARPort(
        INSTRUMENT_1.instanceID(),
        ARPortID.ofString("7f5ab52c-4cad-491d-844d-700b24ac4841"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_TARGET,
        new ARPortNumber(0),
        "Source1",
        Set.of()
      );

    graph.instrumentRegister(INSTRUMENT_0);
    assertTrue(graph.instrumentIsRegistered(INSTRUMENT_0.instanceID()));
    graph.instrumentRegister(INSTRUMENT_1);
    assertTrue(graph.instrumentIsRegistered(INSTRUMENT_1.instanceID()));

    graph.portRegister(port0);
    graph.portRegister(port1);

    final var ex =
      Assertions.assertThrows(ARException.class, () -> {
        graph.portConnect(port1.id(), port0.id());
      });
    assertEquals("error-port-not-source", ex.errorCode());
  }

  @Test
  public void testPortRegisterConnectNotTarget()
    throws ARException
  {
    final var graph = AREnsGraph.create();

    final var port0 =
      new ARPort(
        INSTRUMENT_0.instanceID(),
        ARPortID.ofString("1ef64b40-cfac-456a-9a20-b168c8e579e9"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_SOURCE,
        new ARPortNumber(0),
        "Source0",
        Set.of()
      );

    final var port1 =
      new ARPort(
        INSTRUMENT_1.instanceID(),
        ARPortID.ofString("7f5ab52c-4cad-491d-844d-700b24ac4841"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_SOURCE,
        new ARPortNumber(0),
        "Source1",
        Set.of()
      );

    graph.instrumentRegister(INSTRUMENT_0);
    assertTrue(graph.instrumentIsRegistered(INSTRUMENT_0.instanceID()));
    graph.instrumentRegister(INSTRUMENT_1);
    assertTrue(graph.instrumentIsRegistered(INSTRUMENT_1.instanceID()));

    graph.portRegister(port0);
    graph.portRegister(port1);

    final var ex =
      Assertions.assertThrows(ARException.class, () -> {
        graph.portConnect(port1.id(), port0.id());
      });
    assertEquals("error-port-not-target", ex.errorCode());
  }

  @Test
  public void testPortRegisterConnectNonexistent()
    throws ARException
  {
    final var graph = AREnsGraph.create();

    final var port0 =
      new ARPort(
        INSTRUMENT_0.instanceID(),
        ARPortID.ofString("1ef64b40-cfac-456a-9a20-b168c8e579e9"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_SOURCE,
        new ARPortNumber(0),
        "Source0",
        Set.of()
      );

    final var port1 =
      new ARPort(
        INSTRUMENT_1.instanceID(),
        ARPortID.ofString("7f5ab52c-4cad-491d-844d-700b24ac4841"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_TARGET,
        new ARPortNumber(0),
        "Target1",
        Set.of()
      );

    graph.instrumentRegister(INSTRUMENT_0);
    assertTrue(graph.instrumentIsRegistered(INSTRUMENT_0.instanceID()));
    graph.instrumentRegister(INSTRUMENT_1);
    assertTrue(graph.instrumentIsRegistered(INSTRUMENT_1.instanceID()));

    graph.portRegister(port0);

    final var ex =
      Assertions.assertThrows(ARException.class, () -> {
        graph.portConnect(port0.id(), port1.id());
      });
    assertEquals("error-port-nonexistent", ex.errorCode());
  }

  @Test
  public void testPortRegisterConnectNotDifferentInstrument()
    throws ARException
  {
    final var graph = AREnsGraph.create();

    final var port0 =
      new ARPort(
        INSTRUMENT_0.instanceID(),
        ARPortID.ofString("1ef64b40-cfac-456a-9a20-b168c8e579e9"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_SOURCE,
        new ARPortNumber(0),
        "Source0",
        Set.of()
      );

    final var port1 =
      new ARPort(
        INSTRUMENT_0.instanceID(),
        ARPortID.ofString("7f5ab52c-4cad-491d-844d-700b24ac4841"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_TARGET,
        new ARPortNumber(0),
        "Source1",
        Set.of()
      );

    graph.instrumentRegister(INSTRUMENT_0);
    assertTrue(graph.instrumentIsRegistered(INSTRUMENT_0.instanceID()));
    graph.instrumentRegister(INSTRUMENT_1);
    assertTrue(graph.instrumentIsRegistered(INSTRUMENT_1.instanceID()));

    graph.portRegister(port0);
    graph.portRegister(port1);

    final var ex =
      Assertions.assertThrows(ARException.class, () -> {
        graph.portConnect(port0.id(), port1.id());
      });
    assertEquals("error-instruments-same", ex.errorCode());
  }

  @Test
  public void testPortRegisterDuplicate()
    throws ARException
  {
    final var graph = AREnsGraph.create();

    final var port0 =
      new ARPort(
        INSTRUMENT_0.instanceID(),
        ARPortID.ofString("1ef64b40-cfac-456a-9a20-b168c8e579e9"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_SOURCE,
        new ARPortNumber(0),
        "Source0",
        Set.of()
      );

    graph.instrumentRegister(INSTRUMENT_0);
    assertTrue(graph.instrumentIsRegistered(INSTRUMENT_0.instanceID()));
    graph.portRegister(port0);

    final var ex =
      Assertions.assertThrows(ARException.class, () -> {
        graph.portRegister(port0);
      });
    assertEquals("error-port-duplicate", ex.errorCode());
  }

  @Test
  public void testPortRegisterDeregisterConnected()
    throws ARException
  {
    final var graph = AREnsGraph.create();

    final var port0 =
      new ARPort(
        INSTRUMENT_0.instanceID(),
        ARPortID.ofString("1ef64b40-cfac-456a-9a20-b168c8e579e9"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_SOURCE,
        new ARPortNumber(0),
        "Source0",
        Set.of()
      );

    final var port1 =
      new ARPort(
        INSTRUMENT_1.instanceID(),
        ARPortID.ofString("7f5ab52c-4cad-491d-844d-700b24ac4841"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_TARGET,
        new ARPortNumber(0),
        "Source1",
        Set.of()
      );

    graph.instrumentRegister(INSTRUMENT_0);
    assertTrue(graph.instrumentIsRegistered(INSTRUMENT_0.instanceID()));
    graph.instrumentRegister(INSTRUMENT_1);
    assertTrue(graph.instrumentIsRegistered(INSTRUMENT_1.instanceID()));

    graph.portRegister(port0);
    graph.portRegister(port1);
    graph.portConnect(port0.id(), port1.id());

    final var ex =
      Assertions.assertThrows(ARException.class, () -> {
        graph.portDeregister(port0);
      });
    assertEquals("error-port-connected", ex.errorCode());
  }

  @Test
  public void testPortRegisterDeregister()
    throws ARException
  {
    final var graph = AREnsGraph.create();

    final var port0 =
      new ARPort(
        INSTRUMENT_0.instanceID(),
        ARPortID.ofString("1ef64b40-cfac-456a-9a20-b168c8e579e9"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_SOURCE,
        new ARPortNumber(0),
        "Source0",
        Set.of()
      );

    graph.instrumentRegister(INSTRUMENT_0);
    assertTrue(graph.instrumentIsRegistered(INSTRUMENT_0.instanceID()));

    graph.portRegister(port0);
    assertTrue(graph.portIsRegistered(port0.id()));
    graph.portDeregister(port0);
    assertFalse(graph.portIsRegistered(port0.id()));
    graph.portRegister(port0);
    assertTrue(graph.portIsRegistered(port0.id()));
    graph.portDeregister(port0);
    assertFalse(graph.portIsRegistered(port0.id()));
  }

  @Test
  public void testPortRegisterConnectAlreadyConnected()
    throws ARException
  {
    final var graph = AREnsGraph.create();

    final var port0 =
      new ARPort(
        INSTRUMENT_0.instanceID(),
        ARPortID.ofString("1ef64b40-cfac-456a-9a20-b168c8e579e9"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_SOURCE,
        new ARPortNumber(0),
        "Source0",
        Set.of()
      );

    final var port1 =
      new ARPort(
        INSTRUMENT_0.instanceID(),
        ARPortID.ofString("b61cbfbb-99bb-4527-9a19-3ad665733e7c"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_SOURCE,
        new ARPortNumber(1),
        "Source1",
        Set.of()
      );

    final var port2 =
      new ARPort(
        INSTRUMENT_1.instanceID(),
        ARPortID.ofString("7f5ab52c-4cad-491d-844d-700b24ac4841"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_TARGET,
        new ARPortNumber(0),
        "Target1",
        Set.of()
      );

    graph.instrumentRegister(INSTRUMENT_0);
    assertTrue(graph.instrumentIsRegistered(INSTRUMENT_0.instanceID()));
    graph.instrumentRegister(INSTRUMENT_1);
    assertTrue(graph.instrumentIsRegistered(INSTRUMENT_1.instanceID()));

    graph.portRegister(port0);
    graph.portRegister(port1);
    graph.portRegister(port2);

    graph.portConnect(port0.id(), port2.id());

    final var ex =
      Assertions.assertThrows(ARException.class, () -> {
        graph.portConnect(port1.id(), port2.id());
      });
    assertEquals("error-port-connected", ex.errorCode());
  }

  @Test
  public void testPortConnectDisconnect()
    throws ARException
  {
    final var graph = AREnsGraph.create();

    final var port0 =
      new ARPort(
        INSTRUMENT_0.instanceID(),
        ARPortID.ofString("1ef64b40-cfac-456a-9a20-b168c8e579e9"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_SOURCE,
        new ARPortNumber(0),
        "Source0",
        Set.of()
      );

    final var port1 =
      new ARPort(
        INSTRUMENT_1.instanceID(),
        ARPortID.ofString("7f5ab52c-4cad-491d-844d-700b24ac4841"),
        ARPortKind.AR_AUDIO,
        ARPortDirection.AR_TARGET,
        new ARPortNumber(0),
        "Source1",
        Set.of()
      );

    graph.instrumentRegister(INSTRUMENT_0);
    assertTrue(graph.instrumentIsRegistered(INSTRUMENT_0.instanceID()));
    graph.instrumentRegister(INSTRUMENT_1);
    assertTrue(graph.instrumentIsRegistered(INSTRUMENT_1.instanceID()));

    graph.portRegister(port0);
    graph.portRegister(port1);

    graph.portConnect(port0.id(), port1.id());
    assertTrue(graph.portIsConnected(port0.id(), port1.id()));
    assertFalse(graph.portIsConnected(port1.id(), port0.id()));

    graph.portDisconnect(port0.id(), port1.id());
    assertFalse(graph.portIsConnected(port0.id(), port1.id()));
    assertFalse(graph.portIsConnected(port1.id(), port0.id()));

    final var ex =
      Assertions.assertThrows(ARException.class, () -> {
        graph.portDisconnect(port0.id(), port1.id());
      });
    assertEquals("error-ports-not-connected", ex.errorCode());
  }
}
