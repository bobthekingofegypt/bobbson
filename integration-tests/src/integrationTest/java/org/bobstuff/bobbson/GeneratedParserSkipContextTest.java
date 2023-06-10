package org.bobstuff.bobbson;

import static org.mockito.Mockito.verify;

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
import org.mockito.Mockito;

public class GeneratedParserSkipContextTest {
  @Test
  public void testSkipsContext() throws Exception {
    BobBson bobBson = new BobBson();

    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new ByteBufferBobBsonBuffer(new byte[100]));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);

    BsonWriter writer = new StackBsonWriter(buffer);
    writer.writeStartDocument();
    writer.writeString("something", "mr");
    writer.writeString("name", "bob");
    writer.writeString("description", "a description");
    writer.writeString("nottitle", "mr");
    writer.writeString("banana", "mr");
    writer.writeString("crazy", "mr");
    writer.writeString("hello", "mr");
    writer.writeString("wowza", "mr");
    writer.writeEndDocument();

    var bytes = buffer.toByteArray();

    BsonReader reader = new StackBsonReader(new ByteBufferBobBsonBuffer(bytes));
    BsonReader spyReader = Mockito.spy(reader);
    var result = bobBson.deserialise(BeanWithIgnoreFields.class, spyReader);
    Assertions.assertEquals("bob", result.getName());
    Assertions.assertNull(result.getDescription());
    Assertions.assertEquals("mr", result.getTitle());

    verify(spyReader, Mockito.times(1)).skipContext();
    // should have skipped something key and description which is ignored
    verify(spyReader, Mockito.times(2)).skipValue();
  }
}
