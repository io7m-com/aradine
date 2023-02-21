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


package com.io7m.aradine.tests.spi1;

import com.io7m.aradine.instrument.spi1.ARI1ParameterDescriptionIntegerType;
import com.io7m.aradine.instrument.spi1.ARI1ParameterId;
import com.io7m.aradine.tests.ARI1ParameterInteger;

public final class ARI1ParameterIntegerTest
  extends ARI1ParameterIntegerContract<ARI1ParameterInteger>
{
  @Override
  protected ARI1ParameterInteger createParameter(
    final ARI1ParameterId id,
    final long valueMinimum,
    final long valueMaximum,
    final long valueDefault)
  {
    return new ARI1ParameterInteger(
      new ARI1ParameterDescriptionIntegerType()
      {
        @Override
        public String unitOfMeasurement()
        {
          return "x";
        }

        @Override
        public long valueMinimum()
        {
          return valueMinimum;
        }

        @Override
        public long valueMaximum()
        {
          return valueMaximum;
        }

        @Override
        public long valueDefault()
        {
          return valueDefault;
        }

        @Override
        public ARI1ParameterId id()
        {
          return id;
        }

        @Override
        public String label()
        {
          return "Label";
        }
      }
    );
  }

  @Override
  protected void setValue(
    final ARI1ParameterInteger parameter,
    final int time,
    final long value)
  {
    parameter.valueChange(time, value);
  }

  @Override
  protected void clearChanges(
    final ARI1ParameterInteger parameter)
  {
    parameter.valueChangesClear();
  }
}
