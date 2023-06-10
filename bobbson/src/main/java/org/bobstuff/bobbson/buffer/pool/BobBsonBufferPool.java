package org.bobstuff.bobbson.buffer.pool;

import org.bobstuff.bobbson.buffer.BobBsonBuffer;

/** A pool that provides {@code BobBsonBuffer} instances and supports recycling. */
public interface BobBsonBufferPool {
  /**
   * Allocate or return an instance of BobBsonBuffer that is safe for caller to use
   *
   * <p>Buffer returned can be greater than the size requested but will must always to sufficient to
   * hold the requested size
   *
   * @param size minumum number of bytes buffer must support
   * @return instance of BobBsonBuffer
   */
  BobBsonBuffer allocate(int size);

  /**
   * Return an instance of {@code BobBsonBuffer}, allowing it to be used by another caller in the
   * future. User is required to drop their reference after calling recycle. Continuing to use a
   * recycled buffer will cause undefined behaviour.
   *
   * @param bobBsonBuffer the buffer to be recycled
   */
  void recycle(BobBsonBuffer bobBsonBuffer);
}
