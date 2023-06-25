package org.bobstuff.bobbson;

import org.bobstuff.bobbson.buffer.ByteBufferBobBsonBuffer;
import org.bobstuff.bobbson.buffer.DynamicBobBsonBuffer;
import org.bobstuff.bobbson.buffer.pool.BobBsonBufferPool;
import org.bobstuff.bobbson.buffer.pool.NoopBobBsonBufferPool;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.reader.StackBsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bobstuff.bobbson.writer.StackBsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

public class BeanWithMethodNoFieldTest {

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BobBsonProvider.class)
  public void testReadWrite(BobBsonProvider.BobBsonImplProvider configurationProvider)
      throws Exception {
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new ByteBufferBobBsonBuffer(new byte[100]));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);

    var bean = new BeanWithMethodNoField();

    BobBson bobBson = configurationProvider.provide();
    BsonWriter writer = new StackBsonWriter(buffer);
    bobBson.serialise(bean, BeanWithMethodNoField.class, writer);

    var bytes = buffer.toByteArray();
    BsonReader reader = new StackBsonReader(new ByteBufferBobBsonBuffer(bytes));

    reader.readStartDocument();
    Assertions.assertEquals(BsonType.STRING, reader.readBsonType());
    Assertions.assertEquals("notname", reader.currentFieldName());
    Assertions.assertEquals("Fred", reader.readString());
    Assertions.assertEquals(BsonType.ARRAY, reader.readBsonType());
    Assertions.assertEquals("names", reader.currentFieldName());
    reader.readStartArray();
    Assertions.assertEquals(BsonType.STRING, reader.readBsonType());
    Assertions.assertEquals("bob", reader.readString());
    Assertions.assertEquals(BsonType.STRING, reader.readBsonType());
    Assertions.assertEquals("scott", reader.readString());

    //        var result = bobBson.deserialise(BeanWithMethodNoField.class, reader);

  }
}
