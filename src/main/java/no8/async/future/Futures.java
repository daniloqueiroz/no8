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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;

public final class Futures {

  /**
   * Transforms a {@link Future} return type.
   */
  public static <T, R> FutureTranformer<TransformedFuture<T, R>, T, R> transform(Future<T> future) {
    return new TransformedFutureBuilder<T, R>(future);
  }

  /**
   * Transforms a {@link CompletableFuture} return type.
   */
  public static <T, R> CompletableFutureBuilder<T, R> transform(CompletableFuture<T> future) {
    return new CompletableFutureBuilder<T, R>(future);
  }

  public static class TransformedFutureBuilder<T, R> implements FutureTranformer<TransformedFuture<T, R>, T, R> {
    protected Future<T> future;

    protected TransformedFutureBuilder(Future<T> future) {
      this.future = future;
    }

    @Override
    public Future<R> using(Function<T, R> transformation) {
      return new TransformedFuture<T, R>(this.future, transformation);
    }
  }

  public static class CompletableFutureBuilder<T, R> implements FutureTranformer<CompletableFuture<R>, T, R> {
    protected CompletableFuture<T> future;

    protected CompletableFutureBuilder(CompletableFuture<T> future) {
      this.future = future;
    }

    @Override
    public CompletableFuture<R> using(Function<T, R> transformation) {
      return this.future.handle((result, ex) -> {
        if (result != null) {
          return transformation.apply(result);
        } else {
          // TODO what happen to this exception ?
          throw new RuntimeException(ex);
        }
      });
    }
  }

}
