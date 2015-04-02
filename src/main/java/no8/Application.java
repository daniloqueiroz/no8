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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import no8.async.AsyncLoop;
import no8.examples.echo.ClientApp;
import no8.examples.echo.ServerApp;
import no8.io.AsynchronousIOFactory;

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

  private static Optional<Application> application = Optional.empty();

  public static final Application currentApplication() {
    return application.orElseThrow(() -> {
      return new IllegalStateException();
    });
  }

  private static final void currentApplication(Application app) {
    if (!application.isPresent()) {
      application = Optional.of(app);
    } else {
      throw new IllegalStateException("There's an Application running already.");
    }
  }

  protected static final void resetCurrentApplication() {
    application = Optional.empty();
  }

  public final AsyncLoop loop;
  public final AsynchronousIOFactory io;

  public Application() {
    this(new AsyncLoop());
  }

  public Application(AsyncLoop asyncLoop) {
    this(asyncLoop, new AsynchronousIOFactory(asyncLoop));
  }

  public Application(AsyncLoop asyncLoop, AsynchronousIOFactory asynchronousIOFactory) {
    this.loop = asyncLoop;
    this.io = asynchronousIOFactory;
    currentApplication(this);
  }

  /**
   * Starts the application {@link AsyncLoop}
   */
  public void start() {
    this.loop.start();
    this.loop.submit(this);
  }

  public void shutdown() {
    this.loop.shutdown();
  }

  /**
   * Waits the application ends.
   */
  public void waitFor() {
    if (this.loop.isStarted()) {
      while (this.loop.isStarted()) {
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
