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

import static java.lang.Math.pow;
public enum ByteUnit {

  BYTE(0), KILOBYTE(1), MEGABYTE(2), GIGABYTE(3);

  private long bytes;

  private ByteUnit(int exp) {
    this.bytes = (long) pow(1024, exp);
  }

  public long to(ByteUnit unit) {
    return this.bytes / unit.bytes;
  }

  public static double bytes(double amount, ByteUnit unit) {
    return (unit.to(BYTE) * amount);
  }
}
