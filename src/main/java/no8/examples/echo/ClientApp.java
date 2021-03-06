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

import org.pmw.tinylog.Logger;

import no8.application.Application;
import no8.codec.StringCodec;
import no8.io.AsynchronousSocket;


public class ClientApp extends Application {

  private String msg;
  private InetSocketAddress address;

  @Override
  public String name() {
    return "Echo Client";
  }

  @Override
  public String helpMessage() {
    String message = format("Connect to server, send a message and wait its reply." + "\nParams: \n\t"
        + "--host: server host (default '%s')\n\t" + "--port: server port(default '%s')\n\t"
        + "--message: message to send to server (default 'hi there')", ServerApp.DEFAULT_HOST, ServerApp.DEFAULT_PORT);
    return message;
  }

  @Override
  public void configure(Map<String, String> parameters) {
    String host = parameters.getOrDefault("host", ServerApp.DEFAULT_HOST);
    String port = parameters.getOrDefault("port", ServerApp.DEFAULT_PORT);
    address = new InetSocketAddress(host, Integer.valueOf(port));
    this.msg = parameters.getOrDefault("message", "hi there!");
    this.io.withCodec(new StringCodec());
  }

  @Override
  public void run() {
    AsynchronousSocket<String> socket;
    try {
      socket = this.io.openSocket();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    socket.connect(this.address).thenAccept((s) -> {
      Logger.info("Connected to server {}", this.address);
      this.connected(s);
    }).exceptionally((e) -> {
      this.abort("Unable to connect to server", e);
      return null; // wtf Java?
    });
  }

  private void connected(AsynchronousSocket<String> socket) {
    Logger.info("Sending message '{}' to server", this.msg);
    // TODO codec wrap
    socket.write(this.msg).thenAccept((s) -> {
      this.waitResponse((AsynchronousSocket<String>) s);
    });
  }

  private void waitResponse(AsynchronousSocket<String> socket) {
    socket.read().thenAccept((message) -> {
      System.out.printf("Server reply> %s\n", message.get());
      this.shutdown();
    });
  }
}
