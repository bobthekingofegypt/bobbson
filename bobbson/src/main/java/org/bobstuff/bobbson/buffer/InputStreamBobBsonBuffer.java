package org.bobstuff.bobbson.buffer;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.bobstuff.bobbson.ByteSizes;

public class InputStreamBobBsonBuffer implements BobBsonBuffer {
  private static final int DEFAULT_BUFFER_SIZE = 1024;
  private static final int MAX_VALUE_BUFFER_SIZE = 16777216;
  private static final int MAX_KEY_BUFFER_SIZE = 16777216;
  public static final String WRITE_NOT_SUPPORTED = "Write not supported";
  private byte[] buffer;
  private int bufferIndex;
  private int available;
  private byte[] valueBuffer;
  private byte[] keyBuffer;
  private int keyBufferIndex;
  private int weakHash;
  private InputStream is;

  private ByteRangeComparator comparator;

  public InputStreamBobBsonBuffer(InputStream is) {
    this(is, DEFAULT_BUFFER_SIZE);
  }

  public InputStreamBobBsonBuffer(InputStream is, int bufferSize) {
    this.is = is;
    this.buffer = new byte[bufferSize];
    this.valueBuffer = new byte[256];
    this.keyBuffer = new byte[256];
    this.comparator = new InputStreamByteRangeComparator();
    this.weakHash = 0;
    this.keyBufferIndex = 0;
    this.bufferIndex = 0;
  }

  @SuppressWarnings("PMD.AssignmentInOperand")
  private int fillBuffer() {
    var unread = available - bufferIndex;
    if (unread > 0) {
      System.arraycopy(buffer, bufferIndex, buffer, 0, unread);
    }

    int read;
    int position = unread;
    try {
      while (position < buffer.length
          && (read = is.read(buffer, position, buffer.length - position)) != -1) {
        position += read;
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to read from input stream", e);
    }

    bufferIndex = 0;
    available = position;

    return position - unread;
  }

  public final byte read() {
    if (bufferIndex >= available) {
      fillBuffer();
      throwIfInsufficientData(available, 1);
    }
    return buffer[bufferIndex++];
  }

  private static void throwIfInsufficientData(int available, int valueSize) {
    if (available < valueSize) {
      throw new IllegalStateException();
    }
  }

  @Override
  public byte[] getArray() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean canAccessArray() {
    return false;
  }

  @Override
  public int getInt() {
    if (available - bufferIndex < ByteSizes.SIZE_OF_INT32) {
      fillBuffer();
      throwIfInsufficientData(available, 4);
    }

    return (buffer[bufferIndex++] & 0xff)
        | (buffer[bufferIndex++] & 0xff) << 8
        | (buffer[bufferIndex++] & 0xff) << 16
        | (buffer[bufferIndex++] & 0xff) << 24;
  }

  @Override
  public byte getByte() {
    return read();
  }

  @Override
  public long getLong() {
    if (available - bufferIndex < ByteSizes.SIZE_OF_INT64) {
      fillBuffer();
      throwIfInsufficientData(available, 8);
    }

    return (buffer[bufferIndex++] & 0xffL)
        | (buffer[bufferIndex++] & 0xffL) << 8
        | (buffer[bufferIndex++] & 0xffL) << 16
        | (buffer[bufferIndex++] & 0xffL) << 24
        | (buffer[bufferIndex++] & 0xffL) << 32
        | (buffer[bufferIndex++] & 0xffL) << 40
        | (buffer[bufferIndex++] & 0xffL) << 48
        | (buffer[bufferIndex++] & 0xffL) << 56;
  }

  @Override
  public double getDouble() {
    return Double.longBitsToDouble(getLong());
  }

  @Override
  public byte[] getBytes(int size) {
    var bytes = new byte[size];
    if (size <= (available - bufferIndex)) {
      System.arraycopy(buffer, bufferIndex, bytes, 0, size);
      bufferIndex += size;
    } else if (size < buffer.length) {
      var read = available - bufferIndex;
      System.arraycopy(buffer, bufferIndex, bytes, 0, read);
      bufferIndex = available;
      fillBuffer();

      System.arraycopy(buffer, bufferIndex, bytes, 0, size - read);
      bufferIndex += size - read;
    } else {
      int retrieved = 0;
      int remaining = available - bufferIndex;

      while (retrieved < size) {
        var toRead = Math.min(size - retrieved, remaining);
        System.arraycopy(buffer, bufferIndex, bytes, retrieved, toRead);
        bufferIndex += toRead;
        retrieved += toRead;
        fillBuffer();
        remaining = available - bufferIndex;
      }
    }

    return bytes;
  }

  @Override
  public String getString(int size) {
    if (size <= (available - bufferIndex)) {
      var value = new String(buffer, bufferIndex, size, StandardCharsets.UTF_8);
      bufferIndex += size;
      return value;
    } else if (size < buffer.length) {
      fillBuffer();
      throwIfInsufficientData(available, size);
      var value = new String(buffer, bufferIndex, size, StandardCharsets.UTF_8);
      bufferIndex += size;
      return value;
    }

    // here the requested string will never fit in the working buffer, we need to use the value
    // buffer
    if (valueBuffer.length < size) {
      // make sure the value buffer is sufficient to hold the value
      if (size > MAX_VALUE_BUFFER_SIZE) {
        throw new RuntimeException(
            "Attempting to load a value of "
                + size
                + " bytes, this exceeds the MAX size of "
                + MAX_VALUE_BUFFER_SIZE);
      }
      valueBuffer = new byte[size];
    }

    int retrieved = 0;
    int remaining = available - bufferIndex;

    while (retrieved < size) {
      var toRead = Math.min(size - retrieved, remaining);
      System.arraycopy(buffer, bufferIndex, valueBuffer, retrieved, toRead);
      bufferIndex += toRead;
      retrieved += toRead;
      fillBuffer();
      remaining = available - bufferIndex;
    }

    return new String(valueBuffer, 0, size, StandardCharsets.UTF_8);
  }

  @Override
  public int getWriteRemaining() {
    return 0;
  }

  @Override
  public int getReadRemaining() {
    if (available > 0 && bufferIndex != available) {
      return Integer.MAX_VALUE;
    }
    var count = fillBuffer();
    return count;
  }

  @Override
  public int readUntil(byte value) {
    keyBufferIndex = 0;
    weakHash = 0;
    while (available != 0) {
      var b = read();
      if (b != value) {
        if (keyBufferIndex + 1 == keyBuffer.length) {
          if (keyBuffer.length == MAX_KEY_BUFFER_SIZE) {
            throw new RuntimeException(
                "Attempt to create a key buffer greater than MAX_KEY_BUFFER_SIZE");
          }
          var newLength = Math.min(MAX_KEY_BUFFER_SIZE, keyBuffer.length * 2);
          keyBuffer = Arrays.copyOf(keyBuffer, newLength);
        }
        keyBuffer[keyBufferIndex++] = b;
        weakHash += b;
      } else {
        return keyBufferIndex + 1;
      }
    }

    throw new RuntimeException();
  }

  @Override
  public void skipHead(int size) {
    if (available - bufferIndex > size) {
      bufferIndex += size;
    } else {
      int retrieved = 0;
      int remaining = available - bufferIndex;

      while (retrieved < size) {
        var toRead = Math.min(size - retrieved, remaining);
        bufferIndex += toRead;
        retrieved += toRead;
        if (bufferIndex == available) {
          fillBuffer();
          remaining = available - bufferIndex;
        }
      }
    }
  }

  @Override
  public void skipTail(int size) {}

  @Override
  public int getHead() {
    return bufferIndex;
  }

  @Override
  public int getTail() {
    return 0;
  }

  @Override
  public void setHead(int position) {}

  @Override
  public void setTail(int position) {}

  @Override
  public int getLimit() {
    return available;
  }

  @Override
  public void writeDouble(double value) {
    throw new UnsupportedOperationException(WRITE_NOT_SUPPORTED);
  }

  @Override
  public void writeByte(byte value) {
    throw new UnsupportedOperationException(WRITE_NOT_SUPPORTED);
  }

  @Override
  public void writeInteger(int value) {
    throw new UnsupportedOperationException(WRITE_NOT_SUPPORTED);
  }

  @Override
  public void writeInteger(int position, int value) {
    throw new UnsupportedOperationException(WRITE_NOT_SUPPORTED);
  }

  @Override
  public void writeLong(long value) {
    throw new UnsupportedOperationException(WRITE_NOT_SUPPORTED);
  }

  @Override
  public void writeBytes(byte[] bytes) {
    throw new UnsupportedOperationException(WRITE_NOT_SUPPORTED);
  }

  @Override
  public void writeBytes(byte[] bytes, int offset, int size) {
    throw new UnsupportedOperationException(WRITE_NOT_SUPPORTED);
  }

  @Override
  public void writeString(String value) {
    throw new UnsupportedOperationException(WRITE_NOT_SUPPORTED);
  }

  @Override
  public ByteRangeComparator getByteRangeComparator() {
    return comparator;
  }

  @Override
  public byte[] toByteArray() {
    throw new UnsupportedOperationException("stream does not support toByteArray");
  }

  private class InputStreamByteRangeComparator implements ByteRangeComparator {
    @Override
    public int getWeakHash() {
      return weakHash;
    }

    @Override
    public String value() {
      return new String(keyBuffer, 0, keyBufferIndex, StandardCharsets.UTF_8);
    }

    @Override
    public boolean equalsArray(byte[] value) {
      return Arrays.equals(value, 0, value.length, keyBuffer, 0, keyBufferIndex);
    }

    @Override
    public boolean equalsArray(byte[] value, int weakHashIn) {
      if (weakHashIn != weakHash) {
        return false;
      }
      return Arrays.equals(value, 0, value.length, keyBuffer, 0, keyBufferIndex);
    }
  }
}
