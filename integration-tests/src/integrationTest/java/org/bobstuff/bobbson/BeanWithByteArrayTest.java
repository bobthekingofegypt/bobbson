package org.bobstuff.bobbson;

import java.io.ByteArrayOutputStream;
import org.bobstuff.bobbson.buffer.ByteBufferBobBsonBuffer;
import org.bobstuff.bobbson.buffer.DynamicBobBsonBuffer;
import org.bobstuff.bobbson.buffer.pool.BobBsonBufferPool;
import org.bobstuff.bobbson.buffer.pool.NoopBobBsonBufferPool;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bobstuff.bobbson.writer.StackBsonWriter;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BeanWithByteArrayTest {
  @Test
  public void testReadWriteKeyAsByteArray() throws Exception {
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new ByteBufferBobBsonBuffer(new byte[100]));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);

    var id = new ObjectId().toByteArray();

    BsonWriter writer = new StackBsonWriter(buffer);
    writer.writeStartDocument();
    writer.writeObjectId("key", id);
    writer.writeEndDocument();

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    buffer.pipe(os);
    os.flush();
    os.close();
    byte[] bytes = os.toByteArray();

    BobBson bobBson = new BobBson();
    BsonReader reader = new BsonReaderStack(new ByteBufferBobBsonBuffer(bytes));
    var result = bobBson.deserialise(BeanWithByteArray.class, reader);
    Assertions.assertArrayEquals(id, result.getKey());
  }
}
