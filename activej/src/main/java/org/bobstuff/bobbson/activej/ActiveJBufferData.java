package org.bobstuff.bobbson.activej;

import io.activej.bytebuf.ByteBuf;
import java.nio.charset.StandardCharsets;
import org.bobstuff.bobbson.*;
import org.bobstuff.bobbson.buffer.BobBsonBuffer;

public class ActiveJBufferData implements BobBsonBuffer {
  private ByteBuf buffer;
  private int start;
  public BobBsonByteRange byteRange;
  public byte[] data;

  public ActiveJBufferData(byte[] data) {
    this(data, 0, data.length);
  }

  public ActiveJBufferData(byte[] data, int start) {
    this(data, start, data.length);
  }

  public ActiveJBufferData(byte[] data, int start, int tail) {
    this.data = data;
    buffer = ByteBuf.wrap(data, start, tail);
    this.byteRange = new BobBsonByteRange(data);
  }

  public ActiveJBufferData(ByteBuf buffer) {
    this.data = buffer.array();
    this.buffer = buffer;
    this.byteRange = new BobBsonByteRange(this.data);
  }

  public void process(byte[] data, int start, int tail) {
    buffer = ByteBuf.wrap(data, start, tail);
    byteRange.setData(data);
  }

  public ByteBuf getByteBuf() {
    return buffer;
  }

  @Override
  public byte[] getBytes(int size) {
    byte[] subBuffer = new byte[size];
    buffer.read(subBuffer);
    return subBuffer;
  }

  @Override
  public ByteRangeComparator getByteRangeComparator() {
    return byteRange;
  }

  @Override
  public byte[] toByteArray() {
    var array = buffer.getArray();
    if (array == null) {
      throw new RuntimeException("Cant access backing array of BobBsonBuffer");
    }
    return array;
  }

  @Override
  public int getInt() {
    return Integer.reverseBytes(buffer.readInt());
  }

  @Override
  public byte getByte() {
    return buffer.readByte();
  }

  @Override
  public long getLong() {
    return Long.reverseBytes(buffer.readLong());
  }

  @Override
  public double getDouble() {
    return Double.longBitsToDouble(getLong());
  }

  //  @Override
  //  public int readSizeValue(OutputStream stream) {
  //    var size = getInt();
  //    try {
  //      stream.write(buffer.array(), buffer.head() - 4, size);
  //    } catch (Exception e) {
  //      e.printStackTrace();
  //    }
  //    buffer.head(buffer.head() - 4);
  //    return size;
  //  }

  @Override
  public int readUntil(byte value) {
    int i = buffer.head();
    int start = i;
    int total = 0;
    byte currentByte = -1;
    while (currentByte != value) {
      currentByte = data[i++];
      total += currentByte;
    }
    buffer.head(i);
    var end = i - start;
    byteRange.set(start, end, total);
    return end;
  }

  @Override
  public void skipHead(int size) {
    int start = buffer.head();
    buffer.head(start + size);
  }

  @Override
  public void skipTail(int size) {
    int start = buffer.tail();
    buffer.tail(start + size);
  }

  @Override
  public String getString(int size) {
    int start = buffer.head();
    buffer.head(start + size);
    return new String(data, start, size, StandardCharsets.UTF_8);
  }

  @Override
  public int getWriteRemaining() {
    return buffer.writeRemaining();
  }

  @Override
  public int getReadRemaining() {
    return buffer.readRemaining();
  }

  @Override
  public byte[] getArray() {
    return data;
  }

  /**
   * If the buffer allows direct access to its backing array. Call this method if you need to check
   * before trying to access array.
   *
   * @return true if raw byte array access is possible
   */
  @Override
  public boolean canAccessArray() {
    return true;
  }

  @Override
  public int getLimit() {
    return buffer.limit();
  }

  @Override
  public void writeDouble(double value) {
    buffer.writeLong(Long.reverseBytes(Double.doubleToLongBits(value)));
  }

  @Override
  public void writeByte(byte b) {
    buffer.writeByte(b);
  }

  @Override
  public void writeInteger(int i) {
    buffer.writeInt(Integer.reverseBytes(i));
  }

  @Override
  public void writeBytes(byte[] bytes) {
    buffer.write(bytes);
  }

  @Override
  public void writeBytes(byte[] bytes, int offset, int size) {
    buffer.write(bytes, offset, size);
  }

  @Override
  public void writeInteger(int position, int i) {
    var tail = buffer.tail();
    buffer.tail(position);
    buffer.writeInt(Integer.reverseBytes(i));
    buffer.tail(tail);
  }

  @Override
  public void writeLong(long value) {
    buffer.writeLong(Long.reverseBytes(value));
  }

  @Override
  @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
  public void writeString(String value) {
    var buf = data;
    var i = buffer.tail();
    int start = i;
    char high = '\u0800';
    char low = '\udfff';

    for (int sIndex = 0, sLength = value.length(); sIndex < sLength; sIndex++) {
      char c = value.charAt(sIndex);
      if (c < '\u0080') {
        buf[i++] = (byte) c;
      } else if (c < '\u0800') {
        buf[i++] = (byte) (192 | c >>> 6);
        buf[i++] = (byte) (128 | c & 63);
      } else if (c < '\ud800' || c > '\udfff') {
        buf[i++] = (byte) (224 | c >>> 12);
        buf[i++] = (byte) (128 | c >>> 6 & 63);
        buf[i++] = (byte) (128 | c & 63);
      } else {
        int cp = 0;
        sIndex += 1;
        if (sIndex < sLength) cp = Character.toCodePoint(c, value.charAt(sIndex));
        if ((cp >= 1 << 16) && (cp < 1 << 21)) {
          buf[i++] = (byte) (240 | cp >>> 18);
          buf[i++] = (byte) (128 | cp >>> 12 & 63);
          buf[i++] = (byte) (128 | cp >>> 6 & 63);
          buf[i++] = (byte) (128 | cp & 63);
        } else {
          buf[i++] = (byte) '?';
        }
      }
    }
    buffer.tail(i);
  }

  @Override
  public int getHead() {
    return buffer.head();
  }

  @Override
  public int getTail() {
    return buffer.tail();
  }

  @Override
  public void setHead(int position) {
    buffer.head(position);
  }

  @Override
  public void setTail(int position) {
    buffer.tail(position);
  }
}
