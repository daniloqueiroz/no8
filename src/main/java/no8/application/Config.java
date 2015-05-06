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
package no8.application;

public enum Config {

  JMX_REPORTER("jmxMetricReporter", "true"), WORKER_THREADS("workerThreads", "0");

  private String name;
  private String defaultValue;

  private Config(String systemPropertyName, String defaultValue) {
    this.name = systemPropertyName;
    this.defaultValue = defaultValue;
  }

  public static final String get(Config configuration) {
    return System.getProperty(configuration.name, configuration.defaultValue);
  }

  public static final Boolean getBoolean(Config configuration) {
    return Boolean.parseBoolean(System.getProperty(configuration.name, configuration.defaultValue));
  }

  public static final Integer getInt(Config configuration) {
    return Integer.parseInt(System.getProperty(configuration.name, configuration.defaultValue));
  }
}
