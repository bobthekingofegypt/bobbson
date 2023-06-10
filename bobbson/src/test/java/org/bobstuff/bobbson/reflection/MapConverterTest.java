package org.bobstuff.bobbson.reflection;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import org.bobstuff.bobbson.buffer.BobBufferBobBsonBuffer;
import org.bobstuff.bobbson.converters.IntegerBsonConverter;
import org.bobstuff.bobbson.reader.StackBsonReader;
import org.bobstuff.bobbson.writer.StackBsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MapConverterTest {

  @Test
  public void testReadValue() {
    var sut = new MapConverter<>(HashMap.class, HashMap::new, new IntegerBsonConverter());

    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new StackBsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeStartDocument("ages");
    bsonWriter.writeInteger("fred", 12);
    bsonWriter.writeInteger("john", 234);
    bsonWriter.writeEndDocument();
    bsonWriter.writeEndDocument();

    var reader = new StackBsonReader(buffer);
    reader.readStartDocument();
    reader.readBsonType();

    Assertions.assertEquals("ages", reader.currentFieldName());

    var result = sut.read(reader);
    assertNotNull(result);
    assertEquals(12, result.get("fred"));
    assertEquals(234, result.get("john"));
  }

  @Test
  public void testReadValueWithNull() {
    var sut = new MapConverter<>(HashMap.class, HashMap::new, new IntegerBsonConverter());

    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new StackBsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeStartDocument("ages");
    bsonWriter.writeInteger("fred", 12);
    bsonWriter.writeNull("john");
    bsonWriter.writeEndDocument();
    bsonWriter.writeEndDocument();

    var reader = new StackBsonReader(buffer);
    reader.readStartDocument();
    reader.readBsonType();

    Assertions.assertEquals("ages", reader.currentFieldName());

    var result = sut.read(reader);
    assertNotNull(result);
    assertEquals(12, result.get("fred"));
    assertNull(result.get("john"));
  }

  @Test
  public void testWriteValue() {
    var sut = new MapConverter<>(HashMap.class, HashMap::new, new IntegerBsonConverter());

    var ages = new HashMap<String, Integer>();
    ages.put("fred", 12);
    ages.put("john", 234);

    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new StackBsonWriter(buffer);
    bsonWriter.writeStartDocument();
    sut.write(bsonWriter, "ages", ages);
    bsonWriter.writeEndDocument();

    var reader = new StackBsonReader(buffer);
    reader.readStartDocument();
    reader.readBsonType();

    Assertions.assertEquals("ages", reader.currentFieldName());

    var result = sut.read(reader);
    assertNotNull(result);
    assertEquals(12, result.get("fred"));
    assertEquals(234, result.get("john"));
  }

  @Test
  public void testWriteValueWithNullEntry() {
    var sut = new MapConverter<>(HashMap.class, HashMap::new, new IntegerBsonConverter());

    var ages = new HashMap<String, Integer>();
    ages.put("fred", 12);
    ages.put("john", null);

    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new StackBsonWriter(buffer);
    bsonWriter.writeStartDocument();
    sut.write(bsonWriter, "ages", ages);
    bsonWriter.writeEndDocument();

    var reader = new StackBsonReader(buffer);
    reader.readStartDocument();
    reader.readBsonType();

    Assertions.assertEquals("ages", reader.currentFieldName());

    var result = sut.read(reader);
    assertNotNull(result);
    assertEquals(12, result.get("fred"));
    assertNull(result.get("john"));
  }
}
