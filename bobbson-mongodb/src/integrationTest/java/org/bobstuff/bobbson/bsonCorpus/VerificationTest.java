package org.bobstuff.bobbson.bsonCorpus;

import com.google.common.io.BaseEncoding;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.bobstuff.bobbson.*;
import org.bobstuff.bobbson.buffer.ByteBufferBobBsonBuffer;
import org.bobstuff.bobbson.buffer.DynamicBobBsonBuffer;
import org.bobstuff.bobbson.buffer.pool.BobBsonBufferPool;
import org.bobstuff.bobbson.buffer.pool.NoopBobBsonBufferPool;
import org.bobstuff.bobbson.converters.BsonValueConverters;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.reader.StackBsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bobstuff.bobbson.writer.StackBsonWriter;
import org.bson.BsonDocument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VerificationTest {
  @Test
  public void verification() throws Exception {
    var bson = "10000000016400120000000000F87F00";
    BsonReader reader =
        new StackBsonReader(
            ByteBuffer.wrap(BaseEncoding.base16().decode(bson.toUpperCase()))
                .order(ByteOrder.LITTLE_ENDIAN));
    BobBson bobBson = new BobBson();
    BsonValueConverters.register(bobBson);

    BsonDocument document = bobBson.deserialise(BsonDocument.class, reader);
    System.out.println(document);

    BobBsonBufferPool pool =
        new NoopBobBsonBufferPool((size) -> new ByteBufferBobBsonBuffer(new byte[size]));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);
    BsonWriter writer = new StackBsonWriter(buffer);

    bobBson.serialise(document, BsonDocument.class, writer);

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    buffer.pipe(os);
    os.flush();
    os.close();
    byte[] bytes = os.toByteArray();

    Assertions.assertEquals(bson.toUpperCase(), BaseEncoding.base16().encode(bytes));

    System.out.println(document);
  }
}
