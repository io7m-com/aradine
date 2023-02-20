/*
 * Copyright © 2022 Mark Raynsford <code@io7m.com> https://www.io7m.com
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


package com.io7m.aradine.instrument.codegen.internal;

import com.io7m.aradine.instrument.spi1.ARI1InstrumentDescriptionType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterId;
import com.io7m.aradine.instrument.spi1.ARI1PortDescriptionType;
import com.io7m.aradine.instrument.spi1.ARI1PortId;
import com.io7m.aradine.instrument.spi1.ARI1Version;

import java.util.Map;

/**
 * An instrument description.
 *
 * @param identifier The identifier
 * @param version    The version
 * @param metadata   The metadata
 * @param parameters The parameters
 * @param ports      The ports
 */

public record ARI1InstrumentDescription(
  String identifier,
  ARI1Version version,
  Map<String, String> metadata,
  Map<ARI1ParameterId, ARI1ParameterDescriptionType> parameters,
  Map<ARI1PortId, ARI1PortDescriptionType> ports)
  implements ARI1InstrumentDescriptionType
{

}
