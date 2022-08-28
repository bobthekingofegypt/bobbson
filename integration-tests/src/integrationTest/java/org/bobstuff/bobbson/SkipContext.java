package org.bobstuff.bobbson;

import java.nio.charset.StandardCharsets;
import org.bobstuff.bobbson.utils.MDBBsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

public class SkipContext {
  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BsonDataProvider.class)
  public void skipToEndOfEmbeddedDocument(BsonDataProvider.BufferDataBuilder builder)
      throws Exception {
    var bsonWriter = MDBBsonWriter.writer();
    bsonWriter.writeString("name", "fred");
    bsonWriter.writeStartDocument("internal");
    bsonWriter.writeString("field1", "value1");
    bsonWriter.writeString("field2", "value2");
    bsonWriter.writeString("field3", "value3");
    bsonWriter.writeString("field4", "value4");
    bsonWriter.writeString("field5", "value5");
    bsonWriter.writeEndDocument();
    bsonWriter.writeInt64("age", 10);

    var reader = MDBBsonWriter.reader(builder, bsonWriter);

    reader.readStartDocument();

    var range = reader.getFieldName();

    BsonType type = reader.readBsonType();
    Assertions.assertEquals(BsonType.STRING, type);
    var nameBytes = "name".getBytes(StandardCharsets.UTF_8);
    Assertions.assertTrue(range.equalsArray(nameBytes));

    String name = reader.readString();
    Assertions.assertEquals("fred", name);

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.DOCUMENT, type);
    reader.readStartDocument();

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.STRING, type);
    nameBytes = "field1".getBytes(StandardCharsets.UTF_8);
    Assertions.assertTrue(range.equalsArray(nameBytes));

    String field1 = reader.readString();
    Assertions.assertEquals("value1", field1);

    reader.skipContext();

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.END_OF_DOCUMENT, type);
    reader.readEndDocument();

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.INT64, type);

    var ageBytes = "age".getBytes(StandardCharsets.UTF_8);
    Assertions.assertTrue(range.equalsArray(ageBytes));

    long age = reader.readInt64();
    Assertions.assertEquals(10, age);
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BsonDataProvider.class)
  public void skipToEndOfDocument(BsonDataProvider.BufferDataBuilder builder) throws Exception {
    var bsonWriter = MDBBsonWriter.writer();
    bsonWriter.writeString("name", "fred");
    bsonWriter.writeString("field1", "value1");
    bsonWriter.writeString("field2", "value2");
    bsonWriter.writeString("field3", "value3");
    bsonWriter.writeString("field4", "value4");
    bsonWriter.writeString("field5", "value5");

    var reader = MDBBsonWriter.reader(builder, bsonWriter);

    reader.readStartDocument();

    var range = reader.getFieldName();

    BsonType type = reader.readBsonType();
    Assertions.assertEquals(BsonType.STRING, type);
    var nameBytes = "name".getBytes(StandardCharsets.UTF_8);
    Assertions.assertTrue(range.equalsArray(nameBytes));

    reader.skipContext();
    reader.readEndDocument();

    Assertions.assertEquals(BsonContextType.TOP_LEVEL, reader.getCurrentContextType());
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BsonDataProvider.class)
  public void skipToEndOfArray(BsonDataProvider.BufferDataBuilder builder) throws Exception {
    var bsonWriter = MDBBsonWriter.writer();
    bsonWriter.writeString("name", "fred");
    bsonWriter.writeStartArray("strings");
    bsonWriter.writeString("test1");
    bsonWriter.writeString("test2");
    bsonWriter.writeString("test3");
    bsonWriter.writeString("test4");
    bsonWriter.writeString("test5");
    bsonWriter.writeEndArray();
    bsonWriter.writeString("end", "true");

    var reader = MDBBsonWriter.reader(builder, bsonWriter);

    reader.readStartDocument();

    var range = reader.getFieldName();

    BsonType type = reader.readBsonType();
    Assertions.assertEquals(BsonType.STRING, type);
    var nameBytes = "name".getBytes(StandardCharsets.UTF_8);
    Assertions.assertTrue(range.equalsArray(nameBytes));
    Assertions.assertEquals("fred", reader.readString());

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.ARRAY, type);
    var stringsBytes = "strings".getBytes(StandardCharsets.UTF_8);
    Assertions.assertTrue(range.equalsArray(stringsBytes));
    reader.readStartArray();

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.STRING, type);
    Assertions.assertEquals("test1", reader.readString());

    reader.skipContext();
    reader.readEndDocument();

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.STRING, type);
    var endBytes = "end".getBytes(StandardCharsets.UTF_8);
    Assertions.assertTrue(range.equalsArray(endBytes));
    Assertions.assertEquals("true", reader.readString());
    reader.readEndDocument();

    Assertions.assertEquals(BsonContextType.TOP_LEVEL, reader.getCurrentContextType());
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BsonDataProvider.class)
  public void skipToEndOfDocumentInArrayComplex(BsonDataProvider.BufferDataBuilder builder)
      throws Exception {
    var bsonWriter = MDBBsonWriter.writer();
    bsonWriter.writeString("name", "fred");
    bsonWriter.writeStartArray("strings");
    bsonWriter.writeStartDocument();
    bsonWriter.writeString("internal", "values");
    bsonWriter.writeString("internal2", "values");
    bsonWriter.writeStartArray("strings");
    bsonWriter.writeString("values");
    bsonWriter.writeEndArray();
    bsonWriter.writeStartDocument("banana");
    bsonWriter.writeString("internal-internal", "values");
    bsonWriter.writeString("internal2-internal", "values");
    bsonWriter.writeEndDocument();
    bsonWriter.writeEndDocument();
    bsonWriter.writeStartDocument();
    bsonWriter.writeString("internalb", "values");
    bsonWriter.writeString("internal2", "values");
    bsonWriter.writeEndDocument();
    bsonWriter.writeStartDocument();
    bsonWriter.writeString("internal", "values");
    bsonWriter.writeString("internal2", "values");
    bsonWriter.writeEndDocument();
    bsonWriter.writeEndArray();
    bsonWriter.writeString("end", "true");

    var reader = MDBBsonWriter.reader(builder, bsonWriter);

    reader.readStartDocument();

    var range = reader.getFieldName();

    BsonType type = reader.readBsonType();
    Assertions.assertEquals(BsonType.STRING, type);
    var nameBytes = "name".getBytes(StandardCharsets.UTF_8);
    Assertions.assertTrue(range.equalsArray(nameBytes));
    Assertions.assertEquals("fred", reader.readString());

    type = reader.readBsonType();
    reader.readStartArray();
    type = reader.readBsonType();
    reader.readStartDocument();
    reader.skipContext();
    reader.readEndDocument();
    //    reader.readEndDocument();

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.DOCUMENT, type);
    reader.readStartDocument();
    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.STRING, type);
    byte[] stringsBytes = "internalb".getBytes(StandardCharsets.UTF_8);
    Assertions.assertTrue(range.equalsArray(stringsBytes));

    reader.skipContext();
    reader.readEndDocument();

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.DOCUMENT, type);
    reader.readStartDocument();

    reader.skipContext();
    reader.readEndDocument();

    reader.readEndArray();

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.STRING, type);
    var endBytes = "end".getBytes(StandardCharsets.UTF_8);
    Assertions.assertTrue(range.equalsArray(endBytes));
    Assertions.assertEquals("true", reader.readString());
    reader.readEndDocument();

    Assertions.assertEquals(BsonContextType.TOP_LEVEL, reader.getCurrentContextType());
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BsonDataProvider.class)
  public void skipToEndOfDocumentInArray(BsonDataProvider.BufferDataBuilder builder)
      throws Exception {
    var bsonWriter = MDBBsonWriter.writer();
    bsonWriter.writeString("name", "fred");
    bsonWriter.writeStartArray("strings");
    bsonWriter.writeStartDocument();
    bsonWriter.writeString("internal", "values");
    bsonWriter.writeString("internal2", "values");
    bsonWriter.writeEndDocument();
    bsonWriter.writeStartDocument();
    bsonWriter.writeString("internal", "values");
    bsonWriter.writeString("internal2", "values");
    bsonWriter.writeEndDocument();
    bsonWriter.writeStartDocument();
    bsonWriter.writeString("internal", "values");
    bsonWriter.writeString("internal2", "values");
    bsonWriter.writeEndDocument();
    bsonWriter.writeEndArray();
    bsonWriter.writeString("end", "true");

    var reader = MDBBsonWriter.reader(builder, bsonWriter);

    reader.readStartDocument();

    var range = reader.getFieldName();

    BsonType type = reader.readBsonType();
    Assertions.assertEquals(BsonType.STRING, type);
    var nameBytes = "name".getBytes(StandardCharsets.UTF_8);
    Assertions.assertTrue(range.equalsArray(nameBytes));
    Assertions.assertEquals("fred", reader.readString());

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.ARRAY, type);
    var stringsBytes = "strings".getBytes(StandardCharsets.UTF_8);
    Assertions.assertTrue(range.equalsArray(stringsBytes));
    reader.readStartArray();

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.DOCUMENT, type);
    reader.readStartDocument();
    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.STRING, type);
    stringsBytes = "internal".getBytes(StandardCharsets.UTF_8);
    Assertions.assertTrue(range.equalsArray(stringsBytes));

    reader.skipContext();
    reader.readEndDocument();

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.DOCUMENT, type);
    reader.readStartDocument();
    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.STRING, type);
    stringsBytes = "internal".getBytes(StandardCharsets.UTF_8);
    Assertions.assertTrue(range.equalsArray(stringsBytes));

    reader.skipContext();
    reader.readEndDocument();

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.DOCUMENT, type);
    reader.readStartDocument();

    reader.skipContext();
    reader.readEndDocument();

    reader.readEndArray();

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.STRING, type);
    var endBytes = "end".getBytes(StandardCharsets.UTF_8);
    Assertions.assertTrue(range.equalsArray(endBytes));
    Assertions.assertEquals("true", reader.readString());
    reader.readEndDocument();

    Assertions.assertEquals(BsonContextType.TOP_LEVEL, reader.getCurrentContextType());
  }
}
