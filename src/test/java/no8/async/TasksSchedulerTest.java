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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.temporal.ChronoUnit;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

public class TasksSchedulerTest {

  private TasksScheduler scheduler;

  @Before
  public void setUp() {
    this.scheduler = new TasksScheduler();
    this.scheduler.schedule(100, ChronoUnit.MILLIS, () -> {
    });
    this.scheduler.schedule(1000, ChronoUnit.MILLIS, () -> {
    });
  }

  @Test
  public void scheduleTask() throws InterruptedException {
    Thread.sleep(101);
    Collection<?> toRun = this.scheduler.tasksToRun();
    assertThat(toRun.size(), is(1));
  }

  @Test
  public void cleanScheduledTask() throws InterruptedException {
    Thread.sleep(101);
    this.scheduler.tasksToRun().clear();
    assertTrue(this.scheduler.tasksToRun().isEmpty());
  }
}
