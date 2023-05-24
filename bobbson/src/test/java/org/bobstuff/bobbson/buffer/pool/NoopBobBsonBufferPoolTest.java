package org.bobstuff.bobbson.buffer.pool;

import org.bobstuff.bobbson.buffer.BobBufferBobBsonBuffer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NoopBobBsonBufferPoolTest {
  @Test
  public void provideUsingProvider() {
    var buffer = new BobBufferBobBsonBuffer(new byte[0], 0, 0);
    var sut = new NoopBobBsonBufferPool((size) -> buffer);

    Assertions.assertEquals(sut.allocate(0), buffer);
  }
}
