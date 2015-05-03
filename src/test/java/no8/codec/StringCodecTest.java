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
package no8.codec;

import java.nio.ByteBuffer;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

public class StringCodecTest {

  private static final String RAW_MESSAGE = "some message";
  private static final ByteBuffer ENCODED_MESSAGE = ByteBuffer.wrap(StringCodecTest.RAW_MESSAGE.getBytes());

  StringCodec codec = new StringCodec();

  @Test
  public void encodeStringToByteBuffer() {
    ByteBuffer encoded = this.codec.encode(StringCodecTest.RAW_MESSAGE);
    Assert.assertThat(encoded, CoreMatchers.equalTo(StringCodecTest.ENCODED_MESSAGE));
  }

  @Test
  public void decodeByteBufferToString() {
    String readMessage = this.codec.decode(StringCodecTest.ENCODED_MESSAGE);
    Assert.assertThat(readMessage, CoreMatchers.equalTo(StringCodecTest.RAW_MESSAGE));
  }

  @Test(expected = CodecException.class)
  public void decodeThrowsError() {
    this.codec.decode(null);
  }

  @Test(expected = CodecException.class)
  public void encodeThrowsError() {
    this.codec.encode(null);
  }

}
