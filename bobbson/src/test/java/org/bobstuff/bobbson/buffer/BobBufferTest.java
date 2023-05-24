package org.bobstuff.bobbson.buffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BobBufferTest {
  @Test
  public void createWithTailGreaterThanDataLengthFails() {
    byte[] data = new byte[2];
    Assertions.assertThrows(IllegalArgumentException.class, () -> new BobBuffer(data, 0, 100));
  }

  @Test
  public void writeInt() {
    byte[] data = new byte[100];
    BobBuffer bobBuffer = new BobBuffer(data, 0, 0);
    bobBuffer.writeIntegerLe(48);

    ByteBuffer comp = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    Assertions.assertEquals(48, comp.getInt());
  }

  @Test
  public void writeIntAtPosition() {
    byte[] data = new byte[100];
    BobBuffer bobBuffer = new BobBuffer(data, 4, 10);
    bobBuffer.writeIntegerLe(0, 48);

    ByteBuffer comp = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    Assertions.assertEquals(48, comp.getInt());
  }

  @Test
  public void writeIntInsufficientSpace() {
    byte[] data = new byte[2];
    BobBuffer bobBuffer = new BobBuffer(data, 0, 0);
    Assertions.assertThrows(IllegalStateException.class, () -> bobBuffer.writeIntegerLe(48));
  }

  @Test
  public void readInt() {
    byte[] data = new byte[100];
    ByteBuffer comp = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    comp.putInt(48);

    BobBuffer bobBuffer = new BobBuffer(data, 0, 4);
    Assertions.assertEquals(48, bobBuffer.readIntegerLe());
  }

  @Test
  public void readIntInsufficientData() {
    byte[] data = new byte[2];
    BobBuffer bobBuffer = new BobBuffer(data, 0, 2);
    Assertions.assertThrows(IllegalStateException.class, bobBuffer::readIntegerLe);
  }

  @Test
  public void writeIntHeadWillExceedTail() {
    byte[] data = new byte[100];
    BobBuffer bobBuffer = new BobBuffer(data, 98, 100);
    Assertions.assertThrows(IllegalStateException.class, bobBuffer::readIntegerLe);
  }

  @Test
  public void writeLong() {
    byte[] data = new byte[100];
    BobBuffer bobBuffer = new BobBuffer(data, 0, 0);
    bobBuffer.writeLongLe(48L);

    ByteBuffer comp = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    Assertions.assertEquals(48L, comp.getLong());
  }

  @Test
  public void writeLongInsufficientSpace() {
    byte[] data = new byte[2];
    BobBuffer bobBuffer = new BobBuffer(data, 0, 0);
    Assertions.assertThrows(IllegalStateException.class, () -> bobBuffer.writeLongLe(48));
  }

  @Test
  public void readLong() {
    byte[] data = new byte[100];
    ByteBuffer comp = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    comp.putLong(48L);

    BobBuffer bobBuffer = new BobBuffer(data, 0, 8);
    Assertions.assertEquals(48L, bobBuffer.readLongLe());
  }

  @Test
  public void readLongInsufficientData() {
    byte[] data = new byte[2];
    BobBuffer bobBuffer = new BobBuffer(data, 0, 2);
    Assertions.assertThrows(IllegalStateException.class, bobBuffer::readLongLe);
  }

  @Test
  public void writeLongHeadWillExceedTail() {
    byte[] data = new byte[100];
    BobBuffer bobBuffer = new BobBuffer(data, 98, 100);
    Assertions.assertThrows(IllegalStateException.class, bobBuffer::readLongLe);
  }

  @Test
  public void writeByte() {
    byte[] data = new byte[100];
    BobBuffer bobBuffer = new BobBuffer(data, 0, 0);
    bobBuffer.writeByte((byte) 48);

    ByteBuffer comp = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    Assertions.assertEquals((byte) 48, comp.get());
  }

  @Test
  public void writeByteInsufficientSpace() {
    byte[] data = new byte[0];
    BobBuffer bobBuffer = new BobBuffer(data, 0, 0);
    Assertions.assertThrows(IllegalStateException.class, () -> bobBuffer.writeByte((byte) 48));
  }

  @Test
  public void readByte() {
    byte[] data = new byte[100];
    ByteBuffer comp = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    comp.put((byte) 48);

    BobBuffer bobBuffer = new BobBuffer(data, 0, 8);
    Assertions.assertEquals((byte) 48, bobBuffer.readByte());
  }

  @Test
  public void readByteInsufficientData() {
    byte[] data = new byte[0];
    BobBuffer bobBuffer = new BobBuffer(data, 0, 0);
    Assertions.assertThrows(IllegalStateException.class, bobBuffer::readByte);
  }

  @Test
  public void writeByteHeadWillExceedTail() {
    byte[] data = new byte[100];
    BobBuffer bobBuffer = new BobBuffer(data, 100, 100);
    Assertions.assertThrows(IllegalStateException.class, bobBuffer::readByte);
  }

  @Test
  public void readBytes() {
    byte[] data = new byte[100];
    ByteBuffer dataWriter = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    dataWriter.put((byte) 48);
    dataWriter.putLong(46);
    dataWriter.putLong(98);

    BobBuffer bobBuffer = new BobBuffer(data, 0, 100);
    byte[] newData = new byte[17];
    var count = bobBuffer.readBytes(newData);

    ByteBuffer comp = ByteBuffer.wrap(newData).order(ByteOrder.LITTLE_ENDIAN);
    Assertions.assertEquals((byte) 48, comp.get());
    Assertions.assertEquals(46, comp.getLong());
    Assertions.assertEquals(98, comp.getLong());

    Assertions.assertEquals(100, bobBuffer.getTail());
    Assertions.assertEquals(17, bobBuffer.getHead());
    Assertions.assertEquals(17, count);
  }

  @Test
  public void writeBytes() {
    byte[] data = new byte[17];
    ByteBuffer dataWriter = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    dataWriter.put((byte) 48);
    dataWriter.putLong(46);
    dataWriter.putLong(98);

    byte[] bobData = new byte[100];
    BobBuffer bobBuffer = new BobBuffer(bobData, 0, 0);
    bobBuffer.writeBytes(data);

    ByteBuffer comp = ByteBuffer.wrap(bobData).order(ByteOrder.LITTLE_ENDIAN);
    Assertions.assertEquals((byte) 48, comp.get());
    Assertions.assertEquals(46, comp.getLong());
    Assertions.assertEquals(98, comp.getLong());

    Assertions.assertEquals(17, bobBuffer.getTail());
    Assertions.assertEquals(0, bobBuffer.getHead());
  }

  @Test
  public void writeBytesGivenOffsets() {
    byte[] data = new byte[17];
    ByteBuffer dataWriter = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    dataWriter.put((byte) 48);
    dataWriter.putLong(46);
    dataWriter.putLong(98);

    byte[] bobData = new byte[100];
    BobBuffer bobBuffer = new BobBuffer(bobData, 0, 0);
    bobBuffer.writeBytes(data, 9, 8);

    ByteBuffer comp = ByteBuffer.wrap(bobData).order(ByteOrder.LITTLE_ENDIAN);
    Assertions.assertEquals(98, comp.getLong());

    Assertions.assertEquals(8, bobBuffer.getTail());
    Assertions.assertEquals(0, bobBuffer.getHead());
  }

  @Test
  public void writeBytesBeyondLengthOfGivenData() {
    byte[] data = new byte[17];

    byte[] bobData = new byte[100];
    BobBuffer bobBuffer = new BobBuffer(bobData, 0, 0);
    Assertions.assertThrows(IllegalStateException.class, () -> bobBuffer.writeBytes(data, 9, 80));
  }

  @Test
  public void writeBytesBeyondSizeOfBuffer() {
    byte[] data = new byte[17];

    byte[] bobData = new byte[10];
    BobBuffer bobBuffer = new BobBuffer(bobData, 0, 0);
    Assertions.assertThrows(IllegalStateException.class, () -> bobBuffer.writeBytes(data, 0, 17));
  }

  @Test
  public void getArrayReturnsBackingArray() {
    byte[] bobData = new byte[10];
    BobBuffer bobBuffer = new BobBuffer(bobData, 0, 0);

    Assertions.assertEquals(bobData, bobBuffer.getArray());
  }

  @Test
  public void getLimitReturnsLimit() {
    byte[] bobData = new byte[10];
    BobBuffer bobBuffer = new BobBuffer(bobData, 0, 1);

    Assertions.assertEquals(10, bobBuffer.getLimit());
  }

  @Test
  public void getTailReturnsTailPosition() {
    byte[] bobData = new byte[10];
    BobBuffer bobBuffer = new BobBuffer(bobData, 0, 10);

    Assertions.assertEquals(10, bobBuffer.getTail());
  }

  @Test
  public void getHeadReturnsHeadPosition() {
    byte[] bobData = new byte[10];
    BobBuffer bobBuffer = new BobBuffer(bobData, 3, 10);

    Assertions.assertEquals(3, bobBuffer.getHead());
  }

  @Test
  public void setHeadSetsHeadPosition() {
    byte[] bobData = new byte[10];
    BobBuffer bobBuffer = new BobBuffer(bobData, 3, 10);

    bobBuffer.setHead(5);

    Assertions.assertEquals(5, bobBuffer.getHead());
  }

  @Test
  public void setHeadFailsWhenHeadGreaterThanTail() {
    byte[] bobData = new byte[10];
    BobBuffer bobBuffer = new BobBuffer(bobData, 3, 10);

    Assertions.assertThrows(IllegalArgumentException.class, () -> bobBuffer.setHead(50));
  }

  @Test
  public void skipHeadMovesHeadPosition() {
    byte[] bobData = new byte[10];
    BobBuffer bobBuffer = new BobBuffer(bobData, 3, 10);

    bobBuffer.skipHead(5);

    Assertions.assertEquals(8, bobBuffer.getHead());
  }

  @Test
  public void skipHeadFailsWhenHeadGreaterThanTail() {
    byte[] bobData = new byte[10];
    BobBuffer bobBuffer = new BobBuffer(bobData, 3, 10);

    Assertions.assertThrows(IllegalArgumentException.class, () -> bobBuffer.skipHead(50));
  }

  @Test
  public void setTailSetsTailPosition() {
    byte[] bobData = new byte[10];
    BobBuffer bobBuffer = new BobBuffer(bobData, 3, 10);

    bobBuffer.setTail(5);

    Assertions.assertEquals(5, bobBuffer.getTail());
  }

  @Test
  public void setTailFailsWhenTailGreaterThanLimit() {
    byte[] bobData = new byte[10];
    BobBuffer bobBuffer = new BobBuffer(bobData, 3, 10);

    Assertions.assertThrows(IllegalArgumentException.class, () -> bobBuffer.setTail(50));
  }

  @Test
  public void skipTailMovesTailPosition() {
    byte[] bobData = new byte[10];
    BobBuffer bobBuffer = new BobBuffer(bobData, 3, 5);

    bobBuffer.skipTail(5);

    Assertions.assertEquals(10, bobBuffer.getTail());
  }

  @Test
  public void skipTailFailsWhenHeadGreaterThanLimit() {
    byte[] bobData = new byte[10];
    BobBuffer bobBuffer = new BobBuffer(bobData, 3, 10);

    Assertions.assertThrows(IllegalArgumentException.class, () -> bobBuffer.skipTail(50));
  }

  @Test
  public void writeRemainingReturnsRemainingSpace() {
    byte[] bobData = new byte[10];
    BobBuffer bobBuffer = new BobBuffer(bobData, 3, 8);

    Assertions.assertEquals(2, bobBuffer.writeRemaining());
  }

  @Test
  public void readUtf8String() {
    byte[] data = new byte[100];
    ByteBuffer dataWriter = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    dataWriter.put("testing".getBytes(StandardCharsets.UTF_8));

    BobBuffer bobBuffer = new BobBuffer(data, 0, 100);
    String result = bobBuffer.readUtf8String("testing".getBytes(StandardCharsets.UTF_8).length);

    Assertions.assertEquals("testing", result);
  }

  @Test
  public void readUtf8StringFailsBeyondBufferLimit() {
    byte[] data = new byte[100];
    BobBuffer bobBuffer = new BobBuffer(data, 0, 100);
    Assertions.assertThrows(IllegalStateException.class, () -> bobBuffer.readUtf8String(200));
  }
}
