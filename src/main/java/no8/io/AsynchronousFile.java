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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import no8.async.AsyncLoop;
import no8.async.future.Futures;
import no8.codec.Codec;

/**
 * A wrapper to {@link AsynchronousFileChannel} that executes all the operations inside the
 * {@link AsyncLoop}, providing a {@link CompletableFuture} to encapsulate the result.
 * 
 * @param <T>
 */
public class AsynchronousFile<T> extends AsynchronousIO<AsynchronousFileChannel, T> {

  private AsyncLoop loop;
  private AsynchronousFileChannel fileChannel;
  private int readPosition = 0;
  private int writePosition = 0;

  public AsynchronousFile(AsynchronousFileChannel fileChannel, Codec<T> codec, AsyncLoop loop, int bufferSize) {
    super(fileChannel, codec, loop, bufferSize);
  }

  @Override
  protected CompletableFuture<AsynchronousIO<AsynchronousFileChannel, T>> write(ByteBuffer content) {
    Future<Integer> result = this.fileChannel.write(content, this.writePosition);
    Future<AsynchronousIO<AsynchronousFileChannel, T>> transformedFuture = Futures
        .<Integer, AsynchronousIO<AsynchronousFileChannel, T>> transform(result).using((writtenBytes) -> {
          this.writePosition += writtenBytes;
          return this;
        });
    return this.loop.runWhenDone(transformedFuture);
  }

  public long size() throws IOException {
    return this.fileChannel.size();
  }

  public AsynchronousFileChannel toFileChannel() {
    return this.fileChannel;
  }

  @Override
  protected Future<Integer> read(ByteBuffer buffer) {
    return Futures.<Integer, Integer> transform(this.channel.read(buffer, this.readPosition)).using((read) -> {
      this.readPosition += read;
      return read;
    });
  }
}
