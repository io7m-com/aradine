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

import com.io7m.aradine.api.parameters.ARParameterDescriptionType;
import com.io7m.aradine.api.parameters.ARParameterID;

/**
 * A parameter on an instrument. This is the interface that is
 * visible to the ensemble implementation. This is _not_ visible to the
 * instrument implementation.
 */

public sealed interface AREnsParameterType
  permits AREnsParameterInteger, AREnsParameterReal, AREnsParameterSampleMap
{
  /**
   * @return The port description
   */

  ARParameterDescriptionType description();

  /**
   * @return The ID of the port
   */

  default ARParameterID parameterID()
  {
    return this.description().id();
  }

  /**
   * Clear all value changes for this processing period.
   */

  void valueChangesClear();
}
