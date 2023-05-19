package org.bobstuff.bobbson.jmhlargefail;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import org.bobstuff.bobbson.*;
import org.bobstuff.bobbson.buffer.BobBufferBobBsonBuffer;
import org.bobstuff.bobbson.reflection.CollectionConverterFactory;
import org.bobstuff.bobbson.reflection.ObjectConverterFactory;
import org.junit.jupiter.api.Test;

public class LargeObjectEncodingTest {
  @Test
  public void testEncodingSucceeds() throws Exception {
    BufferDataPool pool =
        new NoopBufferDataPool((size) -> new BobBufferBobBsonBuffer(new byte[size], 0, 0));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);
    BobBson bobBson = new BobBson();

    var obj = Generator.newLargeObject();

    org.bobstuff.bobbson.writer.BsonWriter bsonWriter =
        new org.bobstuff.bobbson.writer.BsonWriter(buffer);
    bobBson.serialise(obj, LargeObject.class, bsonWriter);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    buffer.pipe(bos);
    var data = bos.toByteArray();

    buffer.release();

    var reader = new BsonReaderStack(new BobBufferBobBsonBuffer(data, 0, data.length));
    var result = bobBson.deserialise(LargeObject.class, reader);

    assertEquals(obj, result);
  }

  @Test
  public void testRelfectionEncodingSucceeds() throws Exception {
    BufferDataPool pool =
        new NoopBufferDataPool((size) -> new BobBufferBobBsonBuffer(new byte[size], 0, 0));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);
    BobBson bobBson = new BobBson(new BobBsonConfig(false));
    bobBson.registerFactory(new CollectionConverterFactory());
    bobBson.registerFactory(new ObjectConverterFactory());

    var obj = Generator.newLargeObject();

    org.bobstuff.bobbson.writer.BsonWriter bsonWriter =
        new org.bobstuff.bobbson.writer.BsonWriter(buffer);
    bobBson.serialise(obj, LargeObject.class, bsonWriter);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    buffer.pipe(bos);
    var data = bos.toByteArray();

    buffer.release();

    BobBson bobBson2 = new BobBson();
    //        var reader = new BsonReader(new BobBufferBobBsonBuffer(data, 0, data.length));
    //        var result = bobBson2.deserialise(LargeObject.class, reader);
    var reader = new BsonReaderStack(new BobBufferBobBsonBuffer(data, 0, data.length));
    var result = bobBson.deserialise(LargeObject.class, reader);

    assertEquals(obj, result);
  }
}
