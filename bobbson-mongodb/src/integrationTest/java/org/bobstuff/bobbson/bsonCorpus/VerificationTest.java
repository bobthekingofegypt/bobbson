package org.bobstuff.bobbson.bsonCorpus;

import com.google.common.io.BaseEncoding;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.bobstuff.bobbson.*;
import org.bobstuff.bobbson.converters.BsonValueConverters;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonDocument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VerificationTest {
  @Test
  public void verification() throws Exception {
    var bson = "10000000016400120000000000F87F00";
    BsonReader reader =
        new BsonReaderStack(
            ByteBuffer.wrap(BaseEncoding.base16().decode(bson.toUpperCase()))
                .order(ByteOrder.LITTLE_ENDIAN));
    BobBson bobBson = new BobBson();
    BsonValueConverters.register(bobBson);

    BsonDocument document = bobBson.deserialise(BsonDocument.class, reader);
    System.out.println(document);

    BufferDataPool pool =
        new NoopBufferDataPool((size) -> new ByteBufferBobBsonBuffer(new byte[size]));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);
    BsonWriter writer = new BsonWriter(buffer);

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
