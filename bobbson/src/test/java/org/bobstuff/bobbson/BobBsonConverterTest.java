package org.bobstuff.bobbson;

import java.nio.ByteBuffer;

import org.bobstuff.bobbson.writer.StackBsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BobBsonConverterTest {
  @Test
  public void readThrowsIllegalState() {
    var sut = new BobBsonConverter<String>() {};
    var buffer = ByteBuffer.allocate(10);
    var bsonReader = new BsonReaderStack(buffer);

    Assertions.assertThrows(
        IllegalStateException.class,
        () -> {
          sut.read(bsonReader);
        });
  }

  @Test
  public void writeThrowsIllegalState() {
    var sut = new BobBsonConverter<String>() {};
    byte[] bytes = new byte[100];
    BufferDataPool pool = new NoopBufferDataPool((size) -> new ByteBufferBobBsonBuffer(bytes));
    BobBsonBuffer buffer = new ByteBufferBobBsonBuffer(bytes);
    var bsonWriter = new StackBsonWriter(buffer);

    Assertions.assertThrows(IllegalStateException.class, () -> sut.write(bsonWriter, null));
  }

  @Test
  public void testWriteNoKeyThrowsIllegalState() {
    var sut = new BobBsonConverter<String>() {};
    byte[] bytes = new byte[100];
    BobBsonBuffer buffer = new ByteBufferBobBsonBuffer(bytes);
    var bsonWriter = new StackBsonWriter(buffer);

    Assertions.assertThrows(IllegalStateException.class, () -> sut.write(bsonWriter, ""));
  }
}
