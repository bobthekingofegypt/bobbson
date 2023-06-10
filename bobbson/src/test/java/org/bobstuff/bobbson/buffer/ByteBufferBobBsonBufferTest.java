package org.bobstuff.bobbson.buffer;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ByteBufferBobBsonBufferTest extends BobBsonBufferTest {
  @BeforeEach
  public void setup() {
    data = new byte[1000];
    writeSut = new ByteBufferBobBsonBuffer(data);
    readSut = new ByteBufferBobBsonBuffer(data, 0, 1000);
    comp = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
  }

  @Test
  public void testPipe() throws Exception {
    byte[] data = new byte[4];
    ByteBuffer comp = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    comp.putInt(48);
    var sut = new ByteBufferBobBsonBuffer(data, 0, 4);

    var outputStream = new ByteArrayOutputStream(100);
    sut.pipe(outputStream);

    var result = outputStream.toByteArray();

    Assertions.assertArrayEquals(data, result);
  }
}
