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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no8.async.AsyncLoop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Launcher for {@link Application}.
 * 
 * <pre>
 * Life-cycle:
 *  1. Loads the {@link Application} and creates a new instance;
 *  2. Creates a new {@link AsyncLoop} and sets to the application;
 *  3. Calls the {@link Application#configure(List)} with a list of parameters received from the command line;
 *  4. Starts the application;
 *  5. Waits the application loop stops.
 * </pre>
 */
public class Launcher {

  private static final Logger LOG = LoggerFactory.getLogger(Launcher.class);
  protected Application application;
  private Map<String, String> extraParams;

  @SuppressWarnings("unchecked")
  public Launcher(String applicationClassName, Map<String, String> extraParams) throws ClassNotFoundException {
    this((Class<? extends Application>) ClassLoader.getSystemClassLoader().loadClass(applicationClassName), extraParams);
  }

  public Launcher(Class<? extends Application> applicationClass, Map<String, String> extraParams) {
    try {
      this.application = applicationClass.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new IllegalArgumentException(e);
    }
    this.extraParams = extraParams;
  }

  /**
   * Launches the {@link Application}
   */
  public void launch() {
    LOG.info("Lauching application {}", application.name());
    this.application.loop(new AsyncLoop());
    LOG.debug("Application lifecycle configure being executed now.");
    this.application.configure(this.extraParams);
    this.application.start();
    LOG.info("Application {} is running.", application.name());
    this.application.waitFor();
  }

  public static void main(String[] args) {
    if (args.length < 1) {
      System.err.println("Wrong number of parameters.");
      showHelp();
      System.exit(1);
    }
    String param = args[0];

    switch (param) {
    case "--help":
    case "-h":
      showHelp();
      System.exit(0);
      ;
      ;
    default:
      Map<String, String> extraParams = new HashMap<>();
      try {
        new Launcher(param, extraParams).launch();
      } catch (ClassNotFoundException | IllegalArgumentException e) {
        LOG.error("Error launching application", e);
        System.err.printf("Unable to launch application '%s'.\n", param);
      }
    }
  }

  private static void showHelp() {
    String message = "Usage:\n\tjava no8.Launcher <Application Class>\n\t"
        + "ie.: java no8.Launcher no8.example.echo.ServerApp\n";
    System.out.print(message);
  }
}
