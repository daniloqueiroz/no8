/**
 * No8 Copyright (C) 2015 no8.io
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package no8.application;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no8.application.Application;
import no8.application.Launcher;

import org.junit.Before;
import org.junit.Test;

public class LauncherTest {

  @Before
  public void before() {
    Application.resetCurrentApplication();
  }

  @Test
  public void createsLauncherWithClassName() throws ClassNotFoundException, InstantiationException,
      IllegalAccessException {
    Launcher l = new Launcher("no8.FakeApplication", Collections.emptyMap());
    assertThat(l.application, notNullValue());
  }

  @Test
  public void createsLauncherWithClass() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    Launcher l = new Launcher(FakeApplication.class, Collections.emptyMap());
    assertThat(l.application, notNullValue());
  }

  @Test(expected = InstantiationException.class)
  public void createsLauncherWithInvalidClass() throws ClassNotFoundException, InstantiationException,
      IllegalAccessException {
    new Launcher(Application.class, Collections.emptyMap());
  }

  @Test
  public void launcherSetupsApplication() throws ClassNotFoundException, InstantiationException,
      IllegalAccessException, InterruptedException {
    Application mockApp = mock(Application.class);
    Map<String, String> extraParams = new HashMap<>();
    extraParams.put("key", "value");
    Launcher l = new Launcher(mockApp, extraParams);
    l.application = mockApp;
    when(mockApp.name()).thenReturn("MockApp");

    l.launch();

    verify(mockApp).configure(extraParams);
    verify(mockApp).start();
    verify(mockApp).waitFor();
  }

  @Test
  public void parametersListToMap() {
    List<String> args = Arrays.asList("--param1", "value1", "--param2", "value2");

    Map<String, String> config = Launcher.argsToMap(args);

    assertEquals("value1", config.get("param1"));
    assertEquals("value2", config.get("param2"));
  }
}
