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

import static java.lang.Integer.max;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ForkJoinPool;

import no8.application.Config;

import org.pmw.tinylog.Logger;

/**
 * Utility methods to deal with Threads and related (ie. ThreadPool).
 */
public class Threads {

  public static final int POOL_MIN_NUMBER_OF_THREADS = 2;

  /**
   * Creates a new named daemon thread to run the given payload.
   */
  public static Thread createThread(String name, Runnable payload) {
    Thread t = new Thread(payload, name);
    t.setDaemon(true);
    Logger.info("Thread {} created", t.getName());
    return t;
  }

  /**
   * Creates a {@link ForkJoinPool} with the given {@link UncaughtExceptionHandler}. The parallelism
   * for the given Pool is determined by number of available processors or using the
   * {@link Config#WORKER_THREADS} properties - and it's guarantee to be at least
   * {@link Threads#POOL_MIN_NUMBER_OF_THREADS}.
   */
  public static ForkJoinPool createForkJoinPool(UncaughtExceptionHandler exceptionHandler) {
    return new ForkJoinPool(numberOfThreads(), ForkJoinPool.defaultForkJoinWorkerThreadFactory, exceptionHandler, true);
  }

  /**
   * Calculate the number of threads to be create ForkJoinPool.
   */
  private static int numberOfThreads() {
    int numProcessors = Runtime.getRuntime().availableProcessors();
    int threads = Config.getInt(Config.WORKER_THREADS);
    threads = (threads > 0) ? threads : numProcessors;
    threads = max(threads, POOL_MIN_NUMBER_OF_THREADS);
    Logger.info("Number of Threads for AsyncLoopPool: {}", threads);
    return threads;
  }
}
