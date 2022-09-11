package org.bobstuff.bobbson;

import org.checkerframework.checker.nullness.qual.Nullable;

public interface BobBsonBuffer {
  /**
   * Return the underlying array of the buffer or null if access is not allowed. Providing raw
   * access allows for performance improvements on writes by avoiding repeated calls to write
   * individual bytes.
   *
   * @return raw byte array or null
   */
  byte @Nullable [] getArray();

  int getInt();

  byte getByte();

  long getLong();

  double getDouble();

  byte[] getBytes(int size);

  String getString(int size);

  int getWriteRemaining();

  int getReadRemaining();
  /**
   * Moves the buffer forward until the byte value is encountered. Returns the number of bytes read
   * and internally stores the byte range read, so it can be accessed through getByteRangeComparitor
   *
   * @param value
   * @return
   */
  int readUntil(byte value);

  void skipHead(int size);

  void skipTail(int size);

  int getHead();

  int getTail();

  void setHead(int position);

  void setTail(int position);

  int getLimit();

  void writeDouble(double value);

  void writeByte(byte value);

  void writeInteger(int value);

  void writeInteger(int position, int value);

  void writeLong(long value);

  void writeBytes(byte[] bytes);

  void writeBytes(byte[] bytes, int offset, int size);

  void writeString(String value);

  /**
   * Returns the ByteRangeComparitor, the value this comparitor compares against is the value that
   * was last read by readUntil. This cannot be stored and used to compare against a historic value
   * at a later date. It is always a window into the the last value read by readUntil. It's main
   * purpose is to compare document keys without having to decode the UTF-8 values.
   *
   * @return the byte range comparitor of readUntil operations
   */
  ByteRangeComparitor getByteRangeComparitor();

  interface ByteRangeComparitor {
    int weakHash();

    String name();

    boolean equalsArray(byte[] array);

    boolean equalsArray(byte[] array, int weakHash);
  }
}
