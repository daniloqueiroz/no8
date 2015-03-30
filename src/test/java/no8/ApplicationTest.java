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
package no8;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import no8.async.AsyncLoop;

import org.junit.Before;
import org.junit.Test;

public class ApplicationTest {

  private FakeApplication application;
  private AsyncLoop mockLoop;

  @Before
  public void setUp() {
    this.application = new FakeApplication();
    this.mockLoop = mock(AsyncLoop.class);
  }

  @Test(expected = IllegalStateException.class)
  public void getLoopNoLoop() {
    application.loop();
  }

  @Test
  public void setLoopOK() {
    AsyncLoop loop = new AsyncLoop();
    this.application.loop(new AsyncLoop());
    this.application.loop(loop);
    assertThat(application.loop(), is(loop));
  }

  @Test(expected = IllegalStateException.class)
  public void setLoopAlreadyStarted() {
    AsyncLoop loop = new AsyncLoop();
    this.application.loop(loop);
    this.application.start();
    this.application.loop(loop);
  }

  @Test
  public void startStartsLoop() {
    this.application.loop(this.mockLoop);

    this.application.start();

    verify(this.mockLoop).start();
  }

  @Test(expected = IllegalStateException.class)
  public void waitForNotStartedLoopFails() {
    this.application.loop(this.mockLoop);
    this.application.waitFor();
  }

  @Test(expected = IllegalStateException.class)
  public void waitForNoLoopFails() {
    this.application.waitFor();
  }

  @Test
  public void waitFor2Interactions() {
    when(this.mockLoop.isStarted()).thenReturn(true, true, false);
    this.application.loop(this.mockLoop);
    this.application.waitFor();
    verify(this.mockLoop, times(3)).isStarted();
  }
}
