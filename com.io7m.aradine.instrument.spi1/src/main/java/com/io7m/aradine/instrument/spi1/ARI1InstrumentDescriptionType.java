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


package com.io7m.aradine.instrument.spi1;

import java.util.Map;

/**
 * A description of an instrument.
 */

public interface ARI1InstrumentDescriptionType
{
  /**
   * The unique identifier of the instrument. This name is declared in the
   * instrument's description and <i>MUST</i> match the <i>symbolic name</i> of
   * the OSGi bundle that contains the instrument.
   *
   * @return The identifier
   */

  String identifier();

  /**
   * The version of the instrument. This version is declared in the instrument's
   * description and <i>MUST</i> match the <i>version</i> of the OSGi bundle
   * that contains the instrument.
   *
   * @return The identifier
   */

  ARI1Version version();

  /**
   * @return The metadata strings declared in the instrument description
   */

  Map<String, String> metadata();

  /**
   * @return The parameters declared by the instrument
   */

  Map<ARI1ParameterId, ARI1ParameterDescriptionType> parameters();

  /**
   * @return The ports declared by the instrument
   */

  Map<ARI1PortId, ARI1PortDescriptionType> ports();
}
