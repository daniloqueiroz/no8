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
import static java.lang.Integer.parseInt;
import static no8.utils.MetricsHelper.histogram;
import static no8.utils.MetricsHelper.timer;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ManagedBlocker;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.pmw.tinylog.Logger;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;

public class AsyncLoop {

  private static final String NUMBER_OF_THREADS = "numberOfThreads";
  private static final int MIN_NUMBER_OF_THREADS = 2;

  protected ForkJoinPool pool;
  private UncaughtExceptionHandler errorHandler = (tt, ee) -> {
    Logger.error(ee, "Uncaught Exception from thread {}", tt);
  };

  private boolean started = false;

  protected BlockingQueue<FutureContext<?>> futuresQueue = new LinkedBlockingQueue<>();

  private Histogram freeMem = histogram("JVM", "memory", "total");
  private Histogram totalMem = histogram("JVM", "memory", "free");;
  private Histogram queueSize = histogram(AsyncLoop.class, "future", "queue", "size");
  private Histogram poolQueueSize = histogram(AsyncLoop.class, "pool", "queue", "size");
  private Histogram poolActiveThreads = histogram(AsyncLoop.class, "pool", "threads", "size");
  private Histogram activeThreads = histogram("JVM", "threads", "active");
  private Timer blockingTime = timer(AsyncLoop.class, "time", "blocking");
  private Timer nonblockingTime = timer(AsyncLoop.class, "time", "non-blocking");

  public AsyncLoop() {
    UncaughtExceptionHandler wrapper = ((t, e) -> {
      this.errorHandler.uncaughtException(t, e);
    });

    int threads = numberOfThreads();

    this.pool = new ForkJoinPool(threads, ForkJoinPool.defaultForkJoinWorkerThreadFactory, wrapper, true);
  }

  /**
   * Calculate the number of threads to be create ForkJoinPool.
   */
  private int numberOfThreads() {
    int numProcessors = Runtime.getRuntime().availableProcessors();
    int threads = parseInt(System.getProperty(NUMBER_OF_THREADS, "-1"));
    threads = (threads > 0) ? threads : numProcessors;
    threads = max(threads, MIN_NUMBER_OF_THREADS);
    Logger.info("Number of Threads for AsyncLoopPool: {}", threads);
    return threads;
  }

  /**
   * Set the handler for uncaught exceptions.
   * 
   * @throws AssertionError
   *           if errorHandler is null.
   */
  public void exceptionHandler(UncaughtExceptionHandler errorHandler) {
    assert errorHandler != null;
    this.errorHandler = errorHandler;
  }

  /**
   * Submit the given {@link Runnable} for execution.
   */
  public ForkJoinTask<?> submit(Runnable runnable) {
    return this.pool.submit(runnable);
  }

  /**
   * Enqueue a future to be executed when it's done.
   * 
   * It returns a {@link CompletableFuture} that can be used to apply functions/operations to be
   * executed when the time arrives.
   */
  public <T> CompletableFuture<T> runWhenDone(Future<T> future) {
    CompletableFuture<T> completable = new CompletableFuture<>();
    try {
      this.futuresQueue.put(new FutureContext<>(future, completable, this.nonblockingTime.time()));
    } catch (InterruptedException e) {
      Logger.error(e, "Unexpected error adding future to AsyncLoop");
      throw new RuntimeException(e);
    }

    return completable;
  }

  /**
   * Executes the given blocking code in an asynchronous fashion.
   * 
   * It returns a {@link CompletableFuture} that can be used to apply functions/operations to be
   * executed when the time arrives.
   */
  public <T> CompletableFuture<T> blocking(Supplier<T> synchronousFunction) {
    final CompletableFuture<T> future = new CompletableFuture<>();
    try {
      ForkJoinPool.managedBlock(new ManagedBlocker() {
        AtomicBoolean lock = new AtomicBoolean(false);

        @Override
        public boolean isReleasable() {
          return lock.get();
        }

        @Override
        public boolean block() throws InterruptedException {
          try (Timer.Context context = blockingTime.time()) {
            if (!this.lock.get()) {
              future.complete(synchronousFunction.get());
              this.lock.set(true);
            }
            return this.lock.get();
          }
        }
      });
    } catch (InterruptedException e) {
      Logger.error(e, "Error while executing blocking task");
      throw new RuntimeException(e);
    }

    return future;
  }

  /**
   * Shutdown the loop. If loop not running, not happens.
   */
  public void shutdown() {
    if (this.started) {
      this.started = false;
      this.pool.shutdown();
    }
  }

  public boolean isStarted() {
    return this.started;
  }

  /**
   * Starts the loop- if loop is already running, nothing happens.
   * 
   * **Internals notes**:
   * 
   * Starting the loop means setting up a loop that will take care process Futures as soon as they
   * get completed and starting the internal {@link ExecutorService}.
   */
  public void start() {
    if (!this.started) {
      this.started = true;
      this.submit(() -> {
        // Future loop
        while (started) {
          updateMetrics();
          tryProcessFuture(this.pollFutures());
        }
      });
    }
  }

  private void updateMetrics() {
    Runtime runtime = Runtime.getRuntime();
    this.freeMem.update(runtime.freeMemory());
    this.totalMem.update(runtime.totalMemory());
    this.queueSize.update(this.futuresQueue.size());
    this.activeThreads.update(Thread.activeCount());
    this.poolActiveThreads.update(this.pool.getRunningThreadCount());
    this.poolQueueSize.update(this.pool.getQueuedSubmissionCount());
  }

  /**
   * If the given {@link FutureContext} is done, it submits another task to process the result.
   */
  private <T> void tryProcessFuture(Optional<FutureContext<T>> container) {
    container.map(c -> c.future).ifPresent(future -> {
      if (future.isDone()) {
        dispatchFutureResult(container.get());
      } else {
        try {
          this.futuresQueue.put(container.get());
        } catch (InterruptedException e) {
          Logger.error(e, "Unable to re-enqueue unfinished Future");
        }
      }
    });
  }

  @SuppressWarnings("unchecked")
  private <T> Optional<FutureContext<T>> pollFutures() {
    Optional<FutureContext<T>> future = Optional.empty();
    try {
      FutureContext<T> container = (FutureContext<T>) this.futuresQueue.poll(100, TimeUnit.MILLISECONDS);
      future = (container != null) ? Optional.of(container) : Optional.empty();
    } catch (InterruptedException e) {
      Logger.warn(e, "Someone has interrupted futuresQueue Pool");
    }
    return future;
  }

  private <T> void dispatchFutureResult(FutureContext<T> container) {
    // Submit the dispatching to be executed separately, avoiding blocking future consumer
    this.submit(() -> {
      try {
        container.completable.complete(container.future.get());
      } catch (Exception e) {
        container.completable.completeExceptionally(e);
      } finally {
        container.timeContext.close();
      }
    });
  }

  /**
   * A simple wrapper class for futures
   */
  private class FutureContext<T> {
    Future<T> future;
    CompletableFuture<T> completable;
    Context timeContext;

    public FutureContext(Future<T> future, CompletableFuture<T> completable, Timer.Context timeContext) {
      this.future = future;
      this.completable = completable;
      this.timeContext = timeContext;
    }
  }
}