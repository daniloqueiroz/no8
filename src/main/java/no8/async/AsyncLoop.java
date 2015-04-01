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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ManagedBlocker;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import no8.Application;

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

  public void shutdown() {
    if (this.started) {
      this.started = false;
      this.pool.shutdown();
    } else {
      throw new IllegalStateException("Loop not started");
    }
  }

  public boolean isStarted() {
    return this.started;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void start() {
    if (!this.started) {
      this.started = true;

      this.pool.execute(() -> {
        while (started) {
          try {
            // TODO use optional / monad
            FutureContainer container = this.futuresQueue.poll(100, TimeUnit.MILLISECONDS);
            if (container != null) {
              if (container.future.isDone()) {
                container.completable.complete(container.future.get());
              } else {
                this.futuresQueue.put(container);
              }
            }
          } catch (Exception e) {
            LOG.error("Unexpected exception on future loop", e);
          }
        }
      });
    } else {
      throw new IllegalStateException("Loop already started");
    }
  }

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

  private class FutureContainer<T> {
    Future<T> future;
    CompletableFuture<T> completable;

    public FutureContainer(Future<T> future, CompletableFuture<T> completable) {
      this.future = future;
      this.completable = completable;
    }
  }

  public void submit(Application application) {
    this.pool.submit(application);
  }
}