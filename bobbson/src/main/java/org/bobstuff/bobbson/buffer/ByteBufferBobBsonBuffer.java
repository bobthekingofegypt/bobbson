package org.bobstuff.bobbson.buffer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.bobstuff.bobbson.BobBsonByteRange;

/**
 * Implementation of {@code BobBsonBuffer} that operates on a {@code ByteBuffer}.
 *
 * <p>ByteBuffer must be little endian and not direct.
 */
public class ByteBufferBobBsonBuffer implements BobBsonBuffer {
  private final ByteBuffer buffer;
  private final BobBsonByteRange byteRange;
  private int head;
  private int tail;
  private int position;

  /**
   * Construct new ByteBufferBobBsonBuffer using the given buffer instance. Head and tail will be
   * set to the given values and the position of the ByteBuffer will now be controlled by this class
   *
   * @param buffer backing {@code ByteBuffer} instance
   * @param head position in buffer to start read operations
   * @param tail position in buffer to start write operations
   */
  public ByteBufferBobBsonBuffer(ByteBuffer buffer, int head, int tail) {
    this.buffer = buffer;
    this.byteRange = new BobBsonByteRange(buffer.array());
    this.head = head;
    this.tail = tail;
    buffer.position(head);
    this.position = head;
  }

  /**
   * Construct new {@code ByteBufferBobBsonBuffer} using the given buffer defaulting head and tail
   * to zero.
   *
   * @param buffer backing {@code ByteBuffer} instance
   */
  public ByteBufferBobBsonBuffer(ByteBuffer buffer) {
    this(buffer, 0, 0);
  }

  /**
   * Construct new instance using the give raw byte array. Starting head/tail positions are 0.
   *
   * @param data raw byte array to read/write from
   */
  public ByteBufferBobBsonBuffer(byte[] data) {
    this(ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN), 0, 0);
  }

  /**
   * Construct new instance using the given raw byte array, setting the starting head/tail position
   * to passed values.
   *
   * @param data raw byte array to read/write from
   * @param head position to start reading from
   * @param tail position to start writing from
   */
  public ByteBufferBobBsonBuffer(byte[] data, int head, int tail) {
    this(ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN), head, tail);
  }

  @Override
  public byte[] getArray() {
    if (buffer.hasArray()) {
      return buffer.array();
    }
    throw new UnsupportedOperationException("ByteBuffer does not provide access to backing array");
  }

  /**
   * If the buffer allows direct access to its backing array. Call this method if you need to check
   * before trying to access array.
   *
   * @return true if raw byte array access is possible
   */
  @Override
  public boolean canAccessArray() {
    return buffer.hasArray();
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
  public void writeString(String value) {
    if (position != tail) {
      buffer.position(tail);
    }
    var buf = buffer.array();
    var i = buffer.position();

    tail = BufferUtilities.writeStringToByteArray(value, buf, i);
    position = tail;
    buffer.position(tail);
  }

  @Override
  public ByteRangeComparator getByteRangeComparator() {
    return byteRange;
  }

  @Override
  public byte[] toByteArray() {
    return Arrays.copyOf(buffer.array(), getTail());
  }

  /**
   * Pipe buffer contents into output stream
   *
   * @param out stream write data onto
   * @throws IOException if write operation fails
   */
  public void pipe(final OutputStream out) throws IOException {
    out.write(buffer.array(), 0, tail);
  }
}
