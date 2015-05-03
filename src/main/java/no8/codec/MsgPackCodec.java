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

import java.io.IOException;
import java.nio.ByteBuffer;

import org.msgpack.MessagePack;

/**
 * @param <T>
 */
public class MsgPackCodec<T> implements Codec<T> {

  private Class<T> clazz;
  private MessagePack messagePack;

  public MsgPackCodec(Class<T> clazz) {
    this.clazz = clazz;
    this.messagePack = new MessagePack();
  }

  @Override
  public ByteBuffer encode(T content) {
    try {
      return ByteBuffer.wrap(this.messagePack.write(content));
    } catch (IOException e) {
      throw new CodecException(e);
    }
  }

  @Override
  public T decode(ByteBuffer buffer) {
    try {
      return this.messagePack.read(buffer, this.clazz);
    } catch (IOException e) {
      throw new CodecException(e);
    }
  }

}
