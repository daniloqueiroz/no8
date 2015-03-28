package no8.async;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * A {@link Future} that encapsulates a given {@link Future} and applies a given {@link Function} to
 * transform it return type from R to T.
 */
public final class TransformedFuture<T, R> implements Future<R> {

  public static <T, R> TransformedFuture.Builder<T, R> from(Future<T> future) {
    return new Builder<T, R>(future);
  }

  public static class Builder<T, R> {
    protected Future<T> future;

    protected Builder(Future<T> future) {
      this.future = future;
    }

    public TransformedFuture<T, R> to(Function<T, R> transformation) {
      return new TransformedFuture<T, R>(this.future, transformation);
    }
  }

  private Function<T, R> transformation;
  private Future<T> future;

  protected TransformedFuture(Future<T> future, Function<T, R> transformation) {
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
}
