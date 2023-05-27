package org.bobstuff.bobbson.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bobstuff.bobbson.BsonReaderStack;
import org.bobstuff.bobbson.buffer.BobBufferBobBsonBuffer;
import org.bobstuff.bobbson.converters.IntegerBsonConverter;
import org.bobstuff.bobbson.writer.StackBsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CollectionConverterTest {

  @Test
  public void testReadList() {
    var sut =
        new CollectionConverter<Integer, List<Integer>>(
            List.class, ArrayList::new, new IntegerBsonConverter());

    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new StackBsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeStartArray("bob");
    bsonWriter.writeInteger(3);
    bsonWriter.writeInteger(4);
    bsonWriter.writeEndArray();
    bsonWriter.writeEndDocument();

    var reader = new BsonReaderStack(buffer);
    reader.readStartDocument();
    reader.readBsonType();

    var result = sut.read(reader);
    assertEquals(2, result.size());
    assertEquals(3, result.get(0));
    assertEquals(4, result.get(1));
  }

  @Test
  public void testReadSet() {
    var sut =
        new CollectionConverter<Integer, Set<Integer>>(
            Set.class, HashSet::new, new IntegerBsonConverter());

    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new StackBsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeStartArray("bob");
    bsonWriter.writeInteger(3);
    bsonWriter.writeInteger(4);
    bsonWriter.writeEndArray();
    bsonWriter.writeEndDocument();

    var reader = new BsonReaderStack(buffer);
    reader.readStartDocument();
    reader.readBsonType();

    var result = sut.read(reader);
    assertEquals(2, result.size());
    assertTrue(result.contains(3));
    assertTrue(result.contains(4));
  }

  @Test
  public void testWriteList() {
    var values = new ArrayList<Integer>();
    values.add(12);
    values.add(2);
    values.add(2453);
    values.add(843);

    var sut =
        new CollectionConverter<Integer, List<Integer>>(
            List.class, ArrayList::new, new IntegerBsonConverter());

    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new StackBsonWriter(buffer);
    bsonWriter.writeStartDocument();
    sut.write(bsonWriter, "ages", values);
    bsonWriter.writeEndDocument();

    var reader = new BsonReaderStack(buffer);
    reader.readStartDocument();
    reader.readBsonType();

    Assertions.assertEquals("ages", reader.getFieldName().value());

    var result = sut.read(reader);

    Assertions.assertEquals(values, result);
  }

  @Test
  public void testWriteListWithNulls() {
    var values = new ArrayList<Integer>();
    values.add(12);
    values.add(null);
    values.add(2453);
    values.add(null);

    var sut =
        new CollectionConverter<Integer, List<Integer>>(
            List.class, ArrayList::new, new IntegerBsonConverter());

    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new StackBsonWriter(buffer);
    bsonWriter.writeStartDocument();
    sut.write(bsonWriter, "ages", values);
    bsonWriter.writeEndDocument();

    var reader = new BsonReaderStack(buffer);
    reader.readStartDocument();
    reader.readBsonType();

    Assertions.assertEquals("ages", reader.getFieldName().value());

    var result = sut.read(reader);

    Assertions.assertEquals(values, result);
  }

  @Test
  public void testWriteSet() {
    var values = new ArrayList<Integer>();
    values.add(12);
    values.add(2);
    values.add(2453);
    values.add(843);
    var valuesSet = new HashSet<>(values);

    var sut =
        new CollectionConverter<Integer, Set<Integer>>(
            Set.class, HashSet::new, new IntegerBsonConverter());

    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new StackBsonWriter(buffer);
    bsonWriter.writeStartDocument();
    sut.write(bsonWriter, "ages", valuesSet);
    bsonWriter.writeEndDocument();

    var reader = new BsonReaderStack(buffer);
    reader.readStartDocument();
    reader.readBsonType();

    Assertions.assertEquals("ages", reader.getFieldName().value());

    var result = sut.read(reader);

    Assertions.assertEquals(valuesSet, result);
  }

  @Test
  public void testInstanceFactoryException() {
    var values = new ArrayList<Integer>();
    values.add(12);
    values.add(2);
    values.add(2453);
    values.add(843);
    var valuesSet = new HashSet<>(values);

    var sut =
        new CollectionConverter<Integer, Set<Integer>>(
            Set.class,
            () -> {
              throw new RuntimeException("Fail");
            },
            new IntegerBsonConverter());

    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new StackBsonWriter(buffer);
    bsonWriter.writeStartDocument();
    sut.write(bsonWriter, "ages", valuesSet);
    bsonWriter.writeEndDocument();

    var reader = new BsonReaderStack(buffer);
    reader.readStartDocument();
    reader.readBsonType();

    Assertions.assertEquals("ages", reader.getFieldName().value());

    Assertions.assertThrows(RuntimeException.class, () -> sut.read(reader));
  }
}
