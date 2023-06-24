package org.bobstuff.bobbson;

import java.util.Arrays;
import java.util.HashSet;
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

public class BeanWithSetTest {
  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BobBsonProvider.class)
  public void testWriteWithNullSet(BobBsonProvider.BobBsonImplProvider configurationProvider)
      throws Exception {
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new ByteBufferBobBsonBuffer(new byte[100]));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);

    var bean = new BeanWithSet();

    BobBson bobBson = configurationProvider.provide();
    BsonWriter writer = new StackBsonWriter(buffer);
    bobBson.serialise(bean, BeanWithSet.class, writer);

    var bytes = buffer.toByteArray();

    BsonReader reader = new StackBsonReader(new ByteBufferBobBsonBuffer(bytes));
    var result = bobBson.deserialise(BeanWithSet.class, reader);
    Assertions.assertNull(result.getTheSet());
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BobBsonProvider.class)
  public void testWriteWithSet(BobBsonProvider.BobBsonImplProvider configurationProvider)
      throws Exception {
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new ByteBufferBobBsonBuffer(new byte[100]));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);

    var bean = new BeanWithSet();
    var names = new HashSet(Arrays.asList("bob", "john", "scott"));
    bean.setTheSet(names);

    BobBson bobBson = configurationProvider.provide();
    BsonWriter writer = new StackBsonWriter(buffer);
    bobBson.serialise(bean, BeanWithSet.class, writer);

    var bytes = buffer.toByteArray();

    BsonReader reader = new StackBsonReader(new ByteBufferBobBsonBuffer(bytes));
    var result = bobBson.deserialise(BeanWithSet.class, reader);
    Assertions.assertEquals(names, result.getTheSet());
  }
}
