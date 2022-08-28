package org.bobstuff.bobbson;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

// TODO need direct array version of this class for when array is not available
public class BobBsonByteRange implements BobBsonBuffer.ByteRangeComparitor {
  private int start;
  private int size;
  private int weakHash;
  private byte[] data;

  public BobBsonByteRange(byte[] data) {
    this.data = data;
  }

  public void set(int start, int size, int weakHash) {
    this.start = start;
    this.size = size;
    this.weakHash = weakHash;
  }

  public String name() {
    return new String(data, start, size - 1, StandardCharsets.UTF_8);
  }

  public boolean equalsArray(byte[] array) {
    return Arrays.equals(data, start, start + size - 1, array, 0, array.length);
  }

  public boolean equalsArray(byte[] array, int otherWeakHash) {
    if (weakHash != otherWeakHash) {
      return false;
    }
    return Arrays.equals(data, start, start + size - 1, array, 0, array.length);
  }
}
