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

import static no8.utils.MetricsHelper.histogram;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import no8.async.AsyncLoop;
import no8.async.future.Futures;
import no8.codec.Codec;

import com.codahale.metrics.Histogram;

public class AsynchronousSocket<T> extends AsynchronousIO<AsynchronousSocketChannel, T> {

  private Histogram sentBytes = histogram(AsynchronousSocket.class, "bytes", "sent");;
  private Histogram receivedBytes = histogram(AsynchronousSocket.class, "bytes", "received");

  public AsynchronousSocket(AsynchronousSocketChannel socketChannel, Codec<T> codec, AsyncLoop loop, int bufferSize) {
    super(socketChannel, codec, loop, bufferSize);
  }

  public CompletableFuture<AsynchronousSocket<T>> connect(SocketAddress address) {
    Future<Void> connection = this.channel.connect(address);
    Future<AsynchronousSocket<T>> transformedFuture = Futures.<Void, AsynchronousSocket<T>> transform(
        connection).using((Void) -> {
      return this;
    });

    return this.loop.runWhenDone(transformedFuture);
  }

  @Override
  protected CompletableFuture<AsynchronousIO<AsynchronousSocketChannel, T>> write(ByteBuffer buffer) {
    Future<Integer> result = this.channel.write(buffer);
    Future<AsynchronousIO<AsynchronousSocketChannel, T>> transformedFuture = Futures
        .<Integer, AsynchronousIO<AsynchronousSocketChannel, T>> transform(result).using(
        (writtenBytes) -> {
          this.sentBytes.update(writtenBytes);
          return this;
        });
    return this.loop.runWhenDone(transformedFuture);
  }

  @Override
  protected Future<Integer> read(ByteBuffer buffer) {
    return Futures.<Integer, Integer> transform(this.channel.read(buffer)).using((read) -> {
      this.receivedBytes.update(read);
      return read;
    });
  }

  /**
   * @see AsynchronousSocketChannel#getRemoteAddress()
   */
  public SocketAddress remoteAddress() {
    try {
      return this.channel.getRemoteAddress();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @see AsynchronousSocketChannel#getLocalAddress()
   */
  public SocketAddress localAddress() {
    try {
      return this.channel.getLocalAddress();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


}
