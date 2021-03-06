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

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.junit.Before;
import org.junit.Test;

public class MsgPackCodecTest {

  private Message msg;
  private MsgPackCodec<Message> codec;

  @Before
  public void setup() {
    this.codec = new MsgPackCodec<>(Message.class);
    this.msg = new Message();
    msg.text = "Some text";
    msg.number = 10;
  }

  @Test
  public void encodeAndDecode() {
    ByteBuffer buf = this.codec.encode(this.msg);
    buf.rewind();
    Message decoded = this.codec.decode(buf);
    assertThat(this.msg).isEqualTo(decoded);
  }

  @org.msgpack.annotation.Message
  public static class Message {
    public String text;
    public int number;

    @Override
    public boolean equals(Object obj) {
      Message msg = (Message) obj;
      return Objects.equals(msg.text, this.text) && Objects.equals(msg.number, this.number);
    }
  }
}
