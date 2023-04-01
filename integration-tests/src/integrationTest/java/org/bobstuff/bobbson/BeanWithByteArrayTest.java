package org.bobstuff.bobbson;

import org.bobstuff.bobbson.utils.MDBBsonWriter;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BeanWithByteArrayTest {
  @Test
  public void testReadWriteKeyAsByteArray()
      throws Exception {
    BufferDataPool pool =
            new NoopBufferDataPool((size) -> new ByteBufferBobBsonBuffer(new byte[100]));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);

    var id = new ObjectId().toByteArray();

    BsonWriter writer = new BsonWriter(buffer);
    writer.writeStartDocument();
    writer.writeObjectId("key", id);
    writer.writeEndDocument();

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    buffer.pipe(os);
    os.flush();
    os.close();
    byte[] bytes = os.toByteArray();

    BobBson bobBson = new BobBson();
    BsonReader reader = new BsonReader(new ByteBufferBobBsonBuffer(bytes));
    var result = bobBson.deserialise(BeanWithByteArray.class, reader);
    Assertions.assertArrayEquals(id, result.getKey());
  }
}
