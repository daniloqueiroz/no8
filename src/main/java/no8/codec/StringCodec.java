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
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

/**
 * A simple String to/from {@link ByteBuffer} codec.
 */
public class StringCodec implements Codec<String> {

  public static final String DEFAULT_ENCODING = "utf-8";
  private CharsetEncoder encoder;
  private CharsetDecoder decoder;

  public StringCodec() {
    this(DEFAULT_ENCODING);
  }

  public StringCodec(String encoding) {
    Charset charset = Charset.forName(encoding);
    this.encoder = charset.newEncoder();
    this.decoder = charset.newDecoder();
  }

  @Override
  public ByteBuffer encode(String content) {
    try {
      return this.encoder.encode(CharBuffer.wrap(content));
    } catch (CharacterCodingException | NullPointerException e) {
      throw new CodecException(e);
    }
  }

  @Override
  public String decode(ByteBuffer content) {
    try {
      return this.decoder.decode(content).toString();
    } catch (CharacterCodingException | NullPointerException e) {
      throw new CodecException(e);
    }
  }
}
