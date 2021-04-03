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

package com.io7m.aradine.xml.internal;

import com.io7m.blackthorne.api.BTElementHandlerType;
import com.io7m.blackthorne.api.BTElementParsingContextType;
import com.io7m.jranges.RangeInclusiveD;
import org.xml.sax.Attributes;

public final class AR1ExpectedSignalRangeParser
  implements BTElementHandlerType<Object, RangeInclusiveD>
{
  private double rangeMin;
  private double rangeMax;

  public AR1ExpectedSignalRangeParser(
    final BTElementParsingContextType context)
  {
    this.rangeMin = 0.0;
    this.rangeMax = 1.0;
  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
  {
    this.rangeMin =
      Double.parseDouble(attributes.getValue("minimum"));
    this.rangeMax =
      Double.parseDouble(attributes.getValue("maximum"));
  }

  @Override
  public RangeInclusiveD onElementFinished(
    final BTElementParsingContextType context)
  {
    return RangeInclusiveD.of(this.rangeMin, this.rangeMax);
  }
}
