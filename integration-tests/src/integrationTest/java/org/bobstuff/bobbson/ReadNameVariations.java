package org.bobstuff.bobbson;

import java.nio.charset.StandardCharsets;
import org.bobstuff.bobbson.utils.MDBBsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

public class ReadNameVariations {
  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BsonDataProvider.class)
  public void compareUsingByteRangeHelper(BsonDataProvider.BufferDataBuilder builder)
      throws Exception {
    var bsonWriter = MDBBsonWriter.writer();
    bsonWriter.writeInt64("age", 10);
    bsonWriter.writeString("name", "fred");

    var reader = MDBBsonWriter.reader(builder, bsonWriter);

    reader.readStartDocument();

    BsonType type = reader.readBsonType();
    Assertions.assertEquals(BsonType.INT64, type);
    BobBsonBuffer.ByteRangeComparitor range = reader.getFieldName();

    var ageBytes = "age".getBytes(StandardCharsets.UTF_8);
    Assertions.assertTrue(range.equalsArray(ageBytes));

    long age = reader.readInt64();
    Assertions.assertEquals(10, age);

    type = reader.readBsonType();
    Assertions.assertEquals(BsonType.STRING, type);
    var nameBytes = "name".getBytes(StandardCharsets.UTF_8);
    Assertions.assertTrue(range.equalsArray(nameBytes));

    String name = reader.readString();
    Assertions.assertEquals("fred", name);
  }
}
