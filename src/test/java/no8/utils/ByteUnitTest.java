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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ByteUnitTest {

  @Test
  public void byteToByte() {
    assertThat(ByteUnit.BYTE.to(ByteUnit.BYTE)).isEqualTo(1L);
  }

  @Test
  public void kilobyteToByte() {
    assertThat(ByteUnit.KILOBYTE.to(ByteUnit.BYTE)).isEqualTo(1024L);
  }

  @Test
  public void megabyteToKilobyte() {
    assertThat(ByteUnit.MEGABYTE.to(ByteUnit.KILOBYTE)).isEqualTo(1024L);
  }

  @Test
  public void gigabyteToMegabyte() {
    assertThat(ByteUnit.GIGABYTE.to(ByteUnit.MEGABYTE)).isEqualTo(1024L);
  }

  @Test
  public void toBytes() {
    assertThat(ByteUnit.bytes(0.5, ByteUnit.KILOBYTE)).isEqualTo(512d);
  }
}
