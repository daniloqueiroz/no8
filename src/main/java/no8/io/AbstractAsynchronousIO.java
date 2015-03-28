package no8.io;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.AsynchronousChannel;

import no8.async.ApplicationLoop;

/**
 * Base wrapper class AsynchronousIO
 */
public class AbstractAsynchronousIO<T extends AsynchronousChannel> implements Closeable {

  protected ApplicationLoop loop;
  protected T channel;

  protected AbstractAsynchronousIO(T channel, ApplicationLoop loop) {
    this.channel = channel;
    this.loop = loop;
  }

  @Override
  public void close() throws IOException {
    this.channel.close();
  }

  public T toAsynchronousChannel() {
    return this.channel;
  }
}