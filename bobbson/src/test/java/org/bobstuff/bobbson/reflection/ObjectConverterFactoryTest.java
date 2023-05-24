package org.bobstuff.bobbson.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Type;
import org.bobstuff.bobbson.*;
import org.bobstuff.bobbson.annotations.BsonWriterOptions;
import org.bobstuff.bobbson.buffer.BobBufferBobBsonBuffer;
import org.bobstuff.bobbson.converters.IntegerBsonConverter;
import org.bobstuff.bobbson.converters.StringBsonConverter;
import org.bobstuff.bobbson.writer.StackBsonWriter;
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

  public static class DontWriteNulls {
    @BsonWriterOptions(writeNull = false)
    private String name;

    private String other;
    private String otherNull;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getOther() {
      return other;
    }

    public void setOther(String other) {
      this.other = other;
    }

    public String getOtherNull() {
      return otherNull;
    }

    public void setOtherNull(String otherNull) {
      this.otherNull = otherNull;
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
    var bsonWriter = new StackBsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeString("name", "bob");
    bsonWriter.writeInteger("age", 23);
    bsonWriter.writeEndDocument();

    var reader = new BsonReaderStack(buffer);

    var result = (BasicTypes) converter.read(reader);
    assertEquals("bob", result.getName());
    assertEquals(23, result.getAge());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testTryWrite() {
    var bobBson = Mockito.mock(BobBson.class);
    Mockito.when(bobBson.tryFindConverter((Type) String.class))
        .thenReturn((BobBsonConverter) new StringBsonConverter());
    var sut = new ObjectConverterFactory();
    var converter = sut.tryCreate(DontWriteNulls.class, bobBson);

    var testModel = new DontWriteNulls();
    testModel.setOther("othervalue");
    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new StackBsonWriter(buffer);
    converter.write(bsonWriter, testModel);

    var reader = new BsonReaderStack(buffer);
    reader.readStartDocument();
    assertEquals(BsonType.STRING, reader.readBsonType());
    assertEquals("other", reader.getFieldName().value());
    assertEquals("othervalue", reader.readString());
    assertEquals(BsonType.NULL, reader.readBsonType());
    assertEquals("otherNull", reader.getFieldName().value());
    reader.readNull();
    reader.readEndDocument();
  }
}
