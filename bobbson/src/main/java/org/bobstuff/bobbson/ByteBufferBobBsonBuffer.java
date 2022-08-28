package org.bobstuff.bobbson;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ByteBufferBobBsonBuffer implements BobBsonBuffer {
  private ByteBuffer buffer;
  private BobBsonByteRange byteRange;
  private int head;
  private int tail;
  private int position;

  public ByteBufferBobBsonBuffer(ByteBuffer buffer, int head, int tail) {
    this.buffer = buffer;
    this.byteRange = new BobBsonByteRange(buffer.array());
    this.head = head;
    this.tail = tail;
    this.position = buffer.position();
  }

  public ByteBufferBobBsonBuffer(ByteBuffer buffer) {
    this(buffer, 0, 0);
  }

  public ByteBufferBobBsonBuffer(byte[] data) {
    this(ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN), 0, 0);
  }

  public ByteBufferBobBsonBuffer(byte[] data, int head, int tail) {
    this(ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN), head, tail);
  }

  @Override
  @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
  public byte @Nullable [] getArray() {
    if (buffer.hasArray()) {
      return buffer.array();
    }
    return null;
  }

  @Override
  public int getInt() {
    if (position != head) {
      buffer.position(head);
    }
    var value = buffer.getInt();
    head += 4;
    position = head;
    return value;
  }

  @Override
  public byte getByte() {
    if (position != head) {
      buffer.position(head);
    }
    var value = buffer.get();
    head += 1;
    position = head;
    return value;
  }

  @Override
  public long getLong() {
    if (position != head) {
      buffer.position(head);
    }
    var value = buffer.getLong();
    head += 8;
    position = head;
    return value;
  }

  @Override
  public double getDouble() {
    if (position != head) {
      buffer.position(head);
    }
    var value = buffer.getDouble();
    head += 8;
    position = head;
    return value;
  }

  @Override
  public byte[] getBytes(int size) {
    if (position != head) {
      buffer.position(head);
    }
    byte[] subBuffer = new byte[size];
    buffer.get(subBuffer);
    head += size;
    position = head;
    return subBuffer;
  }

  @Override
  public String getString(int size) {
    if (position != head) {
      buffer.position(head);
    }
    int start = buffer.position();
    buffer.position(start + size);
    head += size;
    position = head;
    return new String(buffer.array(), start, size, StandardCharsets.UTF_8);
  }

  @Override
  public int getWriteRemaining() {
    return buffer.limit() - tail;
  }

  @Override
  public int getReadRemaining() {
    return tail - head;
  }

  @Override
  public int readUntil(byte value) {
    if (position != head) {
      buffer.position(head);
    }

    byte[] bufferArray = buffer.array();
    boolean checkNext = true;
    int i = buffer.position();
    int start = i;
    int total = 0;
    while (checkNext) {
      var currentByte = bufferArray[i++];
      total += currentByte;
      checkNext = currentByte != value;
    }
    buffer.position(i);
    head = i;
    position = head;
    byteRange.set(start, i - start, total);
    return i - start;
  }

  @Override
  public void skipHead(int size) {
    if (position != head) {
      buffer.position(head);
    }
    head += size;
    position = head;
    buffer.position(head);
  }

  @Override
  public void skipTail(int size) {
    if (position != tail) {
      buffer.position(tail);
    }
    tail += size;
    position = tail;
    buffer.position(tail);
  }

  @Override
  public int getHead() {
    return head;
  }

  @Override
  public int getTail() {
    return tail;
  }

  @Override
  public void setHead(int position) {
    head = position;
  }

  @Override
  public void setTail(int position) {
    tail = position;
  }

  @Override
  public int getLimit() {
    return buffer.limit();
  }

  @Override
  public void writeDouble(double value) {
    if (position != tail) {
      buffer.position(tail);
    }
    buffer.putDouble(value);
    tail += 8;
    position = tail;
  }

  @Override
  public void writeLong(long value) {
    if (position != tail) {
      buffer.position(tail);
    }
    buffer.putLong(value);
    tail += 8;
    position = tail;
  }

  @Override
  public void writeByte(byte value) {
    if (position != tail) {
      buffer.position(tail);
    }
    buffer.put(value);
    tail += 1;
    position = tail;
  }

  @Override
  public void writeInteger(int value) {
    if (position != tail) {
      buffer.position(tail);
    }
    buffer.putInt(value);
    tail += 4;
    position = tail;
  }

  @Override
  public void writeInteger(int position, int value) {
    buffer.putInt(position, value);
  }

  @Override
  public void writeBytes(byte[] bytes) {
    if (position != tail) {
      buffer.position(tail);
    }
    buffer.put(bytes);
    tail += bytes.length;
    position = tail;
  }

  @Override
  public void writeBytes(byte[] bytes, int offset, int size) {
    if (position != tail) {
      buffer.position(tail);
    }
    buffer.put(bytes, offset, size);
    tail += size;
    position = tail;
  }

  @Override
  @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
  public void writeString(String value) {
    if (position != tail) {
      buffer.position(tail);
    }
    var buf = buffer.array();
    var i = buffer.position();

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
    tail = i;
    position = tail;
    buffer.position(tail);
  }

  @Override
  public ByteRangeComparitor getByteRangeComparitor() {
    return byteRange;
  }

  public void pipe(final OutputStream out) throws IOException {
    out.write(buffer.array(), 0, buffer.position());
  }
}
