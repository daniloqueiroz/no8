/**
 * No8  Copyright (C) 2015  no8.io
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package no8.io;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannel;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import no8.async.AsyncLoop;
import no8.async.future.Futures;
import no8.codec.Codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AsynchronousIO entities encapsulate the usage of java.nio.channels {@link AsynchronousChannel}.
 * 
 * {@link AsynchronousIO} objects returns a {@link CompletableFuture} and executes using the
 * {@link AsyncLoop#loop()}.
 */
public abstract class AsynchronousIO<T extends AsynchronousChannel, E> implements Closeable {

  protected static final Logger LOG = LoggerFactory.getLogger(AsynchronousIO.class);

  protected final int bufferSize;
  protected final AsyncLoop loop;
  protected final Codec<E> codec;
  protected final T channel;

  protected AsynchronousIO(T channel, Codec<E> codec, AsyncLoop loop, int bufferSize) {
    this.channel = channel;
    this.codec = codec;
    this.loop = loop;
    this.bufferSize = bufferSize;
  }

  @Override
  public void close() {
    try {
      this.channel.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Gets the {@link AsynchronousChannel} wrapped by this {@link AsynchronousIO} object.
   */
  public T toAsynchronousChannel() {
    return this.channel;
  }

  public final CompletableFuture<AsynchronousIO<T, E>> write(E content) {
    return this.write(this.codec.encode(content));
  }

  public final CompletableFuture<Optional<E>> read() {
    ByteBuffer buffer = ByteBuffer.allocateDirect(this.bufferSize);
    Future<Integer> result = this.read(buffer);
    Future<Optional<E>> transformedFuture = Futures.<Integer, Optional<E>> transform(result).using((readBytes) -> {
      buffer.rewind();
      return (readBytes > 0) ? Optional.<E> of(this.codec.decode(buffer)) : Optional.empty();
    });
    return this.loop.runWhenDone(transformedFuture);
  }

  protected abstract Future<Integer> read(ByteBuffer buffer);

  protected abstract CompletableFuture<AsynchronousIO<T, E>> write(ByteBuffer buffer);
}