package no8.async.future;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

public class CallableScheduledFutureTest {

  @Test
  public void getDelay() throws InterruptedException {
    CallableScheduledFuture<Void> future = new CallableScheduledFuture<>(() -> {
      return null;
    }, 10, ChronoUnit.MINUTES);
    assertTrue(future.getDelay(TimeUnit.MINUTES) > 9);
  }

  @Test
  public void compareFutures() {
    CallableScheduledFuture<Void> early = new CallableScheduledFuture<>(() -> {
      return null;
    }, 90, ChronoUnit.SECONDS);
    CallableScheduledFuture<Void> late = new CallableScheduledFuture<>(() -> {
      return null;
    }, 100, ChronoUnit.SECONDS);
    assertTrue(late.compareTo(early) < 0);
    assertThat(early.compareTo(early), equalTo(0));
    assertTrue(early.compareTo(late) > 0);
  }

  @Test
  public void isDone_notDone() {
    CallableScheduledFuture<Void> future = new CallableScheduledFuture<>(() -> {
      return null;
    }, 10, ChronoUnit.MINUTES);
    assertFalse(future.isDone());
  }

  @Test
  public void isDone_done() {
    CallableScheduledFuture<Void> future = new CallableScheduledFuture<>(() -> {
      return null;
    }, 1, ChronoUnit.MILLIS);
    future.tryToComplete();
    assertTrue(future.isDone());
  }

  @Test
  public void isCancelled_notCancelled() {
    CallableScheduledFuture<Void> future = new CallableScheduledFuture<>(() -> {
      return null;
    }, 10, ChronoUnit.MINUTES);
    assertFalse(future.isCancelled());
  }

  @Test
  public void isCancelled_cancelled() {
    CallableScheduledFuture<Void> future = new CallableScheduledFuture<>(() -> {
      return null;
    }, 10, ChronoUnit.MINUTES);
    assertTrue(future.cancel(true));
    assertTrue(future.isCancelled());
  }

  @Test
  public void cancel_doneTask() {
    CallableScheduledFuture<Void> future = new CallableScheduledFuture<>(() -> {
      return null;
    }, 10, ChronoUnit.MINUTES);
    future.tryToComplete();
    assertFalse(future.cancel(true));
  }

  @Test
  public void get_testTimeout() throws InterruptedException, ExecutionException {
    CallableScheduledFuture<Void> future = new CallableScheduledFuture<>(() -> {
      return null;
    }, 10, ChronoUnit.MINUTES);
    Thread t = new Thread(() -> {
      try {
        future.get();
      } catch (InterruptedException e) {
        // OK
      } catch (Exception e) {
        fail(e.getMessage());
      }
    });
    t.start();
    Thread.sleep(5);
    assertTrue(t.isAlive());
    t.interrupt();
  }

  @Test(expected = TimeoutException.class)
  public void get_timeout() throws InterruptedException, ExecutionException, TimeoutException {
    CallableScheduledFuture<Void> future = new CallableScheduledFuture<>(() -> {
      return null;
    }, 10, ChronoUnit.MINUTES);
    future.get(5, TimeUnit.MILLISECONDS);
  }

}
