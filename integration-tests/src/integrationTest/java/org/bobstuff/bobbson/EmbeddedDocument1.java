package org.bobstuff.bobbson;

import org.bobstuff.bobbson.utils.MDBBsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

public class EmbeddedDocument1 {
  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BsonDataProvider.class)
  public void readEmbeddedDocSimple(BsonDataProvider.BufferDataBuilder builder) throws Exception {
    var bsonWriter = MDBBsonWriter.writer();
    bsonWriter.writeStartDocument("profile");
    bsonWriter.writeString("name", "Fred");
    bsonWriter.writeInt32("age", 10);
    bsonWriter.writeEndDocument();

    var reader = MDBBsonWriter.reader(builder, bsonWriter);

    reader.readStartDocument();
    BsonType type = reader.readBsonType();
    Assertions.assertEquals(BsonType.DOCUMENT, type);
    Assertions.assertEquals("profile", reader.currentFieldName());

    reader.readStartDocument();
    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.STRING, type);
    Assertions.assertEquals("name", reader.currentFieldName());
    String name = reader.readString();
    Assertions.assertEquals("Fred", name);

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.INT32, type);
    Assertions.assertEquals("age", reader.currentFieldName());
    int age = reader.readInt32();
    Assertions.assertEquals(10, age);

    reader.readEndDocument();
    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.END_OF_DOCUMENT, type);
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BsonDataProvider.class)
  public void readEmbeddedDocFollowedByEmbeddedDoc(BsonDataProvider.BufferDataBuilder builder)
      throws Exception {
    var bsonWriter = MDBBsonWriter.writer();
    bsonWriter.writeStartDocument("profile");
    bsonWriter.writeString("name", "Fred");
    bsonWriter.writeInt32("age", 10);
    bsonWriter.writeEndDocument();
    bsonWriter.writeStartDocument("address");
    bsonWriter.writeString("street", "10 something street");
    bsonWriter.writeEndDocument();

    var reader = MDBBsonWriter.reader(builder, bsonWriter);

    reader.readStartDocument();
    BsonType type = reader.readBsonType();
    Assertions.assertEquals(BsonType.DOCUMENT, type);
    Assertions.assertEquals("profile", reader.currentFieldName());

    reader.readStartDocument();
    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.STRING, type);
    Assertions.assertEquals("name", reader.currentFieldName());
    String name = reader.readString();
    Assertions.assertEquals("Fred", name);

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.INT32, type);
    Assertions.assertEquals("age", reader.currentFieldName());
    int age = reader.readInt32();
    Assertions.assertEquals(10, age);

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.END_OF_DOCUMENT, type);
    reader.readEndDocument();

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.DOCUMENT, type);
    reader.readStartDocument();
    Assertions.assertEquals("address", reader.currentFieldName());

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.STRING, type);
    Assertions.assertEquals("street", reader.currentFieldName());
    String street = reader.readString();
    Assertions.assertEquals("10 something street", street);

    reader.readEndDocument();
    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.END_OF_DOCUMENT, type);
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BsonDataProvider.class)
  public void readEmbeddedDocFollowedByField(BsonDataProvider.BufferDataBuilder builder)
      throws Exception {
    var bsonWriter = MDBBsonWriter.writer();
    bsonWriter.writeStartDocument("profile");
    bsonWriter.writeString("name", "Fred");
    bsonWriter.writeInt32("age", 10);
    bsonWriter.writeEndDocument();
    bsonWriter.writeString("street", "10 something street");

    var reader = MDBBsonWriter.reader(builder, bsonWriter);

    reader.readStartDocument();
    BsonType type = reader.readBsonType();
    Assertions.assertEquals(BsonType.DOCUMENT, type);
    Assertions.assertEquals("profile", reader.currentFieldName());

    reader.readStartDocument();
    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.STRING, type);
    Assertions.assertEquals("name", reader.currentFieldName());
    String name = reader.readString();
    Assertions.assertEquals("Fred", name);

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.INT32, type);
    Assertions.assertEquals("age", reader.currentFieldName());
    int age = reader.readInt32();
    Assertions.assertEquals(10, age);

    reader.readEndDocument();

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.STRING, type);
    Assertions.assertEquals("street", reader.currentFieldName());
    String street = reader.readString();
    Assertions.assertEquals("10 something street", street);

    reader.readEndDocument();
    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.END_OF_DOCUMENT, type);
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BsonDataProvider.class)
  public void readEmbeddedDocDontKnowFieldOrder(BsonDataProvider.BufferDataBuilder builder)
      throws Exception {
    var bsonWriter = MDBBsonWriter.writer();
    bsonWriter.writeStartDocument("profile");
    bsonWriter.writeString("name", "Fred");
    bsonWriter.writeInt32("age", 10);
    bsonWriter.writeEndDocument();
    bsonWriter.writeString("street", "10 something street");

    var reader = MDBBsonWriter.reader(builder, bsonWriter);

    reader.readStartDocument();
    BsonType type = reader.readBsonType();
    Assertions.assertEquals(BsonType.DOCUMENT, type);
    Assertions.assertEquals("profile", reader.currentFieldName());

    reader.readStartDocument();
    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
      if (reader.currentFieldName().equals("name")) {
        String name = reader.readString();
        Assertions.assertEquals("Fred", name);
      } else if (reader.currentFieldName().equals("age")) {
        int age = reader.readInt32();
        Assertions.assertEquals(10, age);
      }
    }

    reader.readEndDocument();

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.STRING, type);
    Assertions.assertEquals("street", reader.currentFieldName());
    String street = reader.readString();
    Assertions.assertEquals("10 something street", street);

    reader.readEndDocument();
    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.END_OF_DOCUMENT, type);
  }
}
