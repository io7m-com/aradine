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

import com.io7m.aradine.api.directories.ARApplicationDirectories;
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
 * The info command.
 */

public final class ARCmdInfo extends ARCmdAbstract
{
  /**
   * The info command.
   */

  public ARCmdInfo()
  {
    super(new QCommandMetadata(
      "info",
      new QConstant("Display system information."),
      Optional.empty()
    ));
  }

  @Override
  protected Logger logger()
  {
    return LoggerFactory.getLogger(ARCmdInfo.class);
  }

  @Override
  protected QCommandStatus onExecuteActual(
    final QCommandContextType context)
  {
    final var mapper = JsonMapper.shared();
    final var output = mapper.createObjectNode();

    final var directories = ARApplicationDirectories.directories();
    output.put(
      "CacheDirectory",
      directories.cacheDirectory().toString()
    );
    output.put(
      "ConfigurationDirectory",
      directories.configurationDirectory().toString()
    );
    output.put(
      "DataDirectory",
      directories.dataDirectory().toString()
    );
    output.put(
      "InventoryDatabase",
      ARApplicationDirectories.inventoryDatabase(directories).toString()
    );
    output.put(
      "InventoryBlobDirectory",
      ARApplicationDirectories.inventoryBlobs(directories).toString()
    );

    context.output()
      .println(
        mapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(output)
      );
    return QCommandStatus.SUCCESS;
  }

  @Override
  protected List<QParameterNamedType<?>> onListNamedParametersActual()
  {
    return List.of();
  }
}
