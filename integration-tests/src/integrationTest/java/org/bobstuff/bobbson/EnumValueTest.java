package org.bobstuff.bobbson;

import java.io.ByteArrayOutputStream;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EnumValueTest {
  @Test
  public void testReadWriteBeanWithEnum() throws Exception {
    BufferDataPool pool =
        new NoopBufferDataPool((size) -> new ByteBufferBobBsonBuffer(new byte[1000]));
    DynamicBobBsonBuffer buffer = new DynamicBobBsonBuffer(pool);
    BobBson bobBson = new BobBson();

    var beanWithEnum = new EnumValue();
    beanWithEnum.setValue(EnumValue.AnEnum.VALUE_ONE);

    BsonWriter writer = new BsonWriter(buffer);
    bobBson.serialise(beanWithEnum, EnumValue.class, writer);

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    buffer.pipe(os);
    os.flush();
    os.close();
    byte[] bytes = os.toByteArray();

    BsonReader reader = new BsonReader(new ByteBufferBobBsonBuffer(bytes));
    var result = bobBson.deserialise(EnumValue.class, reader);
    Assertions.assertEquals(beanWithEnum, result);
  }
}
