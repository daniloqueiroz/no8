package no8.io;

import java.net.SocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import no8.async.ApplicationLoop;
import no8.async.TransformedFuture;

public class AsynchronousSocket extends AbstractAsynchronousIO<AsynchronousSocketChannel> {

  protected AsynchronousSocket(AsynchronousSocketChannel socketChannel, ApplicationLoop loop) {
    super(socketChannel, loop);
  }

  public CompletableFuture<AsynchronousSocket> connect(SocketAddress address) {
    Future<Void> connection = this.channel.connect(address);
    TransformedFuture<Void, AsynchronousSocket> transformedFuture = TransformedFuture.<Void, AsynchronousSocket> from(
        connection).to((Void) -> {
      return this;
    });

    return this.loop.toCompletable(transformedFuture);
  }
}
