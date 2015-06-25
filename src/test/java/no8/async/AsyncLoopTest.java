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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;

public class AsyncLoopTest {

  private AsyncLoop loop;

  @Before
  public void setUp() {
    this.loop = new AsyncLoop();
  }

  @Test
  public void runWhenDone_AddsFutureToQueue() {
    assertThat(this.loop.futuresQueue.size()).isEqualTo(0);
    CompletableFuture<?> completable = this.loop.runWhenDone(new CompletableFuture<>());
    assertThat(this.loop.futuresQueue.size()).isEqualTo(1);
    assertThat(completable).isNotNull();

  }

  @Test
  public void blocking_AddsFutureToQueue() {
    assertThat(this.loop.futuresQueue.size()).isEqualTo(0);
    CompletableFuture<Boolean> completable = this.loop.blocking(() -> {
      try {
        Thread.sleep(1000);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return true;
    });
    assertThat(completable).isNotNull();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void futureThrowsException_CompletableFutureHandles() throws InterruptedException, ExecutionException {
    // Setup future mock
    Future<String> mockFuture = mock(Future.class);
    when(mockFuture.isDone()).thenReturn(true);
    when(mockFuture.get()).thenThrow(new RuntimeException("Ops, i did it again!"));

    // prepare CompletableFuture to check if we manage to handle te exception
    final AtomicBoolean complete = new AtomicBoolean(false);
    CompletableFuture<String> future = this.loop.runWhenDone(mockFuture);
    future.exceptionally((ex) -> {
      // if we receive and exception, we are good!
      complete.set(true);
      synchronized (this) {
        this.notify();
      }
      return ex.getMessage();
      }).thenAccept(something -> fail("Future should be broken")); // we failed!

    // Starts loop to process future and go to sleep a bit
    this.loop.start();
    synchronized (this) {
      this.wait(50);
    }
    this.loop.shutdown();

    assertThat(complete.get()).isTrue();
  }
}
