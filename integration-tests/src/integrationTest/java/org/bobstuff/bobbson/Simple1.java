package org.bobstuff.bobbson;

import java.nio.charset.StandardCharsets;
import org.bobstuff.bobbson.utils.MDBBsonWriter;
import org.bson.BsonBinary;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

public class Simple1 {
  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BsonDataProvider.class)
  public void readString(BsonDataProvider.BufferDataBuilder builder) throws Exception {
    var bsonWriter = MDBBsonWriter.writer();
    bsonWriter.writeString("monks", "are a thing");

    var buffer = MDBBsonWriter.data(bsonWriter);
    BsonReader reader = new BsonReader(builder.build(buffer));

    reader.readStartDocument();
    BsonType type = reader.readBsonType();
    Assertions.assertEquals("monks", reader.currentFieldName());

    String monks = reader.readString();
    Assertions.assertEquals(BsonType.STRING, type);
    Assertions.assertEquals("are a thing", monks);
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BsonDataProvider.class)
  public void readBinary(BsonDataProvider.BufferDataBuilder builder) throws Exception {
    var bsonWriter = MDBBsonWriter.writer();
    bsonWriter.writeBinaryData(
        "monks", new BsonBinary("are a thing".getBytes(StandardCharsets.UTF_8)));

    var buffer = MDBBsonWriter.data(bsonWriter);
    BsonReader reader = new BsonReader(builder.build(buffer));

    reader.readStartDocument();
    BsonType type = reader.readBsonType();
    Assertions.assertEquals(BsonType.BINARY, type);
    Assertions.assertEquals("monks", reader.currentFieldName());

    String monks = new String(reader.readBinary().getData());
    Assertions.assertEquals("are a thing", monks);
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BsonDataProvider.class)
  public void readMultipleStrings(BsonDataProvider.BufferDataBuilder builder) throws Exception {
    var bsonWriter = MDBBsonWriter.writer();
    bsonWriter.writeString("monks", "are a thing");
    bsonWriter.writeString("cats", "aren't dogs");

    var buffer = MDBBsonWriter.data(bsonWriter);
    BsonReader reader = new BsonReader(builder.build(buffer));

    reader.readStartDocument();
    BsonType type = reader.readBsonType();
    Assertions.assertEquals(BsonType.STRING, type);
    Assertions.assertEquals("monks", reader.currentFieldName());

    String monks = reader.readString();
    Assertions.assertEquals("are a thing", monks);

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.STRING, type);
    Assertions.assertEquals("cats", reader.currentFieldName());

    String cats = reader.readString();
    Assertions.assertEquals("aren't dogs", cats);
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BsonDataProvider.class)
  public void readInt32(BsonDataProvider.BufferDataBuilder builder) throws Exception {
    var bsonWriter = MDBBsonWriter.writer();
    bsonWriter.writeInt32("age", 10);

    var reader = MDBBsonWriter.reader(builder, bsonWriter);

    reader.readStartDocument();
    BsonType type = reader.readBsonType();
    Assertions.assertEquals(BsonType.INT32, type);
    Assertions.assertEquals("age", reader.currentFieldName());

    int age = reader.readInt32();
    Assertions.assertEquals(10, age);
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BsonDataProvider.class)
  public void readInt64(BsonDataProvider.BufferDataBuilder builder) throws Exception {
    var bsonWriter = MDBBsonWriter.writer();
    bsonWriter.writeInt64("age", 10);

    var reader = MDBBsonWriter.reader(builder, bsonWriter);

    reader.readStartDocument();
    BsonType type = reader.readBsonType();
    Assertions.assertEquals(BsonType.INT64, type);
    Assertions.assertEquals("age", reader.currentFieldName());

    long age = reader.readInt64();
    Assertions.assertEquals(10, age);
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BsonDataProvider.class)
  public void readInt64thenInt32(BsonDataProvider.BufferDataBuilder builder) throws Exception {
    var bsonWriter = MDBBsonWriter.writer();
    bsonWriter.writeInt64("age", 10);
    bsonWriter.writeInt32("iq", 100);

    var reader = MDBBsonWriter.reader(builder, bsonWriter);

    reader.readStartDocument();
    BsonType type = reader.readBsonType();
    Assertions.assertEquals(BsonType.INT64, type);
    Assertions.assertEquals("age", reader.currentFieldName());

    long age = reader.readInt64();
    Assertions.assertEquals(10, age);

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.INT32, type);
    Assertions.assertEquals("iq", reader.currentFieldName());

    long iq = reader.readInt32();
    Assertions.assertEquals(100, iq);
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BsonDataProvider.class)
  public void readInt64thenString(BsonDataProvider.BufferDataBuilder builder) throws Exception {
    var bsonWriter = MDBBsonWriter.writer();
    bsonWriter.writeInt64("age", 10);
    bsonWriter.writeString("name", "fred");

    var reader = MDBBsonWriter.reader(builder, bsonWriter);

    reader.readStartDocument();
    BsonType type = reader.readBsonType();
    Assertions.assertEquals(BsonType.INT64, type);
    Assertions.assertEquals("age", reader.currentFieldName());

    long age = reader.readInt64();
    Assertions.assertEquals(10, age);

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.STRING, type);
    Assertions.assertEquals("name", reader.currentFieldName());

    String name = reader.readString();
    Assertions.assertEquals("fred", name);
  }
}
