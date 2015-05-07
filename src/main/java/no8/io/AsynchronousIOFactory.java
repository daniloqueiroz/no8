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

import static no8.utils.ByteUnit.KILOBYTE;
import static no8.utils.ByteUnit.bytes;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;

import no8.application.Config;
import no8.async.AsyncLoop;
import no8.codec.ByteBufferCodec;
import no8.codec.Codec;

public class AsynchronousIOFactory {

  private int bufferSize;
  private Codec<?> codec = new ByteBufferCodec();
  private AsyncLoop loop;

  public AsynchronousIOFactory(AsyncLoop loop) {
    this.loop = loop;
    this.bufferSize((int) bytes(Config.getInt(Config.BYTE_BUFFER_SIZE), KILOBYTE));
  }

  public void bufferSize(int byteBufferSize) {
    this.bufferSize = byteBufferSize;
  }

  public int bufferSize() {
    return this.bufferSize;
  }

  public <T> AsynchronousIOFactory withCodec(Codec<T> codec) {
    this.codec = codec;
    return this;
  }

  /**
   * Creates an {@link AsynchronousFile}.
   * 
   * @see AsynchronousFileChannel#open(Path, OpenOption...)
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <T> AsynchronousFile<T> openFile(Path file, OpenOption options) throws IOException {
    return new AsynchronousFile(AsynchronousFileChannel.open(file, options), this.codec, this.loop, this.bufferSize);
  }

  /**
   * Creates an {@link AsynchronousSocket}
   * 
   * @see AsynchronousSocketChannel#open()
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <T> AsynchronousSocket<T> openSocket() throws IOException {
    return new AsynchronousSocket(AsynchronousSocketChannel.open(), this.codec, this.loop, this.bufferSize);
  }

  /**
   * Creates an {@link AsynchronousServerSocket}
   * 
   * @see AsynchronousServerSocketChannel#open()
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <T> AsynchronousServerSocket<T> openServerSocket() throws IOException {
    return new AsynchronousServerSocket(AsynchronousServerSocketChannel.open(), this.codec, this.loop, this.bufferSize);
  }

}
