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

package com.io7m.aradine.instrument.spi1;

import java.util.Objects;
import java.util.Optional;

/**
 * <p>An integer-typed parameter description.</p>
 *
 * @param id                The parameter ID
 * @param label             The parameter label
 * @param documentation     The parameter documentation
 * @param unitOfMeasurement The unit of measurement for the parameter
 * @param valueMinimum      The minimum value (inclusive)
 * @param valueMaximum      The maximum value (inclusive)
 * @param valueDefault      The default value
 */

public record ARI1ParameterDescriptionInteger(
  ARI1ParameterId id,
  String label,
  Optional<ARI1Documentation> documentation,
  ARI1DottedName unitOfMeasurement,
  long valueMinimum,
  long valueMaximum,
  long valueDefault)
  implements ARI1ParameterDescriptionType
{
  /**
   * <p>An integer-typed parameter description.</p>
   *
   * @param id                The parameter ID
   * @param label             The parameter label
   * @param documentation     The parameter documentation
   * @param unitOfMeasurement The unit of measurement for the parameter
   * @param valueMinimum      The minimum value (inclusive)
   * @param valueMaximum      The maximum value (inclusive)
   * @param valueDefault      The default value
   */

  public ARI1ParameterDescriptionInteger
  {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(label, "label");
    Objects.requireNonNull(documentation, "documentation");
    Objects.requireNonNull(unitOfMeasurement, "unitOfMeasurement");

    if (valueMaximum < valueMinimum) {
      throw new IllegalArgumentException(
        "The minimum value %s must be <= maximum value %s."
          .formatted(valueMinimum, valueMaximum)
      );
    }
    if (!(valueDefault >= valueMinimum && valueDefault <= valueMaximum)) {
      throw new IllegalArgumentException(
        "The default value %s must be in the range [%s, %s]."
          .formatted(valueDefault, valueMinimum, valueMaximum)
      );
    }
  }
}
