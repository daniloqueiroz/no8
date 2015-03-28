package no8.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.util.concurrent.CompletableFuture;

import no8.async.ApplicationLoop;

/**
 * A wrapper to {@link AsynchronousFileChannel} that executes all the operations inside the
 * {@link ApplicationLoop}, providing a {@link CompletableFuture} to encapsulate the result.
 */
public class AsynchronousFile extends AbstractAsynchronousIO<AsynchronousFileChannel> {

  private ApplicationLoop loop;
  private AsynchronousFileChannel fileChannel;

  protected AsynchronousFile(AsynchronousFileChannel fileChannel, ApplicationLoop loop) {
    super(fileChannel, loop);
  }

  public CompletableFuture<Integer> write(ByteBuffer buffer, Integer position) {
    return this.loop.toCompletable(this.fileChannel.write(buffer, position));
  }

  public CompletableFuture<Integer> read(ByteBuffer buffer, Integer position) {
    return this.loop.toCompletable(this.fileChannel.read(buffer, position));
  }

  public long size() throws IOException {
    return this.fileChannel.size();
  }

  @Override
  public void close() throws IOException {
    this.fileChannel.close();
  }

  public AsynchronousFileChannel toFileChannel() {
    return this.fileChannel;
  }
}
