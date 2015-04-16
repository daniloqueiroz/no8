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
package no8.async.future;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * A {@link Future} that encapsulates a given {@link Future} and applies a given {@link Function} to
 * transform it return type from R to T.
 */
final class TransformedFuture<T, R> implements Future<R> {

  private Function<T, R> transformation;
  private Future<T> future;

  TransformedFuture(Future<T> future, Function<T, R> transformation) {
    this.future = future;
    this.transformation = transformation;
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    return this.future.cancel(mayInterruptIfRunning);
  }

  @Override
  public boolean isCancelled() {
    return this.future.isCancelled();
  }

  @Override
  public boolean isDone() {
    return this.future.isDone();
  }

  @Override
  public R get() throws InterruptedException, ExecutionException {
    return this.transformation.apply(this.future.get());
  }

  @Override
  public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    return this.transformation.apply(this.future.get(timeout, unit));
  }

  @Override
  public String toString() {
    return this.future.toString();
  }
}
