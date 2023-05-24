package org.bobstuff.bobbson.buffer.pool;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConcurrentBobBsonBufferPoolTest {
  @Test
  public void testAllocateReturnsBuffer() {
    var sut = new ConcurrentBobBsonBufferPool();
    var result = sut.allocate(1024);

    Assertions.assertEquals(1024, result.getLimit());

    var result2 = sut.allocate(1024);

    Assertions.assertNotEquals(result, result2);
  }

  @Test
  public void testAllocateAfterRecycleReturnsOld() {
    var sut = new ConcurrentBobBsonBufferPool();
    var result = sut.allocate(1024);
    sut.recycle(result);

    Assertions.assertEquals(1, sut.getPoolSize(1024));

    var result2 = sut.allocate(1024);

    Assertions.assertEquals(result, result2);
  }
}
