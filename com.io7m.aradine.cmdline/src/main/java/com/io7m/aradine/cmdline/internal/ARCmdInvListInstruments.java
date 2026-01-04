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

package com.io7m.aradine.cmdline.internal;

import com.io7m.aradine.api.ARCloseables;
import com.io7m.aradine.api.directories.ARApplicationDirectories;
import com.io7m.aradine.api.instrument.ARInstrumentID;
import com.io7m.quarrel.core.QCommandContextType;
import com.io7m.quarrel.core.QCommandMetadata;
import com.io7m.quarrel.core.QCommandStatus;
import com.io7m.quarrel.core.QParameterNamedType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Optional;

import static com.io7m.quarrel.core.QStringType.QConstant;

/**
 * The inventory list command.
 */

public final class ARCmdInvListInstruments extends ARCmdAbstract
{
  /**
   * The inventory list command.
   */

  public ARCmdInvListInstruments()
  {
    super(new QCommandMetadata(
      "list-instruments",
      new QConstant("List instruments in the local inventory."),
      Optional.empty()
    ));
  }

  @Override
  protected Logger logger()
  {
    return LoggerFactory.getLogger(ARCmdInvListInstruments.class);
  }

  @Override
  protected QCommandStatus onExecuteActual(
    final QCommandContextType context)
    throws Exception
  {
    final var directories = ARApplicationDirectories.directories();
    try (var resources = ARCloseables.create()) {
      final var inventory =
        ARCInventories.openInventory(directories, resources);

      final var mapper = JsonMapper.shared();
      final var output = mapper.createArrayNode();

      Optional<ARInstrumentID> start = Optional.empty();
      while (true) {
        final var r = inventory.instrumentList(start, 1000).get();
        if (r.isEmpty()) {
          break;
        }
        for (final var summary : r) {
          final var o = mapper.createObjectNode();
          final var identifier = summary.identifier();
          o.put("Group", identifier.group().value());
          o.put("Name", identifier.name().value());
          o.put("Version", identifier.version().toString());
          o.put("Identifier", summary.identifier().toString());
          o.put("Title", summary.title());
          o.put("Description", summary.description());
          output.add(o);
        }
        start = Optional.of(r.getLast().identifier());
      }

      mapper.writerWithDefaultPrettyPrinter().writeValue(System.out, output);
      return QCommandStatus.SUCCESS;
    }
  }

  @Override
  protected List<QParameterNamedType<?>> onListNamedParametersActual()
  {
    return List.of();
  }
}
