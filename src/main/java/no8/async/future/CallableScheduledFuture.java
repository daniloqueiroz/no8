package no8.async.future;

import static java.time.Instant.now;

import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CallableScheduledFuture<V> implements ScheduledFuture<V> {

  private Callable<V> callable;
  private Instant momentToRun;
  private CountDownLatch lock = new CountDownLatch(1);
  private boolean cancel = false;

  public CallableScheduledFuture(Callable<V> callable, long delay, TemporalUnit unit) {
    this.callable = callable;
    this.momentToRun = now().plus(delay, unit);
  }

  @Override
  public long getDelay(TimeUnit unit) {
    return unit.convert(momentToRun.toEpochMilli() - now().toEpochMilli(), TimeUnit.MILLISECONDS);
  }

  @Override
  public int compareTo(Delayed o) {
    return (int) -(this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    if (this.lock.getCount() != 0)
      this.cancel = true;
    return cancel;
  }

  @Override
  public boolean isCancelled() {
    return this.cancel;
  }

  @Override
  public boolean isDone() {
    return this.lock.getCount() == 0;
  }

  @Override
  public V get() throws InterruptedException, ExecutionException {
    this.lock.await();
    return null;
  }

  @Override
  public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    if (this.lock.await(timeout, unit)) {
      return this.get();
    } else {
      throw new TimeoutException();
    }
  }

  public void tryToComplete() {
    try {

    } finally {
      this.lock.countDown();
    }
  }

}
