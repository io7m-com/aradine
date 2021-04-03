/*
 * Copyright © 2021 Mark Raynsford <code@io7m.com> http://io7m.com
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

package com.io7m.aradine.instrument.metadata;

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Metadata about an instrument.
 */

@ImmutablesStyleType
@Value.Immutable
public interface ARInstrumentMetadataType
{
  /**
   * @return The globally unique ID of the instrument
   */

  ARInstrumentID id();

  /**
   * @return The instrument version
   */

  ARInstrumentVersion version();

  /**
   * @return The humanly-readable name of the instrument
   */

  String readableName();

  /**
   * @return The list of authors, if any
   */

  List<ARInstrumentAuthor> authors();

  /**
   * @return The ID of the instrument license
   */

  ARInstrumentLicenseID licenseId();

  /**
   * @return The plugin's web site, if any
   */

  Optional<URI> site();

  /**
   * @return The plugin's icon, if any
   */

  Optional<URI> icon();

  /**
   * @return The instrument input ports
   */

  List<ARInstrumentInputPortDefinitionType> inputs();

  /**
   * @return The instrument output ports
   */

  List<ARInstrumentOutputPortDefinitionType> outputs();

  /**
   * @return The instrument input ports by name
   */

  @Value.Derived
  @Value.Auxiliary
  default Map<ARInstrumentPortID, ARInstrumentInputPortDefinitionType> inputsByName()
  {
    return this.inputs()
      .stream()
      .collect(Collectors.toMap(
        ARInstrumentPortDefinitionType::id,
        Function.identity()
      ));
  }

  /**
   * @return The instrument output ports by name
   */

  @Value.Derived
  @Value.Auxiliary
  default Map<ARInstrumentPortID, ARInstrumentOutputPortDefinitionType> outputsByName()
  {
    return this.outputs()
      .stream()
      .collect(Collectors.toMap(
        ARInstrumentPortDefinitionType::id,
        Function.identity()
      ));
  }
}
