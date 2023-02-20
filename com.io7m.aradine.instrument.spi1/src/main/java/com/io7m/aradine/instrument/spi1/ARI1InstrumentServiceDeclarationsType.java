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
 * Methods used by instruments to retrieve instances of declared parameters and
 * ports.
 */

public interface ARI1InstrumentServiceDeclarationsType
{
  /**
   * @return The instantiated parameters declared by the instrument
   */

  Map<ARI1ParameterId, ARI1ParameterType> declaredParameters();

  /**
   * Retrieve a declared parameter and cast it to the given type.
   *
   * @param id    The parameter ID
   * @param <C>   The parameter class
   * @param clazz The parameter class
   *
   * @return The parameter
   */

  <C extends ARI1ParameterType> C declaredParameter(
    ARI1ParameterId id,
    Class<C> clazz
  );

  /**
   * @return The instantiated ports declared by the instrument
   */

  Map<ARI1PortId, ARI1PortType> declaredPorts();

  /**
   * Retrieve a declared port and cast it to the given type.
   *
   * @param id    The port ID
   * @param <C>   The port class
   * @param clazz The port class
   *
   * @return The port
   */

  <C extends ARI1PortType> C declaredPort(
    ARI1PortId id,
    Class<C> clazz
  );
}
