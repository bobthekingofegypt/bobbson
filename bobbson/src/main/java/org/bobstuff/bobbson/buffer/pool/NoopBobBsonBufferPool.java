package org.bobstuff.bobbson.buffer.pool;

import org.bobstuff.bobbson.buffer.BobBsonBuffer;

/**
 * {@code BobBsonBufferPool} implementation that always returns a new array and provides no
 * recycling.
 */
public class NoopBobBsonBufferPool implements BobBsonBufferPool {
  private final BobBsonBufferProvider bobBsonBufferProvider;

  /**
   * Instantiate no-op pool that provides new BobBsonBuffers using the given provider.
   *
   * @param bobBsonBufferProvider implementation of {@code BobBsonBufferProvider} that creates
   *     buffer
   */
  public NoopBobBsonBufferPool(BobBsonBufferProvider bobBsonBufferProvider) {
    this.bobBsonBufferProvider = bobBsonBufferProvider;
  }

  @Override
  public BobBsonBuffer allocate(int size) {
    return bobBsonBufferProvider.provide(size);
  }

  /**
   * no-op recycle method that does nothing
   *
   * @param bufferData the buffer to be recycled
   */
  @Override
  public void recycle(BobBsonBuffer bufferData) {
    // no-op
  }

  /** Provider of BobBsonBuffer instances */
  @FunctionalInterface
  public interface BobBsonBufferProvider {
    /**
     * Get an instance of BobBsonBuffer of at least the requested size
     *
     * @param sizeInBytes the minimum size of the returned buffer
     * @return buffer of the required size
     */
    BobBsonBuffer provide(int sizeInBytes);
  }
}
