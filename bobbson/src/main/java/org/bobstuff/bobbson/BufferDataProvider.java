package org.bobstuff.bobbson;

@FunctionalInterface
public interface BufferDataProvider {
  BobBsonBuffer provide(int size);
}
