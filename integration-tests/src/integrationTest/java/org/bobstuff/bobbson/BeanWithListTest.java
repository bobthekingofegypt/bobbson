package org.bobstuff.bobbson;

import java.util.Arrays;
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

public class BeanWithListTest {
  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BobBsonProvider.class)
  public void testWriteWithNullList(BobBsonProvider.BobBsonImplProvider configurationProvider)
      throws Exception {
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new ByteBufferBobBsonBuffer(new byte[100]));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);

    var bean = new BeanWithList();

    BobBson bobBson = configurationProvider.provide();
    BsonWriter writer = new StackBsonWriter(buffer);
    bobBson.serialise(bean, BeanWithList.class, writer);

    var bytes = buffer.toByteArray();

    BsonReader reader = new StackBsonReader(new ByteBufferBobBsonBuffer(bytes));
    var result = bobBson.deserialise(BeanWithList.class, reader);
    Assertions.assertNull(result.getNames());
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BobBsonProvider.class)
  public void testWriteWithCustomList(BobBsonProvider.BobBsonImplProvider configurationProvider)
      throws Exception {
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new ByteBufferBobBsonBuffer(new byte[100]));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);

    var bean = new BeanWithList();
    var names = Arrays.asList("bob", "john", "scott");
    bean.setNames(names);

    BobBson bobBson = configurationProvider.provide();
    BsonWriter writer = new StackBsonWriter(buffer);
    bobBson.serialise(bean, BeanWithList.class, writer);

    var bytes = buffer.toByteArray();

    BsonReader reader = new StackBsonReader(new ByteBufferBobBsonBuffer(bytes));
    var result = bobBson.deserialise(BeanWithList.class, reader);
    Assertions.assertEquals(names, result.getNames());
  }
}
