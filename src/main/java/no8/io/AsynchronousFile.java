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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import no8.async.AsyncLoop;
import no8.async.future.Futures;

/**
 * A wrapper to {@link AsynchronousFileChannel} that executes all the operations inside the
 * {@link AsyncLoop}, providing a {@link CompletableFuture} to encapsulate the result.
 */
public class AsynchronousFile extends AsynchronousChannelWrapper<AsynchronousFileChannel> implements
    AsynchronousIO<ByteBuffer> {

  private final int BUFFER_SIZE;
  private AsyncLoop loop;
  private AsynchronousFileChannel fileChannel;
  private int readPosition = 0;
  private int writePosition = 0;

  public AsynchronousFile(AsynchronousFileChannel fileChannel, AsyncLoop loop, int bufferSize) {
    super(fileChannel, loop);
    this.BUFFER_SIZE = bufferSize;
  }

  @SuppressWarnings("unchecked")
  @Override
  public CompletableFuture<AsynchronousFile> write(ByteBuffer content) {
    Future<Integer> result = this.fileChannel.write(content, this.writePosition);
    Future<AsynchronousFile> transformedFuture = Futures.<Integer, AsynchronousFile> transform(result).using(
        (writtenBytes) -> {
          this.writePosition += writtenBytes;
          return this;
        });
    return this.loop.runWhenDone(transformedFuture);
  }

  @Override
  public CompletableFuture<Optional<ByteBuffer>> read() {
    ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
    Future<Integer> result = this.channel.read(buffer, this.readPosition);
    Future<Optional<ByteBuffer>> transformedFuture = Futures.<Integer, Optional<ByteBuffer>> transform(result).using(
        (readBytes) -> {
          this.readPosition += readBytes;
          return (readBytes > 0) ? Optional.<ByteBuffer> of(buffer) : Optional.empty();
        });
    return this.loop.runWhenDone(transformedFuture);
  }

  public CompletableFuture<Integer> read(ByteBuffer buffer, Integer position) {
    return this.loop.runWhenDone(this.fileChannel.read(buffer, position));
  }

  public long size() throws IOException {
    return this.fileChannel.size();
  }

  public AsynchronousFileChannel toFileChannel() {
    return this.fileChannel;
  }
}
