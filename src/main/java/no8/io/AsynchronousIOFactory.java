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
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;

import no8.async.AsyncLoop;

public class AsynchronousIOFactory {

  public static final int DEFAULT_BYTE_BUFFER_SIZE = 512;
  private int bufferSize = DEFAULT_BYTE_BUFFER_SIZE;
  private AsyncLoop loop;

  public AsynchronousIOFactory(AsyncLoop loop) {
    this.loop = loop;
  }

  public void bufferSize(int byteBufferSize) {
    this.bufferSize = (byteBufferSize > 0) ? byteBufferSize : DEFAULT_BYTE_BUFFER_SIZE;
  }

  public int bufferSize() {
    return this.bufferSize;
  }

  /**
   * Creates an {@link AsynchronousFile}.
   * 
   * @see AsynchronousFileChannel#open(Path, OpenOption...)
   */
  public AsynchronousFile openFile(Path file, OpenOption options) throws IOException {
    return new AsynchronousFile(AsynchronousFileChannel.open(file, options), this.loop, this.bufferSize);
  }

  /**
   * Creates an {@link AsynchronousSocket}
   * 
   * @see AsynchronousSocketChannel#open()
   */
  public AsynchronousSocket openSocket() throws IOException {
    return new AsynchronousSocket(AsynchronousSocketChannel.open(), this.loop, this.bufferSize);
  }

  /**
   * Creates an {@link AsynchronousServerSocket}
   * 
   * @see AsynchronousServerSocketChannel#open()
   */
  public AsynchronousServerSocket openServerSocket() throws IOException {
    return new AsynchronousServerSocket(AsynchronousServerSocketChannel.open(), this.loop, this.bufferSize);
  }

}
