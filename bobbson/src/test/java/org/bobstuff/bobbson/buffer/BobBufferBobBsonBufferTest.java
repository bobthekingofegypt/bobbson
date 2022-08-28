package org.bobstuff.bobbson.buffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BobBufferBobBsonBufferTest extends BobBsonBufferTest {
  @BeforeEach
  public void setup() {
    data = new byte[1000];
    writeSut = new BobBufferBobBsonBuffer(data, 0, 0);
    readSut = new BobBufferBobBsonBuffer(data, 0, 1000);
    comp = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
  }

  @Test
  public void testGetArray() {
    Assertions.assertNotNull(writeSut.getArray());
  }

  @Test
  public void testGetRawBobBuffer() {
    Assertions.assertNotNull(((BobBufferBobBsonBuffer) readSut).getBobBuffer());
  }

  @Test
  public void testDataConstructor() {
    var sut = new BobBufferBobBsonBuffer(new byte[3]);

    Assertions.assertEquals(0, sut.getHead());
    Assertions.assertEquals(3, sut.getTail());
  }

  @Test
  public void testDataHeadSetConstructor() {
    var sut = new BobBufferBobBsonBuffer(new byte[3], 2);

    Assertions.assertEquals(2, sut.getHead());
    Assertions.assertEquals(3, sut.getTail());
  }

  @Test
  public void testDataHeadAndTailSetConstructor() {
    var sut = new BobBufferBobBsonBuffer(new byte[3], 1, 2);

    Assertions.assertEquals(1, sut.getHead());
    Assertions.assertEquals(2, sut.getTail());
  }

  @Test
  public void testBobBufferToConstructor() {
    BobBuffer buffer = new BobBuffer(new byte[5], 1, 3);
    var sut = new BobBufferBobBsonBuffer(buffer);

    Assertions.assertEquals(1, sut.getHead());
    Assertions.assertEquals(3, sut.getTail());
  }
}