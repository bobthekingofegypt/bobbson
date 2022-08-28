package org.bobstuff.bobbson;

public class BobBsonBinary {
  private final byte type;
  private final byte[] data;

  public BobBsonBinary(byte[] data) {
    this((byte) 0, data);
  }

  public BobBsonBinary(byte type, byte[] data) {
    this.type = type;
    this.data = data;
  }

  public byte getType() {
    return type;
  }

  public byte[] getData() {
    return data;
  }
}
