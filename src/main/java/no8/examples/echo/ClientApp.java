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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;

import no8.Application;
import no8.io.AsynchronousSocket;

public class ClientApp extends Application {

  private String host;
  private String port;
  private String msg;

  @Override
  public String name() {
    return "Echo Client";
  }

  @Override
  public void configure(Map<String, String> parameters) {
    this.host = parameters.getOrDefault("host", "localhost");
    this.port = parameters.getOrDefault("port", "9999");
    this.msg = parameters.getOrDefault("message", "echo");
  }

  @Override
  public void run() {
    SocketAddress address = new InetSocketAddress(host, Integer.valueOf(port));
    AsynchronousSocket socket;
    try {
      socket = this.io.openSocket();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    socket.connect(address).thenAccept((s) -> {
      this.connected(s);
    });
  }

  private void connected(AsynchronousSocket socket) {
    socket.write(msg).thenAccept((s) -> {
      this.waitEcho(s);
    });
  }

  private void waitEcho(AsynchronousSocket socket) {
    socket.read().thenAccept((msg) -> {
      System.out.printf("Server reply> %s\n", msg);
      this.shutdown();
    });
  }
}
