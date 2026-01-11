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

package com.io7m.aradine.api.instrument;

import java.util.Objects;

/**
 * A reference to a loaded instrument.
 *
 * @param instanceID The instance ID
 * @param identifier The instrument identifier
 * @param role       The instrument role
 */

public record ARInstrumentReference(
  ARInstrumentInstanceID instanceID,
  ARInstrumentID identifier,
  ARInstrumentRole role)
{
  /**
   * A reference to a loaded instrument.
   *
   * @param instanceID The instance ID
   * @param identifier The instrument identifier
   * @param role       The instrument role
   */

  public ARInstrumentReference
  {
    Objects.requireNonNull(instanceID, "InstanceID");
    Objects.requireNonNull(identifier, "Identifier");
    Objects.requireNonNull(role, "Role");
  }
}
