package org.bobstuff.bobbson;

import org.bobstuff.bobbson.utils.MDBBsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

public class ArrayDocument1 {
  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BsonDataProvider.class)
  public void testArray(BsonDataProvider.BufferDataBuilder builder) throws Exception {
    var bsonWriter = MDBBsonWriter.writer();
    bsonWriter.writeStartArray("profiles");
    bsonWriter.writeString("Fred");
    bsonWriter.writeString("Fred 2");
    bsonWriter.writeString("Fred 3");
    bsonWriter.writeEndArray();

    var reader = MDBBsonWriter.reader(builder, bsonWriter);

    reader.readStartDocument();
    BsonType type = reader.readBsonType();
    Assertions.assertEquals(BsonType.ARRAY, type);
    Assertions.assertEquals("profiles", reader.currentFieldName());

    reader.readStartArray();

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.STRING, type);
    Assertions.assertEquals("0", reader.currentFieldName());
    String name = reader.readString();
    Assertions.assertEquals("Fred", name);

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.STRING, type);
    Assertions.assertEquals("1", reader.currentFieldName());
    name = reader.readString();
    Assertions.assertEquals("Fred 2", name);

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.STRING, type);
    Assertions.assertEquals("2", reader.currentFieldName());
    name = reader.readString();
    Assertions.assertEquals("Fred 3", name);

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.END_OF_DOCUMENT, type);
    reader.readEndArray();

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.END_OF_DOCUMENT, type);
    reader.readEndDocument();
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BsonDataProvider.class)
  public void testArrayReadWithLoop(BsonDataProvider.BufferDataBuilder builder) throws Exception {
    var bsonWriter = MDBBsonWriter.writer();
    bsonWriter.writeStartArray("profiles");
    for (var i = 0; i < 100; i++) {
      bsonWriter.writeString("Fred" + i);
    }
    bsonWriter.writeEndArray();

    var reader = MDBBsonWriter.reader(builder, bsonWriter);

    reader.readStartDocument();
    BsonType type = reader.readBsonType();
    Assertions.assertEquals(BsonType.ARRAY, type);
    Assertions.assertEquals("profiles", reader.currentFieldName());

    reader.readStartArray();
    var i = 0;
    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
      Assertions.assertEquals("Fred" + i++, reader.readString());
    }
    Assertions.assertEquals(100, i);
    reader.readEndArray();

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.END_OF_DOCUMENT, type);
    reader.readEndDocument();
  }
}
