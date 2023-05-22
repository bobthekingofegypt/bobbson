package org.bobstuff.bobbson;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.bobstuff.bobbson.converters.BsonValueConverters;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bobstuff.bobbson.writer.StackBsonWriter;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BsonDocumentConverterWriteTest {
  @Test
  public void testWriteStringFromDoc() throws Exception {
    BobBson bobBson = new BobBson();
    BsonValueConverters.register(bobBson);

    BufferDataPool pool =
        new NoopBufferDataPool((size) -> new ByteBufferBobBsonBuffer(new byte[10]));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);

    BsonWriter writer = new StackBsonWriter(buffer);

    BsonDocument document = new BsonDocument();
    document.append("name", new BsonString("bob"));

    bobBson.serialise(document, BsonDocument.class, writer);

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    buffer.pipe(os);
    os.flush();
    os.close();
    byte[] bytes = os.toByteArray();

    BsonReader reader = new BsonReaderStack(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN));

    reader.readStartDocument();
    reader.readBsonType();
    Assertions.assertEquals("name", reader.currentFieldName());
    Assertions.assertEquals("bob", reader.readString());
  }
}
