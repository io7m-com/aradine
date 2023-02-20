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


package com.io7m.aradine.tests;

import com.io7m.aradine.instrument.spi1.ARI1ParameterId;
import com.io7m.aradine.instrument.spi1.ARI1ParameterIntegerType;

import java.util.Objects;

public final class ARI1ParameterInteger
  implements ARI1ParameterIntegerType
{
  private volatile long value;
  private final ARI1ParameterId id;
  private volatile String label;
  private long valueMinimum;
  private long valueMaximum;

  public ARI1ParameterInteger(
    final ARI1ParameterId inId)
  {
    this.id = Objects.requireNonNull(inId, "inId");
    this.label = "";
  }

  @Override
  public ARI1ParameterId id()
  {
    return this.id;
  }

  @Override
  public String label()
  {
    return this.label;
  }

  @Override
  public long valueMinimum()
  {
    return this.valueMinimum;
  }

  @Override
  public long valueMaximum()
  {
    return this.valueMaximum;
  }

  @Override
  public long value()
  {
    return this.value;
  }
}
