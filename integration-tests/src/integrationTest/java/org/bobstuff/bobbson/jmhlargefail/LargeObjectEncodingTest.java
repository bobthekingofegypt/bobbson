package org.bobstuff.bobbson.jmhlargefail;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import org.bobstuff.bobbson.*;
import org.bobstuff.bobbson.buffer.BobBufferBobBsonBuffer;
import org.bobstuff.bobbson.buffer.DynamicBobBsonBuffer;
import org.bobstuff.bobbson.buffer.pool.BobBsonBufferPool;
import org.bobstuff.bobbson.buffer.pool.NoopBobBsonBufferPool;
import org.bobstuff.bobbson.reader.StackBsonReader;
import org.bobstuff.bobbson.reflection.CollectionConverterFactory;
import org.bobstuff.bobbson.reflection.ObjectConverterFactory;
import org.bobstuff.bobbson.writer.StackBsonWriter;
import org.junit.jupiter.api.Test;

public class LargeObjectEncodingTest {
  @Test
  public void testEncodingSucceeds() throws Exception {
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new BobBufferBobBsonBuffer(new byte[size], 0, 0));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);
    BobBson bobBson = new BobBson();

    var obj = Generator.newLargeObject();

    org.bobstuff.bobbson.writer.BsonWriter bsonWriter = new StackBsonWriter(buffer);
    bobBson.serialise(obj, LargeObject.class, bsonWriter);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    buffer.pipe(bos);
    var data = bos.toByteArray();

    buffer.release();

    var reader = new StackBsonReader(new BobBufferBobBsonBuffer(data, 0, data.length));
    var result = bobBson.deserialise(LargeObject.class, reader);

    assertEquals(obj, result);
  }

  @Test
  public void testRelfectionEncodingSucceeds() throws Exception {
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new BobBufferBobBsonBuffer(new byte[size], 0, 0));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);
    BobBson bobBson = new BobBson(new BobBsonConfig(false));
    bobBson.registerFactory(new CollectionConverterFactory());
    bobBson.registerFactory(new ObjectConverterFactory());

    var obj = Generator.newLargeObject();

    org.bobstuff.bobbson.writer.BsonWriter bsonWriter = new StackBsonWriter(buffer);
    bobBson.serialise(obj, LargeObject.class, bsonWriter);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    buffer.pipe(bos);
    var data = bos.toByteArray();

    buffer.release();

    BobBson bobBson2 = new BobBson();
    //        var reader = new BsonReader(new BobBufferBobBsonBuffer(data, 0, data.length));
    //        var result = bobBson2.deserialise(LargeObject.class, reader);
    var reader = new StackBsonReader(new BobBufferBobBsonBuffer(data, 0, data.length));
    var result = bobBson.deserialise(LargeObject.class, reader);

    assertEquals(obj, result);
  }
}
