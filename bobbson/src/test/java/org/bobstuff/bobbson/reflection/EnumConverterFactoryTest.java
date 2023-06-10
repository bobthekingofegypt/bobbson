package org.bobstuff.bobbson.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.bobstuff.bobbson.BobBson;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.buffer.BobBufferBobBsonBuffer;
import org.bobstuff.bobbson.reader.StackBsonReader;
import org.bobstuff.bobbson.writer.StackBsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class EnumConverterFactoryTest {
  public enum AnEnum {
    VALUE1,
    VALUE2;
  }

  @Test
  public void testTryCreateList() {
    var bobBson = Mockito.mock(BobBson.class);
    var sut = new EnumConverterFactory<BsonType>();
    var converter = sut.tryCreate(BsonType.class, bobBson);

    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new StackBsonWriter(buffer);
    bsonWriter.writeStartDocument();
    converter.write(bsonWriter, "enumvalue", BsonType.ARRAY);
    bsonWriter.writeEndDocument();

    var reader = new StackBsonReader(buffer);
    reader.readStartDocument();
    reader.readBsonType();

    Assertions.assertEquals("enumvalue", reader.currentFieldName());

    var result = converter.read(reader);

    assertEquals(BsonType.ARRAY, result);
  }

  @Test
  public void testReturnNullNonEnum() {
    var bobBson = Mockito.mock(BobBson.class);
    var sut = new EnumConverterFactory<BsonType>();
    var converter = sut.tryCreate(String.class, bobBson);

    Assertions.assertNull(converter);
  }
}
