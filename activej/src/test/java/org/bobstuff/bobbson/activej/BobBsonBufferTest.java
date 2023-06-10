package org.bobstuff.bobbson.activej;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.bobstuff.bobbson.buffer.BobBsonBuffer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public abstract class BobBsonBufferTest {
  protected ByteBuffer comp;
  protected byte[] data;
  protected BobBsonBuffer writeSut;
  protected BobBsonBuffer readSut;

  protected BobBsonBuffer writeSmallSut;
  protected BobBsonBuffer readSmallSut;

  @Test
  public void testWriteInteger() {
    writeSut.writeInteger(14);

    Assertions.assertEquals(14, comp.getInt());
  }

  @Test
  public void testWriteByte() {
    writeSut.writeByte((byte) 14);

    Assertions.assertEquals((byte) 14, comp.get());
  }

  @Test
  public void testWriteDouble() {
    writeSut.writeDouble(29.38d);

    Assertions.assertEquals(29.38d, comp.getDouble());
  }

  @Test
  public void testWriteLong() {
    writeSut.writeLong(290034959382L);

    Assertions.assertEquals(290034959382L, comp.getLong());
  }

  @Test
  public void testWriteBytes() {
    byte[] bytes = "some bytes".getBytes(StandardCharsets.UTF_8);
    writeSut.writeBytes(bytes);

    byte[] result = new byte[bytes.length];
    comp.get(result);
    Assertions.assertArrayEquals(bytes, result);
  }

  @Test
  public void testWriteBytesOffsetSize() {
    byte[] bytes = "some bytes".getBytes(StandardCharsets.UTF_8);
    writeSut.writeBytes(bytes, 5, 5);

    byte[] result = new byte[5];
    comp.get(result);
    Assertions.assertArrayEquals("bytes".getBytes(StandardCharsets.UTF_8), result);
  }

  @Test
  public void testWriteIntegerAtPosition() {
    writeSut.skipTail(4);
    writeSut.writeDouble(12d);
    writeSut.writeInteger(0, 4);

    Assertions.assertEquals(4, comp.getInt());
    Assertions.assertEquals(12d, comp.getDouble());
  }

  @Test
  public void testWriteAsciiString() {
    String test = "simple ascii";
    writeSut.writeString(test);

    byte[] result = new byte[test.getBytes(StandardCharsets.UTF_8).length];
    comp.get(result);

    Assertions.assertEquals(test, new String(result, StandardCharsets.UTF_8));
  }

  @Test
  public void testWriteUtf8String() {
    String test = "\uD835\uDC1Bâ\uD835\uDDC7ẩṋȁ";
    writeSut.writeString(test);

    byte[] result = new byte[test.getBytes(StandardCharsets.UTF_8).length];
    comp.get(result);

    Assertions.assertEquals(test, new String(result, StandardCharsets.UTF_8));
  }

  @Test
  public void testWriteInvalidUtf8String2() {
    String test = "\u0000FFFD";
    writeSut.writeString(test);

    byte[] result = new byte[test.getBytes(StandardCharsets.UTF_8).length];
    comp.get(result);

    Assertions.assertEquals(test, new String(result, StandardCharsets.UTF_8));
  }

  @Test
  public void testWriteInvalidUtf8String() {
    String test = "���";
    writeSut.writeString(test);

    byte[] result = new byte[test.getBytes(StandardCharsets.UTF_8).length];
    comp.get(result);

    Assertions.assertEquals(test, new String(result, StandardCharsets.UTF_8));
  }

  @Test
  public void testGetHead() {
    writeSut.writeInteger(409);
    Assertions.assertEquals(0, writeSut.getHead());

    writeSut.getInt();
    Assertions.assertEquals(4, writeSut.getHead());
  }

  @Test
  public void testGetTail() {
    Assertions.assertEquals(0, writeSut.getTail());
    writeSut.writeInteger(409);
    Assertions.assertEquals(4, writeSut.getTail());
  }

  @Test
  public void testSetHead() {
    writeSut.writeInteger(409);
    writeSut.writeInteger(2);
    writeSut.writeInteger(4000);

    Assertions.assertEquals(0, writeSut.getHead());
    writeSut.setHead(8);
    Assertions.assertEquals(4000, writeSut.getInt());
    Assertions.assertEquals(12, writeSut.getHead());
  }

  @Test
  public void testSetTail() {
    Assertions.assertEquals(0, writeSut.getTail());
    writeSut.writeInteger(409);
    writeSut.writeInteger(2);
    writeSut.writeInteger(4000);
    writeSut.setTail(0);
    writeSut.writeInteger(88);
    Assertions.assertEquals(88, writeSut.getInt());
    Assertions.assertEquals(4, writeSut.getTail());
  }

  @Test
  public void testReadAsciiString() {
    String test = "simple ascii";
    comp.put(test.getBytes(StandardCharsets.UTF_8));

    var result = readSut.getString(test.getBytes(StandardCharsets.UTF_8).length);

    Assertions.assertEquals(test, result);
  }

  @Test
  public void testReadUtf8String() {
    String test = "\uD835\uDC1Bâ\uD835\uDDC7ẩṋȁ";
    comp.put(test.getBytes(StandardCharsets.UTF_8));

    var result = readSut.getString(test.getBytes(StandardCharsets.UTF_8).length);

    Assertions.assertEquals(test, result);
  }

  @Test
  public void testReadInvalidUtf8String() {
    String test = "���";
    comp.put(test.getBytes(StandardCharsets.UTF_8));

    var result = readSut.getString(test.getBytes(StandardCharsets.UTF_8).length);

    Assertions.assertEquals(test, result);
  }

  @Test
  public void testGetLimitReturnsLimit() {
    Assertions.assertEquals(1000, writeSut.getLimit());
  }

  @Test
  public void testGetRemaining() {
    Assertions.assertEquals(1000, writeSut.getWriteRemaining());

    writeSut.writeLong(2L);

    Assertions.assertEquals(992, writeSut.getWriteRemaining());
  }

  @Test
  public void testSkipHead() {
    Assertions.assertEquals(0, readSut.getHead());
    readSut.skipHead(30);
    Assertions.assertEquals(30, readSut.getHead());
  }

  @Test
  public void testSkipTail() {
    Assertions.assertEquals(0, writeSut.getTail());
    writeSut.skipTail(30);
    Assertions.assertEquals(30, writeSut.getTail());
  }

  @Test
  public void testReadUntil() {
    String test = "simple ascii";
    comp.put(test.getBytes(StandardCharsets.UTF_8));
    comp.put((byte) 0);
    comp.putInt(2349);

    var result = readSut.readUntil((byte) 0);

    var byteRange = readSut.getByteRangeComparator();

    Assertions.assertEquals(test, byteRange.value());
    Assertions.assertEquals(test.getBytes(StandardCharsets.UTF_8).length + 1, result);
  }

  @Test
  public void testReadDouble() {
    comp.putDouble(234.234);

    Assertions.assertEquals(234.234, readSut.getDouble());
  }

  @Test
  public void testReadLong() {
    comp.putLong(234234);

    Assertions.assertEquals(234234, readSut.getLong());
  }

  @Test
  public void testReadByte() {
    comp.put((byte) 4);

    Assertions.assertEquals(4, readSut.getByte());
  }

  @Test
  public void testReadInt() {
    comp.putInt(4);

    Assertions.assertEquals(4, readSut.getInt());
  }

  @Test
  public void testReadBytes() {
    byte[] bytes = new byte[] {2, 3, 4, 5, 6};
    comp.put(bytes);

    Assertions.assertArrayEquals(bytes, readSut.getBytes(5));
  }
}
