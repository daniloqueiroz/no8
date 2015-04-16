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

import static no8.utils.MetricsHelper.meter;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import no8.async.AsyncLoop;
import no8.codec.Codec;

import com.codahale.metrics.Meter;

public class AsynchronousServerSocket<T> extends AsynchronousIO<AsynchronousServerSocketChannel, T> {

  private Meter receivedConn = meter(AsynchronousSocket.class, "connections", "received");

  public AsynchronousServerSocket(AsynchronousServerSocketChannel channel, Codec<T> codec, AsyncLoop loop,
      int bufferSize) {
    super(channel, codec, loop, bufferSize);
  }

  /**
   * Listen for new connections.
   * 
   * @param localAddress The local address to bind and listen for connections.
   * @throws IOException
   */
  public AsynchronousServerSocket<T> listen(SocketAddress localAddress,
      Consumer<AsynchronousSocket<T>> connectionHandler) throws IOException {
    this.channel.bind(localAddress);
    this.acceptConnection(connectionHandler);
    return this;
  }

  /**
   * Keeps waiting for new connections and dispatching it to the given connection handler.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void acceptConnection(Consumer<AsynchronousSocket<T>> connectionHandler) {
    Future<AsynchronousSocketChannel> future = this.channel.accept();
    this.loop.runWhenDone(future).thenAccept((socket) -> {
      this.receivedConn.mark();
      this.loop.submit(() -> {
        connectionHandler.accept(new AsynchronousSocket(socket, this.codec, this.loop, this.bufferSize));
      });
      if (this.loop.isStarted()) {
        this.acceptConnection(connectionHandler);
      }
    });
    // TODO what happen to this future when the connection is closed
  }

  @Override
  protected Future<Integer> read(ByteBuffer buffer) {
    throw new UnsupportedOperationException("Cannot read from a ServerSocket");
  }

  @Override
  protected CompletableFuture<AsynchronousIO<AsynchronousServerSocketChannel, T>> write(ByteBuffer buffer) {
    throw new UnsupportedOperationException("Cannot read from a ServerSocket");
  }
}
