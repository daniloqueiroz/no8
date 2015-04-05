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
    Application.resetCurrentApplication();
    this.mockLoop = mock(AsyncLoop.class);
    this.application = new FakeApplication(this.mockLoop);
  }

  @Test
  public void startStartsLoop() {
    this.application.start();

    verify(this.mockLoop).start();
  }

  @Test
  public void shutdownStopsLoop() {
    this.application.shutdown();

    verify(this.mockLoop).shutdown();
  }

  @Test(expected = IllegalStateException.class)
  public void waitForNotStartedLoopFails() {
    this.application.waitFor();
  }

  @Test(expected = IllegalStateException.class)
  public void waitForNoLoopFails() {
    this.application.waitFor();
  }

  @Test
  public void waitFor2Interactions() {
    when(this.mockLoop.isStarted()).thenReturn(true, true, false);
    this.application.waitFor();
    verify(this.mockLoop, times(3)).isStarted();
  }

  @Test(expected = ApplicationException.class)
  public void abortStopsLoopAndCauseWaitForToThrowException() {
    when(this.mockLoop.isStarted()).thenReturn(true);
    new Thread(() -> {
      try {
        Thread.sleep(50);
        this.application.abort("This is a test", new Exception());
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }).start();
    this.application.waitFor();
  }
}
