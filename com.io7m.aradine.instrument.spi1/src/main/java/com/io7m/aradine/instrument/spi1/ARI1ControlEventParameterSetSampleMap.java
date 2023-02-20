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

import com.io7m.aradine.annotations.ARTimeFrames;

import java.net.URI;
import java.util.Objects;

/**
 * A control event to set the value of a sample-map typed parameter.
 *
 * @param timeOffsetInFrames The time offset
 * @param parameter          The parameter
 * @param location           The value
 */

public record ARI1ControlEventParameterSetSampleMap(
  @ARTimeFrames int timeOffsetInFrames,
  ARI1ParameterId parameter,
  URI location)
  implements ARI1ControlEventParameterSetType
{
  /**
   * A control event to set the value of a sample-map typed parameter.
   *
   * @param timeOffsetInFrames The time offset
   * @param parameter          The parameter
   * @param location           The value
   */

  public ARI1ControlEventParameterSetSampleMap
  {
    Objects.requireNonNull(parameter, "parameter");
    Objects.requireNonNull(location, "location");
  }

  @Override
  public String toString()
  {
    return String.format(
      "[ARI1ControlEventParameterSetSampleMap [Time %d] %s [Value %s]]",
      Integer.valueOf(this.timeOffsetInFrames),
      this.parameter,
      this.location
    );
  }
}
