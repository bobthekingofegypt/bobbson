package org.bobstuff.bobbson.buffer;

import java.nio.charset.StandardCharsets;

public class BobBuffer {
  private int head;
  private int tail;
  private final int limit;
  private final byte[] data;

  public BobBuffer(byte[] data, int head, int tail) {
    if (tail > data.length) {
      throw new IllegalArgumentException("tail cannot exceed length of data array");
    }

    this.data = data;
    this.head = head;
    this.tail = tail;
    this.limit = data.length;
  }

  public byte[] getArray() {
    return data;
  }

  public int getLimit() {
    return limit;
  }

  public int getHead() {
    return head;
  }

  public void setHead(int head) {
    if (head > tail) {
      throw new IllegalArgumentException("trying to skip beyond end of buffer");
    }
    this.head = head;
  }

  public void skipHead(int amount) {
    if (head + amount > tail) {
      throw new IllegalArgumentException("trying to skip beyond end of buffer");
    }
    this.head += amount;
  }

  public int getTail() {
    return tail;
  }

  public void setTail(int tail) {
    if (tail > limit) {
      throw new IllegalArgumentException("trying to set tail beyond end of buffer");
    }
    this.tail = tail;
  }

  public void skipTail(int amount) {
    if (tail + amount > limit) {
      throw new IllegalArgumentException("trying to skip beyond end of buffer");
    }
    this.tail += amount;
  }

  public int writeRemaining() {
    return limit - tail;
  }

  public int readRemaining() {
    return tail - head;
  }

  public void writeIntegerLe(int value) {
    if (tail + 4 > limit) {
      throw new IllegalStateException("Insufficient space in buffer to write int");
    }
    data[tail] = (byte) value;
    data[tail + 1] = (byte) (value >>> 8);
    data[tail + 2] = (byte) (value >>> 16);
    data[tail + 3] = (byte) (value >>> 24);
    tail += 4;
  }

  public void writeIntegerLe(int position, int value) {
    int oldTail = tail;
    tail = position;
    writeIntegerLe(value);
    tail = oldTail;
  }

  public void writeLongLe(long value) {
    if (tail + 8 > limit) {
      throw new IllegalStateException("Insufficient space in buffer to write long");
    }

    data[tail++] = (byte) (value & 0xff);
    data[tail++] = (byte) ((value >>> 8L) & 0xff);
    data[tail++] = (byte) ((value >>> 16L) & 0xff);
    data[tail++] = (byte) ((value >>> 24L) & 0xff);
    data[tail++] = (byte) ((value >>> 32L) & 0xff);
    data[tail++] = (byte) ((value >>> 40L) & 0xff);
    data[tail++] = (byte) ((value >>> 48L) & 0xff);
    data[tail++] = (byte) ((value >>> 56L) & 0xff);
  }

  public void writeByte(byte value) {
    if (tail + 1 > limit) {
      throw new IllegalStateException("Insufficient space in buffer to write long");
    }

    data[tail] = value;
    tail += 1;
  }

  public void writeBytes(byte[] value) {
    writeBytes(value, 0, value.length);
  }

  public void writeBytes(byte[] value, int offset, int length) {
    if ((length + offset) > value.length) {
      throw new IllegalStateException("attempting to read beyond length of given array");
    }
    if ((tail + length) > limit) {
      throw new IllegalStateException("Insufficient space in buffer to write bytes");
    }
    System.arraycopy(value, offset, data, tail, length);
    tail += length;
  }

  public int readIntegerLe() {
    if ((head + 4) > tail) {
      throw new IllegalStateException("insufficient data in buffer");
    }

    return (data[head++] & 0xff)
        | (data[head++] & 0xff) << 8
        | (data[head++] & 0xff) << 16
        | (data[head++] & 0xff) << 24;
  }

  public long readLongLe() {
    if ((head + 8) > tail) {
      throw new IllegalStateException("insufficient data in buffer");
    }

    return (data[head++] & 0xffL)
        | (data[head++] & 0xffL) << 8
        | (data[head++] & 0xffL) << 16
        | (data[head++] & 0xffL) << 24
        | (data[head++] & 0xffL) << 32
        | (data[head++] & 0xffL) << 40
        | (data[head++] & 0xffL) << 48
        | (data[head++] & 0xffL) << 56;
  }

  public byte readByte() {
    if ((head + 1) > tail) {
      throw new IllegalStateException("insufficient data in buffer");
    }

    return data[head++];
  }

  public int readBytes(byte[] sink) {
    int bytesToCopy = Math.min(sink.length, tail - head);
    System.arraycopy(data, head, sink, 0, bytesToCopy);

    head += bytesToCopy;

    return bytesToCopy;
  }

  public String readUtf8String(int size) {
    if (head + size > tail) {
      throw new IllegalStateException("attempting to readUtf8String beyond size of data");
    }
    var value = new String(data, head, size, StandardCharsets.UTF_8);
    head += size;

    return value;
  }
}
