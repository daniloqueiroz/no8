package no8.io;

import static no8.async.ApplicationLoop.loop;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;

public class AsynchonousIO {

  public static AsynchronousFile openFile(Path file, OpenOption options) throws IOException {
    return new AsynchronousFile(AsynchronousFileChannel.open(file, options), loop());
  }

  public static AsynchronousSocket openConnection() throws IOException {
    return new AsynchronousSocket(AsynchronousSocketChannel.open(), loop());
  }
}
