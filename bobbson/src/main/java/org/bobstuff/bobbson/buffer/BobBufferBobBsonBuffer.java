package org.bobstuff.bobbson.buffer;

import java.util.Arrays;
import org.bobstuff.bobbson.BobBsonByteRange;

/** Implementation of {@code BobBsonBuffer} that uses {@code BobBuffer} as its backing buffer. */
public class BobBufferBobBsonBuffer implements BobBsonBuffer {
  private BobBuffer buffer;
  private byte[] data;
  public BobBsonByteRange byteRange;

  /**
   * Construct new buffer using data array, head will be set to 0 and tail to length of data
   *
   * @param data backing byte array
   */
  public BobBufferBobBsonBuffer(byte[] data) {
    this(data, 0, data.length);
  }

  /**
   * Construct a new buffer using data array, head will be set to given head value. Tail will be set
   * to length of data array
   *
   * @param data backing byte array
   * @param head starting value for head
   */
  public BobBufferBobBsonBuffer(byte[] data, int head) {
    this(data, head, data.length);
  }

  /**
   * Construct a new buffer using the data array, starting head and tail set to given values
   *
   * @param data backing byte array
   * @param head starting value for head
   * @param tail starting value for tail
   */
  public BobBufferBobBsonBuffer(byte[] data, int head, int tail) {
    buffer = new BobBuffer(data, head, tail);
    this.byteRange = new BobBsonByteRange(buffer.getArray());
    this.data = data;
  }

  /**
   * Construct a new buffer using an existing {@code BobBuffer}
   *
   * @param buffer {@code BobBuffer} to read from
   */
  public BobBufferBobBsonBuffer(BobBuffer buffer) {
    this.buffer = buffer;
    this.byteRange = new BobBsonByteRange(buffer.getArray());
    this.data = buffer.getArray();
  }

  public BobBuffer getBobBuffer() {
    return buffer;
  }

  /**
   * Set BobBsonBuffer to operate on a new backing array, allows reuse of this instance for new data
   *
   * @param data new backing array
   * @param head new head position to read from
   * @param tail new tail position to write from
   */
  public void process(byte[] data, int head, int tail) {
    buffer = new BobBuffer(data, head, tail);
    this.data = data;
    this.byteRange.setData(data);
  }

  @Override
  public byte[] getBytes(int size) {
    byte[] subBuffer = new byte[size];
    buffer.readBytes(subBuffer);
    return subBuffer;
  }

  @Override
  public ByteRangeComparator getByteRangeComparator() {
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

  @Override
  public byte[] getArray() {
    return buffer.getArray();
  }

  @Override
  public boolean canAccessArray() {
    return true;
  }

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

  @Override
  public void writeInteger(int position, int i) {
    buffer.writeIntegerLe(position, i);
  }

  @Override
  public void writeLong(long value) {
    buffer.writeLongLe(value);
  }

  @Override
  public void writeString(String value) {
    var buf = buffer.getArray();
    var i = buffer.getTail();

    int finalTail = BufferUtilities.writeStringToByteArray(value, buf, i);

    buffer.setTail(finalTail);
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
