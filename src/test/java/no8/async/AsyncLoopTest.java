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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Test;

public class AsyncLoopTest {

  private AsyncLoop loop;

  @Before
  public void setUp() {
    this.loop = new AsyncLoop();
  }

  @Test
  public void runWhenDoneAddsFutureToQueue() {
    assertThat(this.loop.futuresQueue.size(), equalTo(0));
    CompletableFuture<?> completable = this.loop.runWhenDone(new CompletableFuture<>());
    assertThat(this.loop.futuresQueue.size(), equalTo(1));
    assertThat(completable, notNullValue());

  }

  @Test
  public void blockingAddsFutureToQueue() {
    assertThat(this.loop.futuresQueue.size(), equalTo(0));
    CompletableFuture<Boolean> completable = this.loop.blocking(() -> {
      try {
        Thread.sleep(1000);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return true;
    });
    assertThat(completable, notNullValue());
  }

  @Test(expected=IllegalStateException.class)
  public void startingStartLoop() {
    this.loop.start();
    this.loop.start();
  }

  @Test(expected = IllegalStateException.class)
  public void shutdownNotRunninLoop() {
    this.loop.shutdown();
  }
}
