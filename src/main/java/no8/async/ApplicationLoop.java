package no8.async;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class ApplicationLoop {

  public static Optional<ApplicationLoop> loop = Optional.<ApplicationLoop> empty();

  /**
   * Gets the current loop.
   * 
   * @throws IllegalStateException if loop wasn't build yet.
   */
  public static ApplicationLoop loop() {
    return loop.orElseThrow(() -> {
      return new IllegalStateException();
    });
  }

  private ApplicationLoop() {

  }


  public void join() {

  }

  public void shutdown() {

  }

  public <T> CompletableFuture<T> toCompletable(Future<T> future) {
    return null;
  }

  public <T> CompletableFuture<T> blocking() {
    return null;
  }
}
