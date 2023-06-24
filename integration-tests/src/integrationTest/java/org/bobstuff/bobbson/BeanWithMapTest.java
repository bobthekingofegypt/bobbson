package org.bobstuff.bobbson;

import java.util.HashMap;
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

public class BeanWithMapTest {
  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BobBsonProvider.class)
  public void testWriteWithNullMap(BobBsonProvider.BobBsonImplProvider configurationProvider)
      throws Exception {
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new ByteBufferBobBsonBuffer(new byte[100]));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);

    var bean = new BeanWithMap();

    BobBson bobBson = configurationProvider.provide();
    BsonWriter writer = new StackBsonWriter(buffer);
    bobBson.serialise(bean, BeanWithMap.class, writer);

    var bytes = buffer.toByteArray();

    BsonReader reader = new StackBsonReader(new ByteBufferBobBsonBuffer(bytes));
    var result = bobBson.deserialise(BeanWithMap.class, reader);
    Assertions.assertNull(result.getTheMap());
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BobBsonProvider.class)
  public void testWriteWithCustomList(BobBsonProvider.BobBsonImplProvider configurationProvider)
      throws Exception {
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new ByteBufferBobBsonBuffer(new byte[100]));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);

    var bean = new BeanWithMap();
    var map = new HashMap<String, String>();
    map.put("one", "v1");
    map.put("two", "v2");
    map.put("three", "v3");
    bean.setTheMap(map);

    BobBson bobBson = configurationProvider.provide();
    BsonWriter writer = new StackBsonWriter(buffer);
    bobBson.serialise(bean, BeanWithMap.class, writer);

    var bytes = buffer.toByteArray();

    BsonReader reader = new StackBsonReader(new ByteBufferBobBsonBuffer(bytes));
    var result = bobBson.deserialise(BeanWithMap.class, reader);
    Assertions.assertEquals(map, result.getTheMap());
  }
}
