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

import java.net.SocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import no8.async.AsyncLoop;
import no8.async.TransformedFuture;

public class AsynchronousSocket extends AsynchronousIO<AsynchronousSocketChannel> {

  public AsynchronousSocket(AsynchronousSocketChannel socketChannel, AsyncLoop loop) {
    super(socketChannel, loop);
  }

  public CompletableFuture<AsynchronousSocket> connect(SocketAddress address) {
    Future<Void> connection = this.channel.connect(address);
    TransformedFuture<Void, AsynchronousSocket> transformedFuture = TransformedFuture.<Void, AsynchronousSocket> from(
        connection).to((Void) -> {
      return this;
    });

    return this.loop.runWhenDone(transformedFuture);
  }
}
