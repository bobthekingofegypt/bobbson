package org.bobstuff.bobbson;

import org.bobstuff.bobbson.buffer.ByteBufferBobBsonBuffer;
import org.bobstuff.bobbson.buffer.DynamicBobBsonBuffer;
import org.bobstuff.bobbson.buffer.pool.BobBsonBufferPool;
import org.bobstuff.bobbson.buffer.pool.NoopBobBsonBufferPool;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.reader.StackBsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bobstuff.bobbson.writer.StackBsonWriter;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

public class BeanWithNoFieldsTest {
  @Test
  public void testReadWriteKeyAsByteArray() throws Exception {
    // this test case should just pass, no need to validate anything
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new ByteBufferBobBsonBuffer(new byte[100]));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);

    var id = new ObjectId().toByteArray();

    BsonWriter writer = new StackBsonWriter(buffer);
    writer.writeStartDocument();
    writer.writeEndDocument();

    var bytes = buffer.toByteArray();

    BobBson bobBson = new BobBson();
    BsonReader reader = new StackBsonReader(new ByteBufferBobBsonBuffer(bytes));
    var result = bobBson.deserialise(BeanWithNoFields.class, reader);
  }
}
