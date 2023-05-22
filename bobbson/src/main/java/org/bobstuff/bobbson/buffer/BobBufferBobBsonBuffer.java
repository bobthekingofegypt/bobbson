package org.bobstuff.bobbson.buffer;

import java.util.Arrays;
import org.bobstuff.bobbson.BobBsonBuffer;
import org.bobstuff.bobbson.BobBsonByteRange;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BobBufferBobBsonBuffer implements BobBsonBuffer {
  private BobBuffer buffer;
  private byte[] data;
  private int start;
  public BobBsonByteRange byteRange;

  public BobBufferBobBsonBuffer(byte[] data) {
    this(data, 0, data.length);
  }

  public BobBufferBobBsonBuffer(byte[] data, int start) {
    this(data, start, data.length);
  }

  public BobBufferBobBsonBuffer(byte[] data, int start, int tail) {
    buffer = new BobBuffer(data, start, tail);
    this.byteRange = new BobBsonByteRange(buffer.getArray());
    this.data = data;
  }

  public BobBufferBobBsonBuffer(BobBuffer buffer) {
    this.buffer = buffer;
    this.byteRange = new BobBsonByteRange(buffer.getArray());
    this.data = buffer.getArray();
  }

  public BobBuffer getBobBuffer() {
    return buffer;
  }

  public void process(byte[] data, int head, int tail) {
    buffer = new BobBuffer(data, head, tail);
    this.byteRange.setData(data);
  }

  @Override
  public byte[] getBytes(int size) {
    byte[] subBuffer = new byte[size];
    buffer.readBytes(subBuffer);
    return subBuffer;
  }

  @Override
  public BobBsonBuffer.ByteRangeComparitor getByteRangeComparitor() {
    return byteRange;
  }

  @Override
  public byte[] toByteArray() {
    return Arrays.copyOf(buffer.getArray(), getTail());
  }

  @Override
  public int getInt() {
    return buffer.readIntegerLe();
  }

  @Override
  public byte getByte() {
    return buffer.readByte();
  }

  @Override
  public long getLong() {
    return buffer.readLongLe();
  }

  @Override
  public double getDouble() {
    return Double.longBitsToDouble(buffer.readLongLe());
  }

  //  //  @Override
  //  //  public int readSizeValue(OutputStream stream) {
  //  //    var size = getInt();
  //  //    try {
  //  //      stream.write(buffer.array(), buffer.head() - 4, size);
  //  //    } catch (Exception e) {
  //  //      e.printStackTrace();
  //  //    }
  //  //    buffer.head(buffer.head() - 4);
  //  //    return size;
  //  //  }
  //
  @Override
  public int readUntil(byte value) {
    byte[] bufferArray = data;
    boolean checkNext = true;
    int i = buffer.getHead();
    int start = i;
    int total = 0;
    while (checkNext && i < bufferArray.length) {
      var currentByte = bufferArray[i++];
      total += currentByte;
      checkNext = currentByte != value;
    }
    buffer.setHead(i);
    if (checkNext) {
      return -1;
    }
    byteRange.set(start, i - start, total);
    return i - start;
  }

  @Override
  public void skipHead(int size) {
    buffer.skipHead(size);
  }

  @Override
  public void skipTail(int size) {
    buffer.skipTail(size);
  }

  @Override
  public String getString(int size) {
    return buffer.readUtf8String(size);
  }

  @Override
  public int getWriteRemaining() {
    return buffer.writeRemaining();
  }

  @Override
  public int getReadRemaining() {
    return buffer.readRemaining();
  }

  //
  @Override
  public byte @Nullable [] getArray() {
    return buffer.getArray();
  }
  //
  @Override
  public int getLimit() {
    return buffer.getLimit();
  }

  @Override
  public void writeDouble(double value) {
    buffer.writeLongLe(Double.doubleToLongBits(value));
  }

  @Override
  public void writeByte(byte b) {
    buffer.writeByte(b);
  }
  //
  @Override
  public void writeInteger(int i) {
    buffer.writeIntegerLe(i);
  }

  @Override
  public void writeBytes(byte[] bytes) {
    buffer.writeBytes(bytes);
  }

  @Override
  public void writeBytes(byte[] bytes, int offset, int size) {
    buffer.writeBytes(bytes, offset, size);
  }
  //
  @Override
  public void writeInteger(int position, int i) {
    buffer.writeIntegerLe(position, i);
  }
  //
  @Override
  public void writeLong(long value) {
    buffer.writeLongLe(value);
  }

  @Override
  @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
  public void writeString(String value) {
    var buf = buffer.getArray();
    var i = buffer.getTail();
    int start = i;

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
    buffer.setTail(i);
  }

  @Override
  public int getHead() {
    return buffer.getHead();
  }

  @Override
  public int getTail() {
    return buffer.getTail();
  }

  @Override
  public void setHead(int position) {
    buffer.setHead(position);
  }

  @Override
  public void setTail(int position) {
    buffer.setTail(position);
  }
}
