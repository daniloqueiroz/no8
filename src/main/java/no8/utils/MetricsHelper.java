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
package no8.utils;

import static com.codahale.metrics.MetricRegistry.name;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public final class MetricsHelper {

  private static final MetricRegistry REGISTRY = new MetricRegistry();

  public static void setupJMXReporter() {
    JmxReporter reporter = JmxReporter.forRegistry(REGISTRY).build();
    reporter.start();
  }

  public static MetricRegistry registry() {
    return REGISTRY;
  }

  public static Histogram histogram(Class<?> klass, String... names) {
    return REGISTRY.histogram(name(klass, names));
  }

  public static Histogram histogram(String name, String... names) {
    return REGISTRY.histogram(name(name, names));
  }

  public static Meter meter(Class<?> klass, String... names) {
    return REGISTRY.meter(name(klass, names));
  }

  public static Timer timer(Class<?> klass, String... names) {
    return REGISTRY.timer(name(klass, names));
  }
}
