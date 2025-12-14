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

package com.io7m.aradine.inventory.internal;

import com.io7m.aradine.inventory.api.ARInventoryException;
import com.io7m.aradine.inventory.api.ARInventoryHash;
import com.io7m.jmulticlose.core.CloseableCollectionType;
import com.io7m.streamtime.core.STTimedInputStream;
import com.io7m.streamtime.core.STTransferStatistics;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.concurrent.CancellationException;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * A multi-thread, multi-process blob directory.
 */

public final class ARInventoryBlobDirectory
{
  private static final OpenOption[] CREATE_OPEN_OPTIONS = {
    StandardOpenOption.CREATE,
    StandardOpenOption.TRUNCATE_EXISTING,
    StandardOpenOption.WRITE,
  };

  private static final OpenOption[] LOCK_OPEN_OPTIONS = {
    StandardOpenOption.CREATE,
    StandardOpenOption.TRUNCATE_EXISTING,
    StandardOpenOption.WRITE,
  };

  private final Path baseDirectory;
  private final Object writeLock;

  /**
   * Create a blob directory.
   *
   * @param inBaseDirectory The directory
   */

  public ARInventoryBlobDirectory(
    final Path inBaseDirectory)
  {
    this.baseDirectory =
      Objects.requireNonNull(inBaseDirectory, "BaseDirectory");
    this.writeLock =
      new Object();
  }

  private static void doWrite(
    final BooleanSupplier cancelled,
    final CloseableCollectionType<ARInventoryException> resources,
    final FileChannel outChannel,
    final STTimedInputStream timedStream)
    throws IOException
  {
    final var outStream =
      resources.add(Channels.newOutputStream(outChannel));

    final var buffer = new byte[4096];
    while (true) {
      checkCancelled(cancelled);

      final var r = timedStream.read(buffer);
      if (r == -1) {
        break;
      }
      outStream.write(buffer, 0, r);
    }
  }

  private static void checkCancelled(
    final BooleanSupplier cancelled)
  {
    if (cancelled.getAsBoolean()) {
      throw new CancellationException();
    }
  }

  /**
   * Copy a file into the directory.
   *
   * @param hash      The hash
   * @param file      The file
   * @param progress  The progress receiver
   * @param cancelled A cancellation oracle
   *
   * @return The written file path
   *
   * @throws IOException           On errors
   * @throws ARInventoryException    On errors
   * @throws CancellationException On errors
   */

  public Path copyIn(
    final ARInventoryHash hash,
    final Path file,
    final Consumer<Double> progress,
    final BooleanSupplier cancelled)
    throws
    IOException,
    ARInventoryException,
    CancellationException
  {
    synchronized (this.writeLock) {
      return this.copyInLocked(hash, file, progress, cancelled);
    }
  }

  private Path copyInLocked(
    final ARInventoryHash hash,
    final Path file,
    final Consumer<Double> progress,
    final BooleanSupplier cancelled)
    throws ARInventoryException, IOException
  {
    checkCancelled(cancelled);

    final var hashBase =
      this.baseDirectory.resolve(hash.algorithm().name())
        .normalize();
    final var outputFile =
      hashBase.resolve(hash.value() + ".blob")
        .normalize();
    final var outputFileTemp =
      hashBase.resolve(hash.value() + ".blob.tmp")
        .normalize();
    final var outputFileLock =
      hashBase.resolve(hash.value() + ".blob.lock")
        .normalize();

    try (var resources = ARCloseables.create()) {
      checkCancelled(cancelled);
      Files.createDirectories(hashBase);
      checkCancelled(cancelled);
      this.obtainLock(resources, outputFileLock);
      checkCancelled(cancelled);

      final Consumer<STTransferStatistics> statConsumer = stats -> {
        progress.accept(stats.percentNormalized().orElse(0.0));
      };

      final var stream =
        resources.add(Files.newInputStream(file));
      checkCancelled(cancelled);

      final var timedStream =
        resources.add(new STTimedInputStream(statConsumer, stream));
      checkCancelled(cancelled);

      try (var outChannel =
             FileChannel.open(outputFileTemp, CREATE_OPEN_OPTIONS)) {
        checkCancelled(cancelled);
        doWrite(cancelled, resources, outChannel, timedStream);
        statConsumer.accept(doneStats(file));

        Files.move(
          outputFileTemp,
          outputFile,
          StandardCopyOption.ATOMIC_MOVE,
          StandardCopyOption.REPLACE_EXISTING
        );
      }
    }
    return outputFile;
  }

  private static STTransferStatistics doneStats(
    final Path file)
    throws IOException
  {
    final long size = Files.size(file);
    return new STTransferStatistics(
      OptionalLong.of(size),
      size,
      size
    );
  }

  private FileLock obtainLock(
    final CloseableCollectionType<ARInventoryException> resources,
    final Path outputFileLock)
    throws IOException
  {
    final var lockChannel =
      resources.add(FileChannel.open(outputFileLock, LOCK_OPEN_OPTIONS));

    return lockChannel.lock();
  }
}
