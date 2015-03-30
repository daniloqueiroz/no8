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

  @Override
  public String name() {
    return "Echo Client";
  }

  @Override
  public void configure(Map<String, String> parameters) {
    String host = (parameters.size() >= 1) ? parameters.get(0) : "localhost";
    String port = (parameters.size() >= 2) ? parameters.get(1) : "9999";
    String msg = (parameters.size() >= 3) ? parameters.get(2) : "message";

    AsynchronousSocket socket;
    try {
       socket = this.openSocket();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    
    SocketAddress address = new InetSocketAddress(host, Integer.valueOf(port));
    socket.connect(address).thenAccept((s) -> {
      s.write(msg);
    });
  }

}
