package org.bobstuff.bobbson.buffer;

import org.checkerframework.checker.nullness.qual.Nullable;

/** A buffer that can read and write raw bytes that make up bson data. */
public interface BobBsonBuffer {
  /**
   * Return the underlying array of the buffer or null if access is not allowed. Providing raw
   * access can allow for performance improvements on writes by avoiding repeated calls to write
   * individual bytes.
   *
   * @return raw byte array or null
   */
  byte @Nullable [] getArray();

  /**
   * Reads a 32bit integer value from the buffer
   *
   * @return int value read from the buffer
   */
  int getInt();

  /**
   * Reads a single byte from the buffer
   *
   * @return byte read
   */
  byte getByte();

  /**
   * Reads a 64bit number from the buffer
   *
   * @return long value read from the buffer
   */
  long getLong();

  /**
   * Reads a 64bit double from the buffer
   *
   * @return double value read from the buffer
   */
  double getDouble();

  /**
   * Reads {@code size} bytes from the buffer and returns byte array containing values. Allocates a
   * new byte array during call
   *
   * @param size number of bytes to read
   * @return data read from buffer
   */
  byte[] getBytes(int size);

  /**
   * Read a string of {@code size} bytes from the buffer.
   *
   * @param size number of bytes that represent the string
   * @return decoded string instance
   */
  String getString(int size);

  /**
   * Write remaining is the space left to write new data to the underlying buffer
   *
   * @return number of bytes space left to write to
   */
  int getWriteRemaining();

  /**
   * Read remaining is how many bytes are left to be read before the head marker reaches the tail
   * marker and there is no more new data in the buffer
   *
   * @return number of bytes left to be read
   */
  int getReadRemaining();

  /**
   * Moves the buffer forward until the byte value is encountered. Returns the number of bytes read
   * and internally stores the byte range read, so it can be accessed through getByteRangeComparitor
   *
   * @param value  byte value to look for
   * @return number of bytes read, or -1 if value never reached
   */
  int readUntil(byte value);

  /**
   * Move the head of the buffer forward by {@code size} bytes
   * @param size  number of bytes to move head
   */
  void skipHead(int size);

  /**
   * Move the tail of the buffer forward by {@code size} bytes so next write operation happens at tail + size location
   * @param size  number of bytes to move tail
   */
  void skipTail(int size);

  /**
   * Gets the head position of the buffer, this is the location where the next read will happen.
   * @return current head position
   */
  int getHead();

  /**
   * Gets the tail position of the buffer, this is the location where the next write will happen.
   * @return current tail position
   */
  int getTail();

  /**
   * Sets the current head position to given position, this is the location where the next read will happen
   * @param position  new head position
   */
  void setHead(int position);

  /**
   * Sets the current tail position to give position, this is the location where the next write will happen
   * @param position  new tail position
   */
  void setTail(int position);

  /**
   * Gets the current limit on the buffer.  This is the maximum value that tail can be set to and indicates the maximum length of the buffer
   * @return the buffers limit
   */
  int getLimit();

  /**
   * Write 64bit double value to the buffer
   * @param value  double value to write
   */
  void writeDouble(double value);

  /**
   * Write single byte value to the buffer
   * @param value byte to write
   */
  void writeByte(byte value);

  /**
   * Write 32bit integer to the buffer
   * @param value  int value to write
   */
  void writeInteger(int value);

  /**
   * Writes 32bit integer at the given position
   * @param position  position in buffer to write int value
   * @param value  int value to write
   */
  void writeInteger(int position, int value);

  /**
   * Write 64bit long to the buffer
   * @param value long to write
   */
  void writeLong(long value);

  /**
   * Write the given bytes into the buffer
   * @param bytes  data to write into buffer
   */
  void writeBytes(byte[] bytes);

  /**
   * Write from the given bytes array values from the offset upto offset + size
   * @param bytes  array containing bytes to be writen
   * @param offset  offset into provided array to start
   * @param size  number of bytes to read from given byte array
   */
  void writeBytes(byte[] bytes, int offset, int size);

  /**
   * Write the given string to the buffer, encoded in UTF-8
   * @param value string to write
   */
  void writeString(String value);

  /**
   * Returns the ByteRangeComparator, the value this comparator compares against is the value that
   * was last read by readUntil. This cannot be stored and used to compare against a historic value
   * at a later date. It is always a window into the the last value read by readUntil. It's main
   * purpose is to compare document keys without having to decode the UTF-8 values.
   *
   * @return the byte range comparator of readUntil operations
   */
  ByteRangeComparator getByteRangeComparator();

  /**
   * Convert buffer into a single byte array of size tail.
   * @return buffer data
   */
  byte[] toByteArray();

  /**
   * Comparator for byte ranges read in through {@code BobBsonBuffer.readUntil}.  Used to avoid decoding UTF8 strings where raw comparisons are possible
   */
  interface ByteRangeComparator {
    /**
     * Gets the current weak hash of the bytes available in the comparator.
     * <p>
     * The weak hash is the sum of all byte values present in the range
     * @return the current weak hash
     */
    int getWeakHash();

    /**
     * Decode the bytes in the range and return the String representation
     * @return value of the current range
     */
    String value();

    /**
     * Compares the given array to the current byte range in the comparator
     * @param value  byte array to compare against
     * @return true if value matches comparator range
     */
    boolean equalsArray(byte[] value);

    /**
     * Compares the given hash value to the comparators hash and if they match compares the raw arrays, if they don't returns false straight away.
     * @param value  byte array to compare against
     * @param weakHash  hash to compare against
     * @return true if hash and value match, otherwise false
     */
    boolean equalsArray(byte[] value, int weakHash);
  }
}
