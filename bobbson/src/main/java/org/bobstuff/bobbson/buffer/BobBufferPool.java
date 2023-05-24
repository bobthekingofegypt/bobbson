package org.bobstuff.bobbson.buffer;

import java.util.concurrent.ConcurrentLinkedQueue;
import org.bobstuff.bobbson.buffer.pool.BobBsonBufferPool;

public class BobBufferPool implements BobBsonBufferPool {
  private ConcurrentLinkedQueue<BobBsonBuffer>[] slabs;

  @SuppressWarnings("unchecked")
  public BobBufferPool() {
    this.slabs = new ConcurrentLinkedQueue[32];
    for (var i = 0; i < 32; i += 1) {
      this.slabs[i] = new ConcurrentLinkedQueue<>();
    }
  }

  @Override
  public BobBsonBuffer allocate(int size) {
    int index = 32 - Integer.numberOfLeadingZeros(size - 1);
    var queue = slabs[index];
    BobBsonBuffer buffer = queue.poll();
    if (buffer != null) {
      buffer.setTail(0);
      buffer.setHead(0);
    } else {
      buffer = new BobBufferBobBsonBuffer(new byte[index == 32 ? 0 : 1 << index], 0, 0);
    }
    return buffer;
  }

  @Override
  public void recycle(BobBsonBuffer bufferData) {
    // TODO change this to use a capacity concept across all buffers
    var array = bufferData.getArray();
    if (array == null) {
      throw new IllegalStateException("trying to access a buffer not backed by simple array");
    }
    int slab = 32 - Integer.numberOfLeadingZeros(array.length - 1);
    var queue = slabs[slab];
    queue.offer(bufferData);
  }
}
