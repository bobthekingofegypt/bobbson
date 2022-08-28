package org.bobstuff.bobbson.buffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.bobstuff.bobbson.ByteBufferBobBsonBuffer;
import org.junit.jupiter.api.BeforeEach;

public class ByteBufferBobBsonBufferTest extends BobBsonBufferTest {
  @BeforeEach
  public void setup() {
    data = new byte[1000];
    writeSut = new ByteBufferBobBsonBuffer(data);
    readSut = new ByteBufferBobBsonBuffer(data, 0, 1000);
    comp = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
  }
}
