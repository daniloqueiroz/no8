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

import static java.lang.Integer.parseInt;

import java.util.Map;

import no8.async.AsyncLoop;

public class FakeApplication extends Application {

  private Integer sleepMs;
  public boolean run = true;
  private int loops = 0;

  public FakeApplication() {
    super();
  }

  public FakeApplication(AsyncLoop mockLoop) {
    super(mockLoop);
  }

  public int loops() {
    return loops;
  }

  @Override
  public void configure(Map<String, String> parameters) {
    this.sleepMs = parseInt(parameters.getOrDefault("sleepMs", "100"));
  }

  @Override
  public String name() {
    return "Fake Application";
  }

  @Override
  public String helpMessage() {
    return "I'm a FAKE!";
  }

  @Override
  public void run() {
    try {
      while (run) {
        this.loops++;
        System.out.printf("Fake app will sleep %s for secs -> loops: %s\n", sleepMs, loops);
        Thread.sleep(this.sleepMs);
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
