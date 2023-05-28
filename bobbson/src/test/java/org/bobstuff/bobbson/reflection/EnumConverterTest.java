package org.bobstuff.bobbson.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.bobstuff.bobbson.BsonReaderStack;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.buffer.BobBufferBobBsonBuffer;
import org.bobstuff.bobbson.writer.StackBsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EnumConverterTest {
  @Test
  public void testReadEnum() {
    var sut = new EnumConverter<>(BsonType.class, BsonType.values());

    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new StackBsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeString("enumvalue", BsonType.ARRAY.name());
    bsonWriter.writeEndDocument();

    var reader = new BsonReaderStack(buffer);
    reader.readStartDocument();
    reader.readBsonType();

    Assertions.assertEquals("enumvalue", reader.currentFieldName());

    var result = sut.read(reader);

    assertEquals(BsonType.ARRAY, result);
  }

  @Test
  public void testReadEnumThrowsOnUnknown() {
    var sut = new EnumConverter<>(BsonType.class, BsonType.values());

    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new StackBsonWriter(buffer);
    bsonWriter.writeStartDocument();
    bsonWriter.writeString("enumvalue", "not a value");
    bsonWriter.writeEndDocument();

    var reader = new BsonReaderStack(buffer);
    reader.readStartDocument();
    reader.readBsonType();

    Assertions.assertEquals("enumvalue", reader.currentFieldName());

    Assertions.assertThrows(IllegalArgumentException.class, () -> sut.read(reader));
  }

  @Test
  public void testWriteEnum() {
    var sut = new EnumConverter<>(BsonType.class, BsonType.values());

    var buffer = new BobBufferBobBsonBuffer(new byte[1000], 0, 0);
    var bsonWriter = new StackBsonWriter(buffer);
    bsonWriter.writeStartDocument();
    sut.write(bsonWriter, "enumvalue", BsonType.ARRAY);
    bsonWriter.writeEndDocument();

    var reader = new BsonReaderStack(buffer);
    reader.readStartDocument();
    reader.readBsonType();

    Assertions.assertEquals("enumvalue", reader.currentFieldName());

    var result = sut.read(reader);

    assertEquals(BsonType.ARRAY, result);
  }
}
