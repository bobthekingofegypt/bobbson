package org.bobstuff.bobbson;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.bobstuff.bobbson.annotations.BsonAttribute;
import org.bobstuff.bobbson.annotations.BsonConverter;
import org.bobstuff.bobbson.buffer.BobBufferBobBsonBuffer;
import org.bobstuff.bobbson.reflection.ObjectConverterFactory;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.junit.jupiter.api.Test;

public class ReflectionDecodingTest {
  @Test
  public void testAlias() throws Exception {
    var buffer = new byte[1000];
    var bsonBuffer = new ByteBufferBobBsonBuffer(buffer);
    var writer = new BsonWriter(bsonBuffer);
    writer.writeStartDocument();
    writer.writeString("notnames", "a value");
    writer.writeEndDocument();

    var bobBson = new BobBson();
    bobBson.registerFactory(new ObjectConverterFactory());

    var reader = new BsonReader(new BobBufferBobBsonBuffer(buffer, 0, buffer.length));
    var result = bobBson.deserialise(AliasTest.class, reader);
    assertEquals("a value", result.getNames());
  }

  @Test
  public void testCustomConverterDecode() throws Exception {
    var buffer = new byte[1000];
    var bsonBuffer = new ByteBufferBobBsonBuffer(buffer);
    var writer = new BsonWriter(bsonBuffer);
    writer.writeStartDocument();
    writer.writeString("names", "a value");
    writer.writeEndDocument();

    var bobBson = new BobBson();
    bobBson.registerFactory(new ObjectConverterFactory());

    var reader = new BsonReader(new BobBufferBobBsonBuffer(buffer, 0, buffer.length));
    var result = bobBson.deserialise(CustomConverterTest.class, reader);
    assertEquals("custom: a value", result.getNames());
  }

  public static class AliasTest {
    @BsonAttribute("notnames")
    private String names;

    public String getNames() {
      return names;
    }

    public void setNames(String names) {
      this.names = names;
    }
  }

  public static class CustomConverterTest {
    @BsonConverter(target = CustomConverter.class)
    private String names;

    public String getNames() {
      return names;
    }

    public void setNames(String names) {
      this.names = names;
    }
  }
}
