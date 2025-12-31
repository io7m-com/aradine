/*
 * Copyright © 2025 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

package com.io7m.aradine.instrument.spi1.json_data.internal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Optional;

/**
 * <p>A real-typed parameter description.</p>
 *
 * @param number            The parameter ID
 * @param label             The parameter label
 * @param documentation     The parameter documentation
 * @param unitOfMeasurement The unit of measurement for the parameter
 * @param valueMinimum      The minimum value (inclusive)
 * @param valueMaximum      The maximum value (inclusive)
 * @param valueDefault      The default value
 */

public record ARI1JParameterDescriptionReal(
  @JsonProperty(value = "Number", required = true)
  ARI1JParameterNumber number,
  @JsonProperty(value = "Label", required = true)
  String label,
  @JsonProperty(value = "Documentation")
  Optional<ARI1JDocumentation> documentation,
  @JsonProperty(value = "UnitOfMeasurement", required = true)
  ARI1JDottedName unitOfMeasurement,
  @JsonProperty(value = "RealValueMinimumInclusive", required = true)
  double valueMinimum,
  @JsonProperty(value = "RealValueMaximumInclusive", required = true)
  double valueMaximum,
  @JsonProperty(value = "RealValueDefault", required = true)
  double valueDefault)
  implements ARI1JParameterDescriptionType,
  ARI1JElementType
{
  /**
   * <p>A real-typed parameter description.</p>
   *
   * @param number            The parameter ID
   * @param label             The parameter label
   * @param documentation     The parameter documentation
   * @param unitOfMeasurement The unit of measurement for the parameter
   * @param valueMinimum      The minimum value (inclusive)
   * @param valueMaximum      The maximum value (inclusive)
   * @param valueDefault      The default value
   */

  public ARI1JParameterDescriptionReal
  {
    Objects.requireNonNull(number, "id");
    Objects.requireNonNull(label, "label");
    Objects.requireNonNull(documentation, "documentation");
    Objects.requireNonNull(unitOfMeasurement, "unitOfMeasurement");
  }
}
