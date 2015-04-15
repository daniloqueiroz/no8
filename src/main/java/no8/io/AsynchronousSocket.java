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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import no8.async.AsyncLoop;
import no8.async.TransformedFuture;

import com.codahale.metrics.Histogram;

public class AsynchronousSocket extends AsynchronousChannelWrapper<AsynchronousSocketChannel> implements
    AsynchronousIO<ByteBuffer> {

  private static final int BUFFER_SIZE = 512;
  private Histogram sentBytes;
  private Histogram receivedBytes;

  public AsynchronousSocket(AsynchronousSocketChannel socketChannel, AsyncLoop loop) {
    super(socketChannel, loop);

    this.receivedBytes = histogram(AsynchronousSocket.class, "bytes", "received");
    this.sentBytes = histogram(AsynchronousSocket.class, "bytes", "sent");
  }

  public CompletableFuture<AsynchronousSocket> connect(SocketAddress address) {
    Future<Void> connection = this.channel.connect(address);
    TransformedFuture<Void, AsynchronousSocket> transformedFuture = TransformedFuture.<Void, AsynchronousSocket> from(
        connection).to((Void) -> {
      return this;
    });

    return this.loop.runWhenDone(transformedFuture);
  }

  @SuppressWarnings("unchecked")
  @Override
  public CompletableFuture<AsynchronousSocket> write(ByteBuffer buffer) {
    // TODO Add timeout
    Future<Integer> result = this.channel.write(buffer);
    TransformedFuture<Integer, AsynchronousSocket> transformedFuture = TransformedFuture
        .<Integer, AsynchronousSocket> from(result).to((writtenBytes) -> {
          this.sentBytes.update(writtenBytes);
          return this;
        });
    return this.loop.runWhenDone(transformedFuture);
  }

  @Override
  public CompletableFuture<Optional<ByteBuffer>> read() {
    ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
    Future<Integer> result = this.channel.read(buffer);
    TransformedFuture<Integer, Optional<ByteBuffer>> transformedFuture = TransformedFuture
        .<Integer, Optional<ByteBuffer>> from(result).to((readBytes) -> {
          this.receivedBytes.update(readBytes);
          return (readBytes > 0) ? Optional.<ByteBuffer> of(buffer) : Optional.empty();
        });
    return this.loop.runWhenDone(transformedFuture);
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
