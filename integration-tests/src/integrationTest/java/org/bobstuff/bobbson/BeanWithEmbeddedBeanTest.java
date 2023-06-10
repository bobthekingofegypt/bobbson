package org.bobstuff.bobbson;

import java.io.ByteArrayOutputStream;
import org.bobstuff.bobbson.buffer.ByteBufferBobBsonBuffer;
import org.bobstuff.bobbson.buffer.DynamicBobBsonBuffer;
import org.bobstuff.bobbson.buffer.pool.BobBsonBufferPool;
import org.bobstuff.bobbson.buffer.pool.NoopBobBsonBufferPool;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.reader.StackBsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bobstuff.bobbson.writer.StackBsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BeanWithEmbeddedBeanTest {
  @Test
  public void testSkipContextOnEmbedded() throws Exception {
    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new ByteBufferBobBsonBuffer(new byte[100]));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);

    BsonWriter writer = new StackBsonWriter(buffer);
    writer.writeStartDocument();
    writer.writeStartDocument("embedded");
    writer.writeString("bob", "rocks");
    writer.writeString("banana", "pajama");
    writer.writeString("something", "rocks");
    writer.writeString("something else", "rocks");
    writer.writeString("something more", "rocks");
    writer.writeEndDocument();
    writer.writeString("name", "jimmy");
    writer.writeEndDocument();

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    buffer.pipe(os);
    os.flush();
    os.close();
    byte[] bytes = os.toByteArray();

    BobBson bobBson = new BobBson();
    BsonReader reader = new StackBsonReader(new ByteBufferBobBsonBuffer(bytes));
    var result = bobBson.deserialise(BeanWithEmbeddedBean.class, reader);
    Assertions.assertEquals("jimmy", result.getName());
    var embedded = result.getEmbedded();
    Assertions.assertEquals("pajama", embedded.getBanana());
    Assertions.assertEquals("rocks", embedded.getBob());
  }
}
