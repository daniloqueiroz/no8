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

  JMX_REPORTER("jmxMetricReporter", "true"),
  WORKER_THREADS("workerThreads", "0"),
  BYTE_BUFFER_SIZE("bufferSizeKB", "512");

  final String propertyName;
  final String defaultValue;

  private Config(String systemPropertyName, String defaultValue) {
    this.propertyName = systemPropertyName;
    this.defaultValue = defaultValue;
  }

  public static void set(Config configuration, Object value) {
    System.setProperty(configuration.propertyName, String.valueOf(value));
  }

  public static String get(Config configuration) {
    return System.getProperty(configuration.propertyName, configuration.defaultValue);
  }

  public static Boolean getBoolean(Config configuration) {
    return Boolean.parseBoolean(System.getProperty(configuration.propertyName, configuration.defaultValue));
  }

  public static Integer getInt(Config configuration) {
    return Integer.parseInt(System.getProperty(configuration.propertyName, configuration.defaultValue));
  }

  public static String dumpConfig() {
    StringBuilder buf = new StringBuilder();
    for (Config cfg : Config.values()) {
      buf.append(cfg.propertyName);
      buf.append(": ");
      buf.append(Config.get(cfg));
      buf.append("; ");
    }
    return buf.toString();
  }
}
