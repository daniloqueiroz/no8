/**
 * No8 Copyright (C) 2015 no8.io
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package no8;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

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

  private static final String HELP = "--help";
  private static final Logger LOG = LoggerFactory.getLogger(Launcher.class);
  protected Application application;
  private Map<String, String> extraParams;

  @SuppressWarnings("unchecked")
  public Launcher(String applicationClassName, Map<String, String> extraParams) throws ClassNotFoundException,
      InstantiationException, IllegalAccessException {
    this((Class<? extends Application>) ClassLoader.getSystemClassLoader().loadClass(applicationClassName), extraParams);
  }

  public Launcher(Class<? extends Application> applicationClass, Map<String, String> extraParams)
      throws InstantiationException, IllegalAccessException {
    this(applicationClass.newInstance(), extraParams);
  }

  public Launcher(Application application, Map<String, String> extraParams) {
    this.application = application;
    this.extraParams = extraParams;
  }

  /**
   * Launches the {@link Application}
   */
  public void launch() throws InterruptedException {
    LOG.info("Lauching application {}", application.name());
    this.application.configure(this.extraParams);
    this.application.start();
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      this.application.shutdown();
    }));
    LOG.info("Application {} is up and running.", application.name());
    this.application.waitFor();
  }

  public void applicationHelp() {
    System.out.printf("No8 Application: %s\n\n%s", this.application.name(), this.application.helpMessage());
  }

  public static void main(String[] args) {
    ExitCode code = ExitCode.SUCCESS;
    if (args.length < 1) {
      System.err.println("Wrong number of parameters.");
      showHelp();
      code = ExitCode.WRONG_USAGE;
    } else {
      String param = args[0];

      switch (param) {
      case HELP:
        showHelp();
      default:
        Map<String, String> extraParams = argsToMap(Arrays.asList(args).subList(1, args.length));
        try {
          Launcher launcher = new Launcher(param, extraParams);
          if (extraParams.containsKey(HELP)) {
            launcher.applicationHelp();
          } else {
            launcher.launch();
          }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
          LOG.error("Error launching application", e);
          System.err.printf("Unable to launch application '%s'.\n", param);
          code = ExitCode.APPLICATION_INITIALIZATION_ERROR;
        } catch (ApplicationException e) {
          LOG.error("Application aborted!", e);
          System.err.printf("%s\nQuitting application\n", e.getMessage());
          code = ExitCode.APPLICATION_ABORTED;
        } catch (Throwable e) {
          LOG.error("Unexpected error", e);
          System.err.printf("Unexpected application error: %s\n", e.getMessage());
          code = ExitCode.UNEXPECTED_ERROR;
        }
      }
      System.exit(code.ordinal());
    }
  }

  private static void showHelp() {
    String message = "Usage:\n\tjava no8.Launcher <Application Class>\n\t"
        + "ie.: java no8.Launcher no8.example.echo.ServerApp\n";
    System.out.print(message);
  }

  protected static Map<String, String> argsToMap(List<String> args) {
    // TODO find a more "java8" way of doing this!
    Map<String, String> config = new HashMap<>();
    Queue<String> key = new LinkedList<>();
    args.stream().forEach((arg) -> {
      if (arg.startsWith("--")) {
        if (arg.equals(HELP)) {
          config.put(arg, arg);
        } else {
          key.offer(arg.substring(2));
        }
      } else {
        config.put(key.poll(), arg);
      }
    });
    return config;
  }

  public enum ExitCode {
    SUCCESS, WRONG_USAGE, APPLICATION_INITIALIZATION_ERROR, APPLICATION_ABORTED, UNEXPECTED_ERROR;
  }
}
