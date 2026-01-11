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
import com.io7m.aradine.api.instrument.ARInstrumentDescription;
import com.io7m.aradine.api.instrument.ARInstrumentID;
import com.io7m.aradine.api.instrument.ARInstrumentInstanceID;
import com.io7m.aradine.api.instrument.ARInstrumentRole;
import com.io7m.aradine.api.parameters.ARParameterID;
import com.io7m.aradine.api.ports.ARPortID;

import java.util.Map;

/**
 * A loaded instrument.
 */

public interface AREnsInstrumentType
  extends AutoCloseable
{
  /**
   * @return The ports
   */

  Map<ARPortID, AREnsPortInstanceType> ports();

  /**
   * @return The parameters
   */

  Map<ARParameterID, AREnsParameterType> parameters();

  /**
   * @return The instrument description
   */

  ARInstrumentDescription description();

  /**
   * @return The instance ID
   */

  default ARInstrumentInstanceID instanceID()
  {
    return this.description().instanceId();
  }

  /**
   * @return The instrument identifier
   */

  default ARInstrumentID identifier()
  {
    return this.description().identifier();
  }

  /**
   * @return The instrument role
   */

  default ARInstrumentRole role()
  {
    return this.description().role();
  }

  @Override
  void close()
    throws ARException;
}
