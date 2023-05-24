package org.bobstuff.bobbson;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.bobstuff.bobbson.buffer.BobBsonBuffer;

// TODO need direct array version of this class for when array is not available
public class BobBsonByteRange implements BobBsonBuffer.ByteRangeComparator {
  private int start;
  private int size;
  private int weakHash;
  private byte[] data;

  public BobBsonByteRange(byte[] data) {
    this.data = data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public void set(int start, int size, int weakHash) {
    this.start = start;
    this.size = size;
    this.weakHash = weakHash;
  }

  public int getWeakHash() {
    return weakHash;
  }

  public String value() {
    return new String(data, start, size - 1, StandardCharsets.UTF_8);
  }

  public boolean equalsArray(byte[] array) {
    return Arrays.equals(data, start, start + size - 1, array, 0, array.length);
  }

  public boolean equalsArray(byte[] array, int otherWeakHash) {
    if (weakHash != otherWeakHash) {
      return false;
    }
    if (array.length < 10) {
      if (array.length != size - 1) {
        return false;
      }
      for (int i = 0; i < array.length; i++) {
        if (array[i] != data[start + i]) {
          return false;
        }
      }
      return true;
    }
    return Arrays.equals(data, start, start + size - 1, array, 0, array.length);
  }
}
