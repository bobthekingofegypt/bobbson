package org.bobstuff.bobbson;

public interface BufferDataPool {
  BobBsonBuffer allocate(int size);

  void recycle(BobBsonBuffer bufferData);
}
