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
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import no8.async.AsyncLoop;

import com.codahale.metrics.Meter;

public class AsynchronousServerSocket extends AsynchronousIO<AsynchronousServerSocketChannel> {

  private Meter receivedConn;

  public AsynchronousServerSocket(AsynchronousServerSocketChannel channel, AsyncLoop loop) {
    super(channel, loop);
    this.receivedConn = meter(AsynchronousSocket.class, "connections", "received");
  }

  /**
   * Listen for new connections.
   * 
   * @param localAddress The local address to bind and listen for connections.
   * @throws IOException
   */
  public AsynchronousServerSocket listen(SocketAddress localAddress, Consumer<AsynchronousSocket> connectionHandler)
      throws IOException {
    this.channel.bind(localAddress);
    this.acceptConnection(connectionHandler);
    return this;
  }

  /**
   * Keeps waiting for new connections and dispatching it to the given connection handler.
   */
  private void acceptConnection(Consumer<AsynchronousSocket> connectionHandler) {
    Future<AsynchronousSocketChannel> future = this.channel.accept();
    this.loop.runWhenDone(future).thenAccept((socket) -> {
      this.receivedConn.mark();
      connectionHandler.accept(new AsynchronousSocket(socket, this.loop));
      if (this.loop.isStarted()) {
        this.acceptConnection(connectionHandler);
      }
    });
  }
}
