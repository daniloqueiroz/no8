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

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.AsynchronousChannel;
import java.util.concurrent.CompletableFuture;

import no8.async.AsyncLoop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AsynchronousIO entities encapsulate the usage of java.nio.channels {@link AsynchronousChannel}.
 * 
 * {@link AsynchronousIO} objects returns a {@link CompletableFuture} and executes using the
 * {@link AsyncLoop#loop()}.
 */
public class AsynchronousIO<T extends AsynchronousChannel> implements Closeable {

  protected static final Logger LOG = LoggerFactory.getLogger(AsynchronousIO.class);

  protected AsyncLoop loop;
  protected T channel;

  protected AsynchronousIO(T channel, AsyncLoop loop) {
    this.channel = channel;
    this.loop = loop;
  }

  @Override
  public void close() {
    try {
      this.channel.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Gets the {@link AsynchronousChannel} wrapped by this {@link AsynchronousIO} object.
   */
  public T toAsynchronousChannel() {
    return this.channel;
  }

}