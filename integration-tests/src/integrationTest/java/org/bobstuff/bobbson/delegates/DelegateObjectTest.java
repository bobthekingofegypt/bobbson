package org.bobstuff.bobbson.delegates;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import org.bobstuff.bobbson.*;
import org.bobstuff.bobbson.buffer.BobBufferBobBsonBuffer;
import org.bobstuff.bobbson.buffer.DynamicBobBsonBuffer;
import org.bobstuff.bobbson.buffer.pool.BobBsonBufferPool;
import org.bobstuff.bobbson.buffer.pool.NoopBobBsonBufferPool;
import org.bobstuff.bobbson.reflection.ObjectConverterFactory;
import org.bobstuff.bobbson.writer.StackBsonWriter;
import org.junit.jupiter.api.Test;

public class DelegateObjectTest {
  @Test
  public void readWriteToDelegateGetterSetter() throws Exception {
    var sut = new DelegateObject();
    sut.setName("bob");
    sut.setAge(1002);
    sut.setOld(true);

    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new BobBufferBobBsonBuffer(new byte[size], 0, 0));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);
    BobBson bobBson = new BobBson(new BobBsonConfig(false));
    bobBson.registerFactory(new ObjectConverterFactory());

    org.bobstuff.bobbson.writer.BsonWriter bsonWriter = new StackBsonWriter(buffer);
    bobBson.serialise(sut, DelegateObject.class, bsonWriter);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    buffer.pipe(bos);
    var data = bos.toByteArray();

    buffer.release();

    var reader = new BsonReaderStack(new BobBufferBobBsonBuffer(data, 0, data.length));
    var result = bobBson.deserialise(DelegateObject.class, reader);

    assertEquals(sut, result);
  }
}
