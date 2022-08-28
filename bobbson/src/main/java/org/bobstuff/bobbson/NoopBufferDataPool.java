package org.bobstuff.bobbson;

public class NoopBufferDataPool implements BufferDataPool {
  private BufferDataProvider bufferDataProvider;

  public NoopBufferDataPool(BufferDataProvider bufferDataProvider) {
    this.bufferDataProvider = bufferDataProvider;
  }

  @Override
  public BobBsonBuffer allocate(int size) {
    return bufferDataProvider.provide(size);
  }

  @Override
  public void recycle(BobBsonBuffer bufferData) {
    // no-op
  }
}
