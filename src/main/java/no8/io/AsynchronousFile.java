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

import no8.async.AsyncLoop;

/**
 * A wrapper to {@link AsynchronousFileChannel} that executes all the operations inside the
 * {@link AsyncLoop}, providing a {@link CompletableFuture} to encapsulate the result.
 */
public class AsynchronousFile extends AsynchronousIO<AsynchronousFileChannel> {

  private AsyncLoop loop;
  private AsynchronousFileChannel fileChannel;

  public AsynchronousFile(AsynchronousFileChannel fileChannel, AsyncLoop loop) {
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
