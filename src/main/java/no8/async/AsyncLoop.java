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

import static java.lang.String.format;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncLoop {

  private static final Logger LOG = LoggerFactory.getLogger(AsyncLoop.class);

  protected ForkJoinPool pool;
  private UncaughtExceptionHandler errorHandler = (tt, ee) -> {
    LOG.error(format("Uncaught Exception from thread %s", tt), ee);
  };

  private boolean started = false;

  protected BlockingQueue<FutureContainer<?>> futuresQueue = new LinkedBlockingQueue<>();

  public AsyncLoop() {
    UncaughtExceptionHandler wrapper = ((t, e) -> {
      this.errorHandler.uncaughtException(t, e);
    });

    this.pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors(),
        ForkJoinPool.defaultForkJoinWorkerThreadFactory, wrapper, true);
  }

  /**
   * Set the handler for uncaught exceptions.
   * 
   * @throws AssertionError if errorHandler is null.
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
      this.futuresQueue.put(new FutureContainer<>(future, completable));
    } catch (InterruptedException e) {
      LOG.error("Unexpected error adding future to AsyncLoop", e);
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
          if (!this.lock.get()) {
            future.complete(synchronousFunction.get());
            this.lock.set(true);
          }
          return this.lock.get();
        }
      });
    } catch (InterruptedException e) {
      LOG.error("Error while executing blocking task", e);
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
          tryProcessFuture(this.pollFutures());
        }
      });
    }
  }

  /**
   * If the given {@link FutureContainer} is done, it submits another task to process the result.
   */
  private <T> void tryProcessFuture(Optional<FutureContainer<T>> container) {
    Optional<CompletableFuture<T>> completableOpt = container.map(c -> c.completable);
    container.map(c -> c.future).ifPresent(future -> {
      if (future.isDone()) {
        dispatchFutureResult(future, completableOpt.get());
      } else {
        try {
          this.futuresQueue.put(container.get());
        } catch (InterruptedException e) {
          LOG.error("Unable to re-enqueue unfinished Future", e);
        }
      }
    });
  }

  @SuppressWarnings("unchecked")
  private <T> Optional<FutureContainer<T>> pollFutures() {
    Optional<FutureContainer<T>> future = Optional.empty();
    try {
      FutureContainer<T> container = (FutureContainer<T>) this.futuresQueue.poll(100, TimeUnit.MILLISECONDS);
      future = (container != null) ? Optional.of(container) : Optional.empty();
    } catch (InterruptedException e) {
      LOG.warn("Someone has interrupted futuresQueue Pool", e);
    }
    return future;
  }

  private <T> void dispatchFutureResult(Future<T> future, CompletableFuture<T> completable) {
    // Submit the dispatching to be executed separately, avoiding blocking future consumer
    this.submit(() -> {
      try {
        completable.complete(future.get());
      } catch (Exception e) {
        completable.completeExceptionally(e);
      }
    });
  }

  private class FutureContainer<T> {
    Future<T> future;
    CompletableFuture<T> completable;

    public FutureContainer(Future<T> future, CompletableFuture<T> completable) {
      this.future = future;
      this.completable = completable;
    }
  }
}