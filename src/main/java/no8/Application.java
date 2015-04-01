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
package no8;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no8.async.AsyncLoop;
import no8.examples.echo.ClientApp;
import no8.examples.echo.ServerApp;
import no8.io.AsynchronousFile;
import no8.io.AsynchronousSocket;

/**
 * Base class for No8 applications.
 * 
 * To create a No8 application, you should extend this class and implement the
 * {@link Application#name()} and {@link Application#configure(List)} methods.
 * 
 * The applications life-cycle are managed by the {@link Launcher}.
 * 
 * @see ServerApp
 * @see ClientApp
 */
public abstract class Application implements Runnable {

  protected Optional<AsyncLoop> loop = Optional.empty();

  /**
   * Gets the {@link AsyncLoop} for this application.
   */
  public AsyncLoop loop() {
    return this.loop.orElseThrow(() -> {
      return new IllegalStateException("Loop not set yet.");
    });
  }

  /**
   * Sets the application {@link AsyncLoop}.
   * 
   * This method is called by the {@link Launcher}.
   * 
   * @throws IllegalStateException if trying to set the loop when the current {@link AsyncLoop} is
   * already started.
   */
  public void loop(AsyncLoop asyncLoop) {
    if (!this.loop.isPresent() || !this.loop().isStarted()) {
      this.loop = Optional.of(asyncLoop);
    } else {
      throw new IllegalStateException("Loop already set.");
    }
  }

  /**
   * Starts the application {@link AsyncLoop}
   */
  public void start() {
    this.loop().start();
    this.loop().submit(this);
  }

  public void shutdown() {
    this.loop().shutdown();
  }

  /**
   * Waits the application ends.
   */
  public void waitFor() {
    if (this.loop().isStarted()) {
      while (this.loop().isStarted()) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    } else {
      throw new IllegalStateException("Loop is not started.");
    }
  }

  @Override
  public String toString() {
    return this.name();
  }

  /**
   * Creates an {@link AsynchronousFile}.
   * 
   * @see AsynchronousFileChannel#open(Path, OpenOption...)
   */
  public AsynchronousFile openFile(Path file, OpenOption options) throws IOException {
    return new AsynchronousFile(AsynchronousFileChannel.open(file, options), this.loop());
  }

  /**
   * Creates an {@link AsynchronousSocket}
   * 
   * @param address
   * 
   * @see AsynchronousSocketChannel#open()
   */
  public AsynchronousSocket openSocket() throws IOException {
    return new AsynchronousSocket(AsynchronousSocketChannel.open(), this.loop());
  }

  /**
   * The application name.
   */
  public abstract String name();

  /**
   * Setup this application.
   * 
   * @param parameters
   */
  public abstract void configure(Map<String, String> parameters);
}
