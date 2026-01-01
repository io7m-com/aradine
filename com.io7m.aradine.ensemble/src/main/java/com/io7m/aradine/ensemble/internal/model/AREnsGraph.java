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

package com.io7m.aradine.ensemble.internal.model;

import com.io7m.aradine.api.ARException;
import com.io7m.aradine.api.instrument.ARInstrumentInstanceID;
import com.io7m.aradine.api.instrument.ARInstrumentReference;
import com.io7m.aradine.api.ports.ARInstrumentConnection;
import com.io7m.aradine.api.ports.ARPort;
import com.io7m.aradine.api.ports.ARPortConnection;
import com.io7m.aradine.api.ports.ARPortDirection;
import com.io7m.aradine.api.ports.ARPortID;
import com.io7m.jaffirm.core.Preconditions;
import org.jgrapht.graph.DirectedAcyclicGraph;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A port/instrument graph.
 */

public final class AREnsGraph implements AREnsGraphType
{
  private final DirectedAcyclicGraph<ARPortID, ARPortConnection> portGraph;
  private final DirectedAcyclicGraph<ARInstrumentInstanceID, ARInstrumentConnection> instrumentGraph;
  private final HashMap<ARPortID, ARPort> ports;
  private final HashMap<ARInstrumentInstanceID, ARInstrumentReference> instruments;

  private AREnsGraph(
    final DirectedAcyclicGraph<ARPortID, ARPortConnection> inPortGraph,
    final DirectedAcyclicGraph<ARInstrumentInstanceID, ARInstrumentConnection> inInstrumentGraph,
    final HashMap<ARPortID, ARPort> inPorts,
    final HashMap<ARInstrumentInstanceID, ARInstrumentReference> inInstruments)
  {
    this.portGraph =
      Objects.requireNonNull(inPortGraph, "PortGraph");
    this.instrumentGraph =
      Objects.requireNonNull(inInstrumentGraph, "InstrumentGraph");
    this.ports =
      Objects.requireNonNull(inPorts, "Ports");
    this.instruments =
      Objects.requireNonNull(inInstruments, "Instruments");
  }

  /**
   * @return A new empty graph
   */

  public static AREnsGraph create()
  {
    final var portGraph =
      new DirectedAcyclicGraph<ARPortID, ARPortConnection>(
        ARPortConnection.class
      );
    final var instrumentGraph =
      new DirectedAcyclicGraph<ARInstrumentInstanceID, ARInstrumentConnection>(
        ARInstrumentConnection.class
      );

    final var ports =
      new HashMap<ARPortID, ARPort>(4 * 16);
    final var instruments =
      new HashMap<ARInstrumentInstanceID, ARInstrumentReference>();

    return new AREnsGraph(portGraph, instrumentGraph, ports, instruments);
  }

  @Override
  public boolean equals(final Object o)
  {
    if (!(o instanceof final AREnsGraph that)) {
      return false;
    }
    return Objects.equals(this.portGraph, that.portGraph)
      && Objects.equals(this.instrumentGraph, that.instrumentGraph)
      && Objects.equals(this.ports, that.ports)
      && Objects.equals(this.instruments, that.instruments);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(
      this.portGraph,
      this.instrumentGraph,
      this.ports,
      this.instruments
    );
  }

  @Override
  public void instrumentRegister(
    final ARInstrumentReference instrument)
    throws ARException
  {
    Objects.requireNonNull(instrument, "Instrument");

    final var instanceID = instrument.instanceID();
    this.checkInstrumentNonexistent(instanceID);

    this.instruments.put(instanceID, instrument);
    this.instrumentGraph.addVertex(instanceID);
  }

  @Override
  public void portConnect(
    final ARPortID portSourceID,
    final ARPortID portTargetID)
    throws ARException
  {
    Objects.requireNonNull(portSourceID, "PortSource");
    Objects.requireNonNull(portTargetID, "PortTarget");

    final var portSource =
      this.checkPortExists(portSourceID);
    final var portTarget =
      this.checkPortExists(portTargetID);

    this.checkPortSource(portSource);
    this.checkPortTarget(portTarget);
    this.checkPortNotConnected(portTargetID);
    this.checkPortsDifferent(portSourceID, portTargetID);
    this.checkPortInstrumentsDifferent(portSourceID, portTargetID);

    this.portGraph.addEdge(
      portSourceID,
      portTargetID,
      new ARPortConnection(portSourceID, portTargetID)
    );
    this.instrumentGraph.addEdge(
      portSource.instrumentInstance(),
      portTarget.instrumentInstance(),
      new ARInstrumentConnection(
        portSource.instrumentInstance(),
        portSourceID,
        portTarget.instrumentInstance(),
        portTargetID
      )
    );
  }

  @Override
  public void portDisconnect(
    final ARPortID portSourceID,
    final ARPortID portTargetID)
    throws ARException
  {
    Objects.requireNonNull(portSourceID, "PortSource");
    Objects.requireNonNull(portTargetID, "PortTarget");

    final var portSource =
      this.checkPortExists(portSourceID);
    final var portTarget =
      this.checkPortExists(portTargetID);

    this.checkPortSource(portSource);
    this.checkPortTarget(portTarget);
    this.checkPortsConnected(portSourceID, portTargetID);

    final var portsEdge =
      new ARPortConnection(portSourceID, portTargetID);
    final var instrumentEdge =
      new ARInstrumentConnection(
        portSource.instrumentInstance(),
        portSourceID,
        portTarget.instrumentInstance(),
        portTargetID
      );

    Preconditions.checkPreconditionV(
      this.portGraph.containsEdge(portsEdge),
      "Port graph must contain edge %s",
      portsEdge
    );
    Preconditions.checkPreconditionV(
      this.instrumentGraph.containsEdge(instrumentEdge),
      "Instrument graph must contain edge %s",
      instrumentEdge
    );

    this.portGraph.removeEdge(portsEdge);
    this.instrumentGraph.removeEdge(instrumentEdge);
  }

  @Override
  public void portRegister(
    final ARPort port)
    throws ARException
  {
    Objects.requireNonNull(port, "Port");

    final var portID = port.id();
    this.checkInstrumentExists(port.instrumentInstance());
    this.checkPortNonexistent(portID);

    this.ports.put(portID, port);
    this.portGraph.addVertex(portID);
  }

  @Override
  public void portDeregister(
    final ARPort port)
    throws ARException
  {
    Objects.requireNonNull(port, "Port");

    final var portID = port.id();
    this.checkInstrumentExists(port.instrumentInstance());
    this.checkPortExists(portID);
    this.checkPortNotConnected(portID);

    this.ports.remove(portID);
    this.portGraph.removeVertex(portID);
  }

  private void checkPortNotConnected(
    final ARPortID portID)
    throws ARException
  {
    final var connections = this.portGraph.degreeOf(portID);
    if (connections != 0) {
      throw new ARException(
        "Port still connected.",
        "error-port-connected",
        Map.of("Port", portID.toString()),
        Optional.empty()
      );
    }
  }

  private void checkPortsConnected(
    final ARPortID portSourceID,
    final ARPortID portTargetID)
    throws ARException
  {
    final var edge = new ARPortConnection(portSourceID, portTargetID);
    if (!this.portGraph.containsEdge(edge)) {
      throw new ARException(
        "Ports are not connected.",
        "error-ports-not-connected",
        Map.ofEntries(
          Map.entry("SourcePort", portSourceID.toString()),
          Map.entry("TargetPort", portTargetID.toString())
        ),
        Optional.empty()
      );
    }
  }

  /**
   * Produce a deep copy of the current graph.
   *
   * @return The copy
   */

  public AREnsGraph copy()
  {
    return new AREnsGraph(
      (DirectedAcyclicGraph<ARPortID, ARPortConnection>)
        this.portGraph.clone(),
      (DirectedAcyclicGraph<ARInstrumentInstanceID, ARInstrumentConnection>)
        this.instrumentGraph.clone(),
      new HashMap<>(this.ports),
      new HashMap<>(this.instruments)
    );
  }

  private void checkPortInstrumentsDifferent(
    final ARPortID portSource,
    final ARPortID portTarget)
    throws ARException
  {
    final var instrumentSource =
      this.ports.get(portSource).instrumentInstance();
    final var instrumentTarget =
      this.ports.get(portTarget).instrumentInstance();

    if (Objects.equals(instrumentSource, instrumentTarget)) {
      throw new ARException(
        "Source and target instruments must be different.",
        "error-instruments-same",
        Map.ofEntries(
          Map.entry("SourcePort", portSource.toString()),
          Map.entry("TargetPort", portTarget.toString()),
          Map.entry("InstrumentSource", instrumentSource.toString()),
          Map.entry("InstrumentTarget", instrumentTarget.toString())
        ),
        Optional.empty()
      );
    }
  }

  private void checkPortsDifferent(
    final ARPortID portSource,
    final ARPortID portTarget)
    throws ARException
  {
    if (Objects.equals(portSource, portTarget)) {
      throw new ARException(
        "Source and target ports must be different.",
        "error-ports-same",
        Map.ofEntries(
          Map.entry("SourcePort", portSource.toString()),
          Map.entry("TargetPort", portTarget.toString())
        ),
        Optional.empty()
      );
    }
  }

  private void checkPortNonexistent(
    final ARPortID portID)
    throws ARException
  {
    if (this.ports.containsKey(portID)) {
      throw new ARException(
        "Port already exists.",
        "error-port-duplicate",
        Map.of("Port", portID.toString()),
        Optional.empty()
      );
    }
  }

  private void checkInstrumentExists(
    final ARInstrumentInstanceID instanceID)
    throws ARException
  {
    if (!this.instruments.containsKey(instanceID)) {
      throw new ARException(
        "Instrument does not exist.",
        "error-instrument-nonexistent",
        Map.of("Instrument", instanceID.toString()),
        Optional.empty()
      );
    }
  }

  private ARPort checkPortExists(
    final ARPortID portID)
    throws ARException
  {
    final var port = this.ports.get(portID);
    if (port == null) {
      throw new ARException(
        "Port does not exist.",
        "error-port-nonexistent",
        Map.of("Port", portID.toString()),
        Optional.empty()
      );
    }
    return port;
  }

  private void checkInstrumentNonexistent(
    final ARInstrumentInstanceID instanceID)
    throws ARException
  {
    if (this.instruments.containsKey(instanceID)) {
      throw new ARException(
        "Instrument already exists.",
        "error-instrument-duplicate",
        Map.of("Instrument", instanceID.toString()),
        Optional.empty()
      );
    }
  }

  private void checkPortTarget(
    final ARPort port)
    throws ARException
  {
    if (port.direction() != ARPortDirection.AR_TARGET) {
      throw new ARException(
        "Port is not a target port.",
        "error-port-not-target",
        Map.of("Port", port.id().toString()),
        Optional.empty()
      );
    }
  }

  private void checkPortSource(
    final ARPort port)
    throws ARException
  {
    if (port.direction() != ARPortDirection.AR_SOURCE) {
      throw new ARException(
        "Port is not a source port.",
        "error-port-not-source",
        Map.of("Port", port.id().toString()),
        Optional.empty()
      );
    }
  }

  @Override
  public boolean portIsRegistered(
    final ARPortID port)
  {
    Objects.requireNonNull(port, "Port");
    return this.ports.containsKey(port);
  }

  @Override
  public boolean portIsConnected(
    final ARPortID portSourceID,
    final ARPortID portTargetID)
    throws ARException
  {
    Objects.requireNonNull(portSourceID, "PortSource");
    Objects.requireNonNull(portTargetID, "PortTarget");

    this.checkPortExists(portSourceID);
    this.checkPortExists(portTargetID);

    return this.portGraph.containsEdge(
      new ARPortConnection(portSourceID, portTargetID)
    );
  }
}
