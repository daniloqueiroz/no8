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

import static java.lang.Math.max;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

import no8.application.Config;

import org.junit.Test;

public class ThreadsTest {

  @Test
  public void createThread_returnsDaemonThread() {
    Thread thread = Threads.createThread("", () -> {
      ;
    });
    assertThat(thread.isDaemon()).isTrue();
  }

  @Test
  public void createThreadWithName_returnsNamedThread() {
    String name = "thread-name";
    Thread thread = Threads.createThread(name, () -> {
      ;
    });
    assertThat(thread.getName()).isEqualTo(name);
  }

  @Test
  public void createThreadWithPayload_returnsThreadWithPayload() throws InterruptedException {
    final AtomicBoolean hasExecuted = new AtomicBoolean(false);
    Thread thread = Threads.createThread("", () -> {
      hasExecuted.set(true);
    });
    thread.start();
    thread.join();
    assertThat(hasExecuted.get()).isTrue();
  }

  @Test
  public void createForkJoinPool_rightNumberOfWorkers() {
    ForkJoinPool pool = Threads.createForkJoinPool(null);
    int expected = max(Runtime.getRuntime().availableProcessors(), Threads.POOL_MIN_NUMBER_OF_THREADS);
    assertThat(pool.getParallelism()).isEqualTo(expected);
  }

  @Test
  public void createForkJoinPool_configuredFromSystem_rightNumberOfWorkers() {
    int expected = 10;
    Config.set(Config.WORKER_THREADS, expected);
    ForkJoinPool pool = Threads.createForkJoinPool(null);
    assertThat(pool.getParallelism()).isEqualTo(10);
  }

  @Test
  public void createForkJoinPool_asyncModeTrue() {
    ForkJoinPool pool = Threads.createForkJoinPool(null);
    assertThat(pool.getAsyncMode()).isTrue();
  }

  @Test
  public void createForkJoinPool_withExceptionHandler() {
    UncaughtExceptionHandler exceptionHandler = ((t, e) -> {});
    ForkJoinPool pool = Threads.createForkJoinPool(exceptionHandler);
    assertThat(pool.getUncaughtExceptionHandler()).isEqualTo(exceptionHandler);
  }
}
