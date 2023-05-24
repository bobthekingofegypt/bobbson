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
}
