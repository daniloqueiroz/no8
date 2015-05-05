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
package no8.io;

import java.nio.channels.AsynchronousChannelGroup;
import java.util.concurrent.ThreadFactory;

import org.pmw.tinylog.Logger;

/**
 * A {@link ThreadFactory} to be used by the system default {@link AsynchronousChannelGroup}
 * 
 * It sets the System settings to use it with initial capacity 1.
 * 
 * All threads from this ThreadFactory are daemon threads and are named using the
 * {@link ChannelGroupThreadFactory#THREAD_NAME_PATERN} pattern.
 */
public class ChannelGroupThreadFactory implements ThreadFactory {

  public static void setup() {
    System.setProperty("java.nio.channels.DefaultThreadPool.initialSize", "1");
    System.setProperty("java.nio.channels.DefaultThreadPool.threadFactory",
        ChannelGroupThreadFactory.class.getCanonicalName());
  }

  private int threadCounter = 0;
  private static final String THREAD_NAME_PATERN = "IOChannelGroup-%s";

  @Override
  public Thread newThread(Runnable target) {
    Thread t = new Thread(target, this.getThreadName());
    t.setDaemon(true);
    Logger.info("IOChannelGroup thread created with name {}", t.getName());
    return t;
  }

  private String getThreadName() {
    return String.format(THREAD_NAME_PATERN, ++this.threadCounter);
  }
}
