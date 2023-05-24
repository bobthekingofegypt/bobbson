package org.bobstuff.bobbson.buffer;

import java.nio.charset.StandardCharsets;

/**
 * Implementation of a raw buffer, like ByteBuf, that supports little endian operations
 * to read values from a byte array.
 * <p>Used internally to power {@link BobBufferBobBsonBuffer}
 * but can be used as a building block to create custom {@link BobBsonBuffer} implementations.
 *
 * <p>Buffer has a concept of head and tail so can support both read and write operations, read
 * operations will begin at head and can read upto tail position and writes will add data from
 * tails position until limit is reached
 */
public class BobBuffer {
  /**
   * the current head of the buffer, location where reads will happen
   */
  private int head;
  /**
   * the current tail of the buffer, location where writes will happen
   */
  private int tail;
  /**
   * the maximum value that tail can write upto, does not have to equal the size of {@code data} array
   */
  private final int limit;
  /**
   * the backing byte array for this buffer
   */
  private final byte[] data;

  /**
   * Initialize BobBuffer with the given head and tail positions
   * @param data  the backing byte array
   * @param head  the starting position for head
   * @param tail  the starting position for tail
   */
  public BobBuffer(byte[] data, int head, int tail) {
    if (tail > data.length) {
      throw new IllegalArgumentException("tail cannot exceed length of data array");
    }

    this.data = data;
    this.head = head;
    this.tail = tail;
    this.limit = data.length;
  }

  /**
   * @return the backing array for the buffer
   */
  public byte[] getArray() {
    return data;
  }

  /**
   * Gets the current limit for the buffer
   * @return current limit
   */
  public int getLimit() {
    return limit;
  }

  /**
   * @return current head position of buffer
   */
  public int getHead() {
    return head;
  }

  /**
   * Update the head position of the buffer
   * @param head  new position for head
   */
  public void setHead(int head) {
    if (head > tail) {
      throw new IllegalArgumentException("trying to skip beyond end of buffer");
    }
    this.head = head;
  }

  /**
   * Adjusts the head position by the given amount, use negative number to move backwards
   * @param amount  number of bytes to move by
   */
  public void skipHead(int amount) {
    if (head + amount > tail) {
      throw new IllegalArgumentException("trying to skip beyond end of buffer");
    }
    this.head += amount;
  }

  /**
   * Gets the tail position for the buffer
   * @return the current tail position
   */
  public int getTail() {
    return tail;
  }

  /**
   * Updates the current tail position of the buffer
   * @param tail  new tail position value
   */
  public void setTail(int tail) {
    if (tail > limit) {
      throw new IllegalArgumentException("trying to set tail beyond end of buffer");
    }
    this.tail = tail;
  }

  /**
   * Adjusts the tail position by the given amount, use negative number to move backwards
   * @param amount  number of byes to adjust by
   */
  public void skipTail(int amount) {
    if (tail + amount > limit) {
      throw new IllegalArgumentException("trying to skip beyond end of buffer");
    }
    this.tail += amount;
  }

  /**
   * @return the number of bytes of space left to write to
   */
  public int writeRemaining() {
    return limit - tail;
  }

  /**
   * @return the number of bytes left to read
   */
  public int readRemaining() {
    return tail - head;
  }

  /**
   * Writes a little endian encoded 32bit integer
   * @param value  the value to write
   */
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

  /**
   * Writes a little endian encoded 32bit integer at the given position, ignores the current head position
   * @param position  position in the buffer to start writing the valu
   * @param value  the integer value to write
   */
  public void writeIntegerLe(int position, int value) {
    int oldTail = tail;
    tail = position;
    writeIntegerLe(value);
    tail = oldTail;
  }

  /**
   * Writes a little endian 64bit long to the buffer
   * @param value  the value to write
   */
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

  /**
   * Write a single byte value to the buffer
   * @param value  the byte to write
   */
  public void writeByte(byte value) {
    if (tail + 1 > limit) {
      throw new IllegalStateException("Insufficient space in buffer to write long");
    }

    data[tail] = value;
    tail += 1;
  }

  /**
   * Writes the given byte array to the buffer
   * @param value  the bytes to be written
   */
  public void writeBytes(byte[] value) {
    writeBytes(value, 0, value.length);
  }

  /**
   * Writes the requested subset of the given byte array to the buffer
   * @param value  the byte array containing data to be copied
   * @param offset  starting point in the given array
   * @param length  number of bytes from array to be written
   */
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

  /**
   * Reads a little endian encoded 32bit integer from the buffer
   * @return value read from buffer
   */
  public int readIntegerLe() {
    if ((head + 4) > tail) {
      throw new IllegalStateException("insufficient data in buffer");
    }

    return (data[head++] & 0xff)
        | (data[head++] & 0xff) << 8
        | (data[head++] & 0xff) << 16
        | (data[head++] & 0xff) << 24;
  }

  /**
   * Reads a little endian encoded 64bit long from the buffer
   * @return value read from buffer
   */
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

  /**
   * Reads a single byte from the buffer
   * @return value read from buffer
   */
  public byte readByte() {
    if ((head + 1) > tail) {
      throw new IllegalStateException("insufficient data in buffer");
    }

    return data[head++];
  }

  /**
   * Reads enough bytes to fill the given sync array or as much data as is left in the buffer if the sync is greater than read remaining
   * @param sink  buffer to read bytes into
   * @return number of bytes written to sink
   */
  public int readBytes(byte[] sink) {
    int bytesToCopy = Math.min(sink.length, tail - head);
    System.arraycopy(data, head, sink, 0, bytesToCopy);

    head += bytesToCopy;

    return bytesToCopy;
  }

  /**
   * Read a utf encoded string of the given length from the buffer
   * @param size  number of bytes making up the string
   * @return the decoded string
   */
  public String readUtf8String(int size) {
    if (head + size > tail) {
      throw new IllegalStateException("attempting to readUtf8String beyond size of data");
    }
    var value = new String(data, head, size, StandardCharsets.UTF_8);
    head += size;

    return value;
  }
}
