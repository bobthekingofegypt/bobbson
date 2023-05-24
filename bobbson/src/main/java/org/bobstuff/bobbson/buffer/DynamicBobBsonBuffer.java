package org.bobstuff.bobbson.buffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bobstuff.bobbson.ByteSizes;
import org.bobstuff.bobbson.buffer.pool.BobBsonBufferPool;

public class DynamicBobBsonBuffer implements BobBsonBuffer {
  private static final int DEFAULT_BUFFER_SIZE = 1024;
  public static final String BUF_IS_NULL = "buf is null";
  public static final String READ_BEYOND_LIMIT_ERROR_MSG =
      "cannot read beyond current tail " + "position";
  private BobBsonBufferPool pool;
  private int[] limits;
  private List<BobBsonBuffer> buffers;
  private int currentBufferIndex;
  private int currentWriteBufferIndex;
  private BobBsonBuffer buffer;
  private BobBsonBuffer writeBuffer;
  private int head;
  private int tail;
  private DynamicByteRangeComparitor byteRangeComparitor;

  public DynamicBobBsonBuffer(BobBsonBufferPool pool) {
    this(pool, pool.allocate(DEFAULT_BUFFER_SIZE));
  }

  public DynamicBobBsonBuffer(List<BobBsonBuffer> buffers, BobBsonBufferPool pool) {
    this.pool = pool;
    this.limits = new int[Math.max(buffers.size(), 30)];
    this.buffers = buffers;

    var i = 0;
    var count = 0;
    for (var b : buffers) {
      this.limits[i++] = b.getTail();
      count += b.getTail();
    }

    this.currentBufferIndex = 0;
    this.buffer = buffers.get(0);
    // TODO this isn't correct, buffers stack could be bigger than current write position
    this.currentWriteBufferIndex = buffers.size() - 1;
    this.writeBuffer = buffers.get(buffers.size() - 1);
    this.head = 0;
    this.tail = count;
    this.byteRangeComparitor = new DynamicByteRangeComparitor();
  }

  public DynamicBobBsonBuffer(BobBsonBufferPool pool, BobBsonBuffer startingBuffer) {
    this.pool = pool;
    this.limits = new int[10];
    this.buffers = new ArrayList<>();
    this.currentBufferIndex = 0;
    this.currentWriteBufferIndex = 0;
    this.buffer = startingBuffer;
    this.writeBuffer = startingBuffer;
    this.buffers.add(this.buffer);
    this.head = 0;
    this.tail = 0;
    this.byteRangeComparitor = new DynamicByteRangeComparitor();
  }

  public void reset() {
    this.currentBufferIndex = 0;
    this.currentWriteBufferIndex = 0;
    this.buffer = this.buffers.get(0);
    this.head = 0;
    this.tail = 0;

    for (var buf : buffers) {
      buf.setHead(0);
      buf.setTail(0);
    }
  }

  public void release() {
    for (var buf : buffers) {
      pool.recycle(buf);
    }
  }

  private byte readByteRollingBufferIfNecessary() {
    if (buffer.getReadRemaining() > 0) {
      head += 1;
      return buffer.getByte();
    }

    rollBufferForReading();
    head += 1;
    return buffer.getByte();
  }

  private void writeByteRollingBufferIfNecessary(byte value) {
    if (writeBuffer.getWriteRemaining() <= 0) {
      rollBufferForWriting();
    }
    tail += 1;
    writeBuffer.writeByte(value);
  }

  private void rollBufferForWriting() {
    if (limits.length == currentWriteBufferIndex + 1) {
      var newLimits = new int[limits.length * 2];
      System.arraycopy(limits, 0, newLimits, 0, limits.length);
      limits = newLimits;
    }
    if (currentWriteBufferIndex < buffers.size() - 1) {
      currentWriteBufferIndex += 1;

      this.writeBuffer = buffers.get(currentWriteBufferIndex);
      this.writeBuffer.setTail(0);
    } else {
      limits[currentWriteBufferIndex] = this.writeBuffer.getLimit();
      currentWriteBufferIndex += 1;
      this.writeBuffer = pool.allocate(DEFAULT_BUFFER_SIZE << Math.min(this.buffers.size(), 20));
      buffers.add(this.writeBuffer);
      if ((long) tail + this.writeBuffer.getLimit() > Integer.MAX_VALUE) {
        throw new RuntimeException(
            "dynamicbobbsonbuffer has an upper limit of " + Integer.MAX_VALUE + " size");
      }
    }
  }

  private void rollBufferForReading() {
    if (currentBufferIndex == buffers.size() - 1) {
      throw new IllegalStateException("Dynamic buffer is at end of stream cannot read further");
    }

    currentBufferIndex += 1;
    this.buffer = buffers.get(currentBufferIndex);
    this.buffer.setHead(0);
  }

  @Override
  public byte[] getArray() {
    throw new UnsupportedOperationException("Cannot access raw array of dynamic buffer");
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
  public int getInt() {
    if (head + ByteSizes.SIZE_OF_INT32 > tail) {
      throw new IllegalStateException(READ_BEYOND_LIMIT_ERROR_MSG);
    }
    var remaining = buffer.getReadRemaining();
    if (remaining < ByteSizes.SIZE_OF_INT32) {
      return (readByteRollingBufferIfNecessary() & 0xff)
          | (readByteRollingBufferIfNecessary() & 0xff) << 8
          | (readByteRollingBufferIfNecessary() & 0xff) << 16
          | (readByteRollingBufferIfNecessary() & 0xff) << 24;
    } else {
      head += ByteSizes.SIZE_OF_INT32;
      return buffer.getInt();
    }
  }

  @Override
  public byte getByte() {
    if (head + ByteSizes.SIZE_OF_BYTE > tail) {
      throw new IllegalStateException(READ_BEYOND_LIMIT_ERROR_MSG);
    }
    if (buffer.getReadRemaining() < ByteSizes.SIZE_OF_BYTE) {
      rollBufferForReading();
    }
    head += ByteSizes.SIZE_OF_BYTE;
    return buffer.getByte();
  }

  @Override
  public long getLong() {
    if (head + ByteSizes.SIZE_OF_INT64 > tail) {
      throw new IllegalStateException(READ_BEYOND_LIMIT_ERROR_MSG);
    }
    if (buffer.getReadRemaining() < ByteSizes.SIZE_OF_INT64) {
      return (readByteRollingBufferIfNecessary() & 0xff)
          | (readByteRollingBufferIfNecessary() & 0xff) << 8
          | (readByteRollingBufferIfNecessary() & 0xff) << 16
          | (long) (readByteRollingBufferIfNecessary() & 0xff) << 24
          | (long) (readByteRollingBufferIfNecessary() & 0xff) << 32
          | (long) (readByteRollingBufferIfNecessary() & 0xff) << 40
          | (long) (readByteRollingBufferIfNecessary() & 0xff) << 48
          | (long) (readByteRollingBufferIfNecessary() & 0xff) << 56;
    }
    head += ByteSizes.SIZE_OF_INT64;
    return buffer.getLong();
  }

  @Override
  public double getDouble() {
    return Double.longBitsToDouble(getLong());
  }

  @Override
  public byte[] getBytes(int size) {
    if (head + size > tail) {
      throw new IllegalStateException(READ_BEYOND_LIMIT_ERROR_MSG);
    }
    int remaining = buffer.getReadRemaining();
    if (remaining < size) {
      byte[] bytes = new byte[size];
      int total = 0;
      while (total < size) {
        byte[] temp = buffer.getBytes(Math.min(remaining, size - total));
        System.arraycopy(temp, 0, bytes, total, temp.length);
        total += temp.length;
        head += temp.length;
        if (total < size) {
          rollBufferForReading();
          remaining = buffer.getReadRemaining();
        }
      }
      return bytes;
    }
    return buffer.getBytes(size);
  }

  @Override
  public String getString(int size) {
    if (head + size > tail) {
      throw new IllegalStateException(READ_BEYOND_LIMIT_ERROR_MSG);
    }
    if (size < buffer.getReadRemaining()) {
      head += size;
      return buffer.getString(size);
    }
    // this is an expensive operation by requiring multiple temp arrays or string concats
    // just don't read from dynamic buffers if possible only use them for writing
    byte[] data = new byte[size];
    int remaining = buffer.getReadRemaining();
    int bytesToRead = size;
    int position = 0;
    while (bytesToRead > 0) {
      var temp = buffer.getBytes(Math.min(remaining, bytesToRead));
      System.arraycopy(temp, 0, data, position, temp.length);
      bytesToRead -= temp.length;
      position += temp.length;
      head += temp.length;

      if (remaining - temp.length == 0 && bytesToRead > 0) {
        rollBufferForReading();
        remaining = buffer.getReadRemaining();
      }
    }
    return new String(data, StandardCharsets.UTF_8);
  }

  @Override
  public int getWriteRemaining() {
    return Integer.MAX_VALUE;
  }

  @Override
  public int getReadRemaining() {
    int position = buffer.getReadRemaining();
    // current buffer use getRemaining
    // other buffers bar last one use limit
    // finally use tail
    for (var i = currentBufferIndex + 1; i < buffers.size() - 1; i += 1) {
      position += limits[i];
    }

    if (currentBufferIndex != buffers.size()) {
      position += buffers.get(buffers.size() - 1).getTail();
    }

    return position;
  }

  @Override
  public int readUntil(byte value) {
    int start = head;
    int bufferStart = buffer.getHead();
    int found = buffer.readUntil(value);
    if (found != -1) {
      head += found;
      byteRangeComparitor.set(start, found);
      return found;
    }
    int total = buffer.getHead() - bufferStart;
    while (found == -1 && currentBufferIndex < buffers.size()) {
      rollBufferForReading();
      bufferStart = buffer.getHead();
      found = buffer.readUntil(value);
      total += buffer.getHead() - bufferStart;
    }

    head += total;

    if (found == -1) {
      return -1;
    }

    byteRangeComparitor.set(start, total);
    return total;
  }

  @Override
  public void skipHead(int size) {
    var position = head + size;
    head = position;
    int bufferIndex = 0;

    while (position > limits[bufferIndex] && bufferIndex < buffers.size() - 1) {
      position -= limits[bufferIndex];
      bufferIndex += 1;
    }
    var buffer = buffers.get(bufferIndex);
    buffer.setHead(position);
    currentBufferIndex = bufferIndex;
    this.buffer = buffer;
  }

  @Override
  public void skipTail(int size) {
    // Need to hard limit a roll if we are skipping beyond a boundary
    var checkSize = writeBuffer.getTail();
    if (size < 0 && checkSize + size > 0) {
      // shortcut logic as current buffer can handle the shift
      writeBuffer.setTail(checkSize + size);
      tail = tail + size;
      return;
    }
    if (size > 0 && checkSize + size < writeBuffer.getLimit()) {
      // shortcut logic as current buffer can handle the shift
      writeBuffer.setTail(checkSize + size);
      tail = tail + size;
      return;
    }

    var newTail = tail + size;
    var current = 0;
    tail = newTail;
    int bufferIndex = 0;

    // TODO shortcut if space exits

    if (newTail == 0) {
      currentWriteBufferIndex = bufferIndex;
      writeBuffer = buffers.get(currentWriteBufferIndex);
      writeBuffer.setTail(0);
      return;
    }

    int lastJump = 0;

    while (current != newTail) {

      if (bufferIndex < buffers.size() - 1) {
        lastJump = Math.min(newTail - current, limits[bufferIndex]);
        buffers.get(bufferIndex).setTail(lastJump);
        current += lastJump;
      } else {
        var limit = this.buffers.get(bufferIndex).getLimit(); // writeBuffer.getLimit();
        var remainingSteps = newTail - current;
        //        var writeRemaining = writeBuffer.getWriteRemaining();
        lastJump = Math.min(limit, remainingSteps);
        current += lastJump;
        //        writeBuffer.setTail(lastJump);
        if (lastJump == limit && newTail - current > 0) {
          rollBufferForWriting();
        }
      }
      if (current != newTail) {
        bufferIndex += 1;
      }
    }

    if (currentWriteBufferIndex != bufferIndex) {
      currentWriteBufferIndex = bufferIndex;
      writeBuffer = buffers.get(currentWriteBufferIndex);
    }
    writeBuffer.setTail(lastJump);
    //    var buffer = buffers.get(bufferIndex);
    //    buffer.setHead(position);
    //    currentBufferIndex = bufferIndex;
    //    this.buffer = buffer;
    // TODO this is not robust, if size > buffer size
    //    if (buffer.getWriteRemaining() < size) {
    //      rollBufferForWriting();
    //    }
    //    buffer.skipTail(size);
  }

  @Override
  public int getHead() {
    int position = 0;
    for (var i = 0; i < currentBufferIndex; i += 1) {
      position += limits[i];
    }
    return position + buffer.getHead();
  }

  @Override
  public int getTail() {
    return tail;
    //    int position = 0;
    //    for (var i = 0; i < currentBufferIndex; i += 1) {
    //      position += limits[i];
    //    }
    //    return position + buffer.getTail();
  }

  @Override
  public void setHead(int position) {
    skipHead(position - head);
  }

  @Override
  public void setTail(int position) {
    skipTail(position - tail);
    //    // TODO rethink this
    //    int bufferIndex = 0;
    //
    //    while (position > limits[bufferIndex] && bufferIndex < buffers.size() - 1) {
    //      position -= limits[bufferIndex];
    //      bufferIndex += 1;
    //    }
    //    var buffer = buffers.get(bufferIndex);
    //    buffer.setTail(position);
    //    currentBufferIndex = bufferIndex;
    //    buffers = buffers.subList(0, currentBufferIndex + 1);
    //    this.buffer = buffer;
  }

  @Override
  public int getLimit() {
    return Integer.MAX_VALUE;
  }

  @Override
  public void writeDouble(double value) {
    writeLong(Double.doubleToRawLongBits(value));
  }

  @Override
  public void writeByte(byte value) {
    tail += 1;
    if (writeBuffer.getWriteRemaining() < ByteSizes.SIZE_OF_BYTE) {
      rollBufferForWriting();
    }
    writeBuffer.writeByte(value);
  }

  @Override
  public void writeInteger(int value) {
    if (writeBuffer.getWriteRemaining() < ByteSizes.SIZE_OF_INT32) {
      writeByteRollingBufferIfNecessary((byte) value);
      writeByteRollingBufferIfNecessary((byte) (value >>> 8));
      writeByteRollingBufferIfNecessary((byte) (value >>> 16));
      writeByteRollingBufferIfNecessary((byte) (value >>> 24));
    } else {
      tail += ByteSizes.SIZE_OF_INT32;
      writeBuffer.writeInteger(value);
    }
  }

  @Override
  public void writeInteger(int position, int value) {
    var before = tail;
    setTail(position);
    writeInteger(value);
    setTail(before);
  }

  @Override
  public void writeLong(long value) {
    if (writeBuffer.getWriteRemaining() < ByteSizes.SIZE_OF_INT64) {
      writeByteRollingBufferIfNecessary((byte) (value & 0xff));
      writeByteRollingBufferIfNecessary((byte) ((value >>> 8L) & 0xff));
      writeByteRollingBufferIfNecessary((byte) ((value >>> 16L) & 0xff));
      writeByteRollingBufferIfNecessary((byte) ((value >>> 24L) & 0xff));
      writeByteRollingBufferIfNecessary((byte) ((value >>> 32L) & 0xff));
      writeByteRollingBufferIfNecessary((byte) ((value >>> 40L) & 0xff));
      writeByteRollingBufferIfNecessary((byte) ((value >>> 48L) & 0xff));
      writeByteRollingBufferIfNecessary((byte) ((value >>> 56L) & 0xff));
    } else {
      tail += ByteSizes.SIZE_OF_INT64;
      writeBuffer.writeLong(value);
    }
  }

  @Override
  public void writeBytes(byte[] bytes) {
    writeBytes(bytes, 0, bytes.length);
  }

  @Override
  public void writeBytes(byte[] bytes, int offset, int size) {
    tail += size;
    int bytesToWrite = size - offset;
    int remaining = writeBuffer.getWriteRemaining();
    if (remaining >= bytesToWrite) {
      writeBuffer.writeBytes(bytes, offset, size);
      return;
    }

    while (bytesToWrite > 0) {
      writeBuffer.writeBytes(bytes, offset, remaining);
      bytesToWrite -= remaining;
      // TODO this is wrong, need tests to sort this out
      if (bytesToWrite > 0) {
        rollBufferForWriting();
        offset += remaining;
        remaining = Math.min(writeBuffer.getWriteRemaining(), bytesToWrite);
      }
    }
  }

  @Override
  @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
  public void writeString(String value) {
    var buf = writeBuffer.getArray();
    if (buf == null) {
      throw new IllegalStateException(BUF_IS_NULL);
    }
    var i = writeBuffer.getTail();
    var remaining = writeBuffer.getWriteRemaining();
    byte TWO_BYTES = 2;
    byte THREE_BYTES = 3;
    byte FOUR_BYTES = 4;

    for (int sIndex = 0, sLength = value.length(); sIndex < sLength; sIndex++) {
      char c = value.charAt(sIndex);
      if (c < '\u0080') {
        if (remaining < ByteSizes.SIZE_OF_BYTE) {
          writeBuffer.setTail(i);
          rollBufferForWriting();
          remaining = writeBuffer.getWriteRemaining();
          buf = writeBuffer.getArray();
          i = 0;
        }
        if (buf == null) {
          throw new IllegalStateException(BUF_IS_NULL);
        }
        buf[i++] = (byte) c;
        remaining -= 1;
        tail += 1;
      } else if (c < '\u0800') {
        if (remaining < TWO_BYTES) {
          writeBuffer.setTail(i);
          writeByteRollingBufferIfNecessary((byte) (192 | c >>> 6));
          writeByteRollingBufferIfNecessary((byte) (128 | c & 63));
          //          writeBuffer.setTail(i);
          //          rollBufferForWriting();
          buf = writeBuffer.getArray();
          if (buf == null) {
            throw new IllegalStateException(BUF_IS_NULL);
          }
          i = writeBuffer.getTail();
          remaining = writeBuffer.getWriteRemaining();
        } else {
          buf[i++] = (byte) (192 | c >>> 6);
          buf[i++] = (byte) (128 | c & 63);
          tail += 2;
          remaining -= 2;
        }
      } else if (c < '\ud800' || c > '\udfff') {
        if (remaining < THREE_BYTES) {
          writeBuffer.setTail(i);
          writeByteRollingBufferIfNecessary((byte) (224 | c >>> 12));
          writeByteRollingBufferIfNecessary((byte) (128 | c >>> 6 & 63));
          writeByteRollingBufferIfNecessary((byte) (128 | c & 63));
          //          writeBuffer.setTail(i);
          //          rollBufferForWriting();
          buf = writeBuffer.getArray();
          if (buf == null) {
            throw new IllegalStateException(BUF_IS_NULL);
          }
          i = writeBuffer.getTail();
          remaining = writeBuffer.getWriteRemaining();
        } else {
          buf[i++] = (byte) (224 | c >>> 12);
          buf[i++] = (byte) (128 | c >>> 6 & 63);
          buf[i++] = (byte) (128 | c & 63);
          tail += 3;
          remaining -= 3;
        }
      } else {
        int cp = 0;
        sIndex += 1;
        if (sIndex < sLength) cp = Character.toCodePoint(c, value.charAt(sIndex));
        if ((cp >= 1 << 16) && (cp < 1 << 21)) {
          if (remaining < FOUR_BYTES) {
            writeBuffer.setTail(i);
            writeByteRollingBufferIfNecessary((byte) (240 | cp >>> 18));
            writeByteRollingBufferIfNecessary((byte) (128 | cp >>> 12 & 63));
            writeByteRollingBufferIfNecessary((byte) (128 | cp >>> 6 & 63));
            writeByteRollingBufferIfNecessary((byte) (128 | cp & 63));
            //            writeBuffer.setTail(i);
            //            rollBufferForWriting();
            buf = writeBuffer.getArray();
            if (buf == null) {
              throw new IllegalStateException(BUF_IS_NULL);
            }
            i = writeBuffer.getTail();
            remaining = writeBuffer.getWriteRemaining();
          } else {
            buf[i++] = (byte) (240 | cp >>> 18);
            buf[i++] = (byte) (128 | cp >>> 12 & 63);
            buf[i++] = (byte) (128 | cp >>> 6 & 63);
            buf[i++] = (byte) (128 | cp & 63);
            tail += 4;
            remaining -= 4;
          }
        } else {
          if (remaining < ByteSizes.SIZE_OF_BYTE) {
            writeBuffer.setTail(i);
            rollBufferForWriting();
            remaining = writeBuffer.getWriteRemaining();
            buf = writeBuffer.getArray();
            i = 0;
          }
          if (buf == null) {
            throw new IllegalStateException(BUF_IS_NULL);
          }
          buf[i++] = (byte) '?';
          remaining -= 1;
          tail += 1;
        }
      }
    }
    writeBuffer.setTail(i);
  }

  @Override
  public ByteRangeComparator getByteRangeComparator() {
    return byteRangeComparitor;
  }

  @Override
  public byte[] toByteArray() {
    if (buffers.size() == 1) {
      return buffers.get(0).toByteArray();
    }

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      pipe(bos);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return bos.toByteArray();
  }

  public void pipe(final OutputStream out) throws IOException {
    for (var b : buffers) {
      byte[] a = b.getArray();
      if (a == null) {
        throw new IllegalStateException("buf array is null");
      }
      out.write(a, 0, b.getTail());
    }
  }

  public List<BobBsonBuffer> getBuffers() {
    return buffers.subList(0, currentWriteBufferIndex + 1);
  }

  private class DynamicByteRangeComparitor implements ByteRangeComparator {
    private int start;
    private int size;

    public void set(int start, int size) {
      this.start = start;
      this.size = size;
    }

    public int getWeakHash() {
      return -1;
    }

    @Override
    public String value() {
      int head = DynamicBobBsonBuffer.this.getHead();
      DynamicBobBsonBuffer.this.setHead(start);
      var result = DynamicBobBsonBuffer.this.getString(size - 1);
      DynamicBobBsonBuffer.this.setHead(head);
      return result;
    }

    @Override
    public boolean equalsArray(byte[] array) {
      int head = DynamicBobBsonBuffer.this.getHead();
      DynamicBobBsonBuffer.this.setHead(start);
      var result = DynamicBobBsonBuffer.this.getBytes(size - 1);
      DynamicBobBsonBuffer.this.setHead(head);
      return Arrays.equals(result, 0, result.length, array, 0, array.length);
    }

    @Override
    public boolean equalsArray(byte[] array, int weakHash) {
      return equalsArray(array);
    }
  }
}
