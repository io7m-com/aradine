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

package com.io7m.aradine.system.jnajack;

import com.io7m.aradine.api.system.ARAudioSystemConfigurationType;
import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

import java.util.Objects;

/**
 * The configuration required for the audio system.
 */

@Value.Immutable
@ImmutablesStyleType
public interface ARJJConfigurationType
  extends ARAudioSystemConfigurationType
{
  /**
   * The application name that will be used for the Jack client.
   *
   * @return The application name
   */

  @Value.Default
  default String applicationName()
  {
    return "Aradine";
  }

  /**
   * Number of source ports to be instantiated.
   *
   * @return The source port count
   */

  @Value.Default
  default int portSourceCount()
  {
    return 16;
  }

  /**
   * Number of target ports to be instantiated.
   *
   * @return The target port count
   */

  @Value.Default
  default int portTargetCount()
  {
    return 16;
  }

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    Objects.checkIndex(this.portSourceCount(), Integer.MAX_VALUE);
    Objects.checkIndex(this.portTargetCount(), Integer.MAX_VALUE);
  }
}
