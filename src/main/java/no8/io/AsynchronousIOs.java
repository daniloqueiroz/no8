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

import static no8.async.AsyncLoop.loop;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;

/**
 * This class provide Factory Methods for creation of {@link AsynchronousIO} entities.
 */
public class AsynchronousIOs {

  /**
   * Creates an {@link AsynchronousFile}.
   * 
   * @see AsynchronousFileChannel#open(Path, OpenOption...)
   */
  public static AsynchronousFile openFile(Path file, OpenOption options) throws IOException {
    return new AsynchronousFile(AsynchronousFileChannel.open(file, options), loop());
  }

  /**
   * Creates an {@link AsynchronousSocket}
   * 
   * @see AsynchronousSocketChannel#open()
   */
  public static AsynchronousSocket openSocket() throws IOException {
    return new AsynchronousSocket(AsynchronousSocketChannel.open(), loop());
  }

}
