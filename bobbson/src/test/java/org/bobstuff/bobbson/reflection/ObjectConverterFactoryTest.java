package org.bobstuff.bobbson.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Type;
import org.bobstuff.bobbson.BobBson;
import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.buffer.BobBufferBobBsonBuffer;
import org.bobstuff.bobbson.converters.IntegerBsonConverter;
import org.bobstuff.bobbson.converters.StringBsonConverter;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ObjectConverterFactoryTest {
  public static class BasicTypes {
    private String name;
    private int age;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getAge() {
      return age;
    }

    public void setAge(int age) {
      this.age = age;
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testTryCreate() {
    var bobBson = Mockito.mock(BobBson.class);
    Mockito.when(bobBson.tryFindConverter((Type) String.class))
        .thenReturn((BobBsonConverter) new StringBsonConverter());
    Mockito.when(bobBson.tryFindConverter((Type) int.class))
        .thenReturn((BobBsonConverter) new IntegerBsonConverter());
    var sut = new ObjectConverterFactory();
    var converter = sut.tryCreate(BasicTypes.class, bobBson);

    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new BsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeString("name", "bob");
    bsonWriter.writeInteger("age", 23);
    bsonWriter.writeEndDocument();

    var reader = new BsonReader(buffer);

    var result = (BasicTypes) converter.read(reader);
    assertEquals("bob", result.getName());
    assertEquals(23, result.getAge());
  }
}
