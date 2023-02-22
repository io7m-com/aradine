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

package com.io7m.aradine.instrument.sampler_m0;

import com.io7m.aradine.instrument.sampler_m0.internal.ARIM0Sampler;
import com.io7m.aradine.instrument.sampler_m0.internal.Parameters;
import com.io7m.aradine.instrument.sampler_m0.internal.Ports;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentFactoryType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentServicesType;
import com.io7m.aradine.instrument.spi1.ARI1InstrumentType;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;

/**
 * A factory of monophonic samplers.
 */

@Component
public final class ARIM0SamplerFactory
  implements ARI1InstrumentFactoryType
{
  /**
   * A factory of monophonic samplers.
   */

  public ARIM0SamplerFactory()
  {

  }

  @Override
  public InputStream openInstrumentDescription()
  {
    return ARIM0SamplerFactory.class.getResourceAsStream(
      "/com/io7m/aradine/instrument/sampler_m0/internal/instrument.xml"
    );
  }

  @Override
  public ARI1InstrumentType createInstrument(
    final ARI1InstrumentServicesType services)
  {
    return new ARIM0Sampler(
      services.createEventBuffer(),
      new Parameters(services),
      new Ports(services)
    );
  }
}
