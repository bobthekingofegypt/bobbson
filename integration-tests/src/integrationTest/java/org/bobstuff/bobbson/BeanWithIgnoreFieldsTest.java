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

public class BeanWithIgnoreFieldsTest {
  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BobBsonProvider.class)
  public void testRead(BobBsonProvider.BobBsonImplProvider bobBsonProvider) throws Exception {
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new ByteBufferBobBsonBuffer(new byte[100]));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);

    BsonWriter writer = new StackBsonWriter(buffer);
    writer.writeStartDocument();
    writer.writeString("name", "bob");
    writer.writeString("description", "a description");
    writer.writeString("nottitle", "mr");
    writer.writeEndDocument();

    var bytes = buffer.toByteArray();

    BobBson bobBson = bobBsonProvider.provide();
    BsonReader reader = new StackBsonReader(new ByteBufferBobBsonBuffer(bytes));
    var result = bobBson.deserialise(BeanWithIgnoreFields.class, reader);
    Assertions.assertEquals("bob", result.getName());
    Assertions.assertNull(result.getDescription());
    Assertions.assertEquals("mr", result.getTitle());
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BobBsonProvider.class)
  public void testWrite(BobBsonProvider.BobBsonImplProvider bobBsonProvider) throws Exception {
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new ByteBufferBobBsonBuffer(new byte[100]));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);
    BobBson bobBson = bobBsonProvider.provide();

    BeanWithIgnoreFields value = new BeanWithIgnoreFields();
    value.setName("bob");
    value.setDescription("a description");
    value.setTitle("mr");

    BsonWriter writer = new StackBsonWriter(buffer);
    bobBson.serialise(value, BeanWithIgnoreFields.class, writer);

    var bytes = buffer.toByteArray();

    BsonReader reader = new StackBsonReader(new ByteBufferBobBsonBuffer(bytes));
    var result = bobBson.deserialise(BeanWithIgnoreFields.class, reader);
    Assertions.assertEquals("bob", result.getName());
    Assertions.assertNull(result.getDescription());
    Assertions.assertEquals("mr", result.getTitle());
  }
}
