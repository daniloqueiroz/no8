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
package no8.async;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class AsyncLoop {

  public static Optional<AsyncLoop> loop = Optional.<AsyncLoop> empty();

  /**
   * Gets the current loop.
   * 
   * @throws IllegalStateException if loop wasn't build yet.
   */
  public static AsyncLoop loop() {
    return loop.orElseThrow(() -> {
      return new IllegalStateException();
    });
  }

  private AsyncLoop() {

  }


  public void join() {

  }

  public void shutdown() {

  }

  public <T> CompletableFuture<T> toCompletable(Future<T> future) {
    return null;
  }

  public <T> CompletableFuture<T> blocking() {
    return null;
  }
}
