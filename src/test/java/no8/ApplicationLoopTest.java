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
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

public class ApplicationLoopTest {

  @Before
  public void before() {
    Application.resetCurrentApplication();
  }

  @Test
  public void fakeApplicationRuns() throws InterruptedException, InstantiationException, IllegalAccessException {
    Launcher launch = new Launcher(FakeApplication.class, Collections.emptyMap());

    assertThat(((FakeApplication) launch.application).loops(), is(0));

    // kills the test after a while
    new Thread(() -> {
      try {
        Thread.sleep(110);
      } catch (Exception e) {
        e.printStackTrace();
      }
      assertTrue(launch.application.loop.isStarted());
      launch.application.shutdown();
    }).start();

    launch.launch();
    assertThat(((FakeApplication) launch.application).loops(), not(0));
  }
}
