package org.bobstuff.bobbson;

import java.nio.charset.StandardCharsets;
import org.bobstuff.bobbson.utils.MDBBsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

public class SkipValues {
  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BsonDataProvider.class)
  public void skipArrayValues(BsonDataProvider.BufferDataBuilder builder) throws Exception {
    var bsonWriter = MDBBsonWriter.writer();
    bsonWriter.writeString("name", "fred");
    bsonWriter.writeStartArray("internal");
    bsonWriter.writeString("value1");
    bsonWriter.writeString("value2");
    bsonWriter.writeEndArray();
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
    Assertions.assertEquals(BsonType.ARRAY, type);
    reader.readStartArray();

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.STRING, type);
    var keyBytes = "0".getBytes(StandardCharsets.UTF_8);
    Assertions.assertTrue(range.equalsArray(keyBytes));

    reader.skipValue();

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.STRING, type);
    keyBytes = "1".getBytes(StandardCharsets.UTF_8);
    Assertions.assertTrue(range.equalsArray(keyBytes));

    String value = reader.readString();
    Assertions.assertEquals("value2", value);

    reader.readEndArray();

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.INT64, type);

    var ageBytes = "age".getBytes(StandardCharsets.UTF_8);
    Assertions.assertTrue(range.equalsArray(ageBytes));

    long age = reader.readInt64();
    Assertions.assertEquals(10, age);
  }
}
