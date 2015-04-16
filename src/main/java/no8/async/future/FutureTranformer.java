package no8.async.future;

import java.util.concurrent.Future;
import java.util.function.Function;

public interface FutureTranformer<K extends Future<R>, T, R> {

  public Future<R> using(Function<T, R> transformation);
}
