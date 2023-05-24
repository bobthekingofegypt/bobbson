package org.bobstuff.bobbson;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import org.bobstuff.bobbson.buffer.ByteBufferBobBsonBuffer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ByteBufferDataTest {
  @Test
  public void testGetInt() {
    byte[] data = new byte[20];
    ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    buffer.putInt(14);

    var sut = new ByteBufferBobBsonBuffer(data);
    Assertions.assertEquals(14, sut.getInt());
  }

  @Test
  public void testGetIntWrongOnBigEndianInt() {
    byte[] data = new byte[20];
    ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
    buffer.putInt(14);

    var sut = new ByteBufferBobBsonBuffer(data);
    Assertions.assertEquals(234881024, sut.getInt());
  }

  @Test
  public void testGetIntMultipleTimes() {
    byte[] data = new byte[20];
    ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    buffer.putInt(14);
    buffer.putInt(4);
    buffer.putInt(72);

    var sut = new ByteBufferBobBsonBuffer(data);
    Assertions.assertEquals(14, sut.getInt());
    Assertions.assertEquals(4, sut.getInt());
    Assertions.assertEquals(72, sut.getInt());
  }

  @Test
  public void testGet() {
    byte[] data = new byte[20];
    ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    buffer.put((byte) 14);

    var sut = new ByteBufferBobBsonBuffer(data);
    Assertions.assertEquals(14, sut.getByte());
  }

  @Test
  public void testGetMultiple() {
    byte[] data = new byte[20];
    ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    buffer.put((byte) 14);
    buffer.put((byte) 4);
    buffer.put((byte) 72);

    var sut = new ByteBufferBobBsonBuffer(data);
    Assertions.assertEquals(14, sut.getByte());
    Assertions.assertEquals(4, sut.getByte());
    Assertions.assertEquals(72, sut.getByte());
  }

  @Test
  public void testGetLong() {
    byte[] data = new byte[20];
    ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    buffer.putLong(193485737329238L);

    var sut = new ByteBufferBobBsonBuffer(data);
    Assertions.assertEquals(193485737329238L, sut.getLong());
  }

  @Test
  public void testGetLongMultiple() {
    byte[] data = new byte[60];
    ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    buffer.putLong(14L);
    buffer.putLong(4000000000L);
    buffer.putLong(72L);

    var sut = new ByteBufferBobBsonBuffer(data);
    Assertions.assertEquals(14L, sut.getLong());
    Assertions.assertEquals(4000000000L, sut.getLong());
    Assertions.assertEquals(72L, sut.getLong());
  }

  @Test
  public void testReadString() {
    byte[] data = new byte[60];
    ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    byte[] textBytes = "this is a string".getBytes(StandardCharsets.UTF_8);
    buffer.put(textBytes);

    var sut = new ByteBufferBobBsonBuffer(data);
    Assertions.assertEquals("this is a string", sut.getString(textBytes.length));
  }

  @Test
  public void testReadUntil() {
    byte[] data = new byte[60];
    ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    byte[] textBytes = "this is a string".getBytes(StandardCharsets.UTF_8);
    buffer.put(textBytes);
    buffer.put((byte) 0);
    buffer.put((byte) 49);

    var sut = new ByteBufferBobBsonBuffer(data);

    sut.readUntil((byte) 0);
    Assertions.assertEquals("this is a string", sut.getByteRangeComparator().value());
    Assertions.assertEquals((byte) 49, sut.getByte());
  }
}
