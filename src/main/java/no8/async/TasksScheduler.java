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
package no8.async;

import static java.time.Instant.now;

import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TasksScheduler {

  private SortedMap<Instant, Runnable> tasks = new TreeMap<>();
  private Lock lock = new ReentrantLock();

  public void schedule(long delta, TemporalUnit unit, Runnable function) {
    this.schedule(now().plus(delta, unit), function);
  }

  public void schedule(Instant timeToRun, Runnable function) {
    lock.lock();
    try {
      this.tasks.put(timeToRun, function);
    } finally {
      lock.unlock();
    }
  }

  public Collection<Runnable> tasksToRun() {
    lock.lock();
    try {
      return this.tasks.headMap(Instant.now()).values();
    } finally {
      lock.unlock();
    }
  }

}
