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
package no8.examples.echo;

import static java.lang.String.format;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

import no8.Application;
import no8.io.AsynchronousServerSocket;
import no8.io.AsynchronousSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerApp extends Application {

  private static final Logger LOG = LoggerFactory.getLogger(ClientApp.class);

  public static final String DEFAULT_HOST = "127.0.0.1";
  public static final String DEFAULT_PORT = "9999";
  private InetSocketAddress address;

  @Override
  public String name() {
    return "Echo Server";
  }

  @Override
  public String helpMessage() {
    String message = format("Listen for connections and replies with same received message." + "\nParams: \n\t"
        + "--host: server host (default '%s')\n\t" + "--port: server port(default '%s')", DEFAULT_HOST, DEFAULT_PORT);
    return message;
  }

  @Override
  public void configure(Map<String, String> parameters) {
    String host = parameters.getOrDefault("host", ServerApp.DEFAULT_HOST);
    String port = parameters.getOrDefault("port", ServerApp.DEFAULT_PORT);
    address = new InetSocketAddress(host, Integer.valueOf(port));
  }

  @Override
  public void run() {
    AsynchronousServerSocket serverSocket;

    try {
      serverSocket = this.io.openServerSocket();
      LOG.info("Listen at {}, waiting connections.", this.address);
      serverSocket.listen(this.address, (socket) -> {
        LOG.info("Connection received from {}", socket.remoteAddress());
        this.handleSocket(socket);
      });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void handleSocket(AsynchronousSocket socket) {
    socket.read().thenAccept((message) -> {
      if (message.isPresent()) {
        LOG.info("Server received message: {}", message.get());
        socket.write(message.get()).thenAccept((s) -> {
          handleSocket(s);
        });
      } else {
        LOG.info("EOS received, closing socket.");
        socket.close();
      }
    });
  }

}
