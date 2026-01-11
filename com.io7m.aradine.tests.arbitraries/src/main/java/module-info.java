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

import com.io7m.aradine.tests.arbitraries.ARI1ProviderEvent;
import com.io7m.aradine.tests.arbitraries.ARI1ProviderInstrumentID;
import com.io7m.aradine.tests.arbitraries.ARI1ProviderInstrumentInstanceID;
import com.io7m.aradine.tests.arbitraries.ARI1ProviderParameterNumber;
import com.io7m.aradine.tests.arbitraries.ARI1ProviderPortNumber;
import com.io7m.aradine.tests.arbitraries.ARI1ProviderSampleMapID;
import com.io7m.aradine.tests.arbitraries.ARI1ProviderSPI1SampleMapID;
import com.io7m.aradine.tests.arbitraries.ARI1ProviderDottedName1;
import com.io7m.aradine.tests.arbitraries.ARI1ProviderUUID;
import com.io7m.aradine.tests.arbitraries.ARI1ProviderValueChangedInteger;
import com.io7m.aradine.tests.arbitraries.ARI1ProviderValueChangedIntegerMaps;
import com.io7m.aradine.tests.arbitraries.ARI1ProviderValueChangedReal;
import com.io7m.aradine.tests.arbitraries.ARI1ProviderValueChangedRealMaps;
import com.io7m.aradine.tests.arbitraries.ARI1ProviderValueChangedSampleMap;
import com.io7m.aradine.tests.arbitraries.ARI1ProviderValueChangedSampleMapMaps;
import com.io7m.aradine.tests.arbitraries.ARProviderPortNumber;
import com.io7m.aradine.tests.arbitraries.ARI1ProviderSPI1SampleMapInstanceID;
import net.jqwik.api.providers.ArbitraryProvider;

/**
 * Modular programmable synthesis (Arbitrary value instances)
 */

module com.io7m.aradine.tests.arbitraries
{
  requires transitive net.jqwik.api;

  requires com.io7m.aradine.api;
  requires com.io7m.aradine.instrument.spi1;
  requires com.io7m.lanark.core;
  requires com.io7m.verona.core;

  provides ArbitraryProvider
    with
      ARI1ProviderSPI1SampleMapInstanceID,
      ARI1ProviderDottedName1,
      ARI1ProviderEvent,
      ARI1ProviderInstrumentID,
      ARI1ProviderInstrumentInstanceID,
      ARI1ProviderParameterNumber,
      ARI1ProviderPortNumber,
      ARI1ProviderSampleMapID,
      ARI1ProviderSPI1SampleMapID,
      ARI1ProviderUUID,
      ARI1ProviderValueChangedInteger,
      ARI1ProviderValueChangedIntegerMaps,
      ARI1ProviderValueChangedReal,
      ARI1ProviderValueChangedRealMaps,
      ARI1ProviderValueChangedSampleMap,
      ARI1ProviderValueChangedSampleMapMaps,
      ARProviderPortNumber
    ;

  exports com.io7m.aradine.tests.arbitraries;
}
