package org.bobstuff.bobbson.converters;

import static java.lang.String.format;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class IntegerBsonConverter implements BobBsonConverter<Integer> {
  @Override
  public @Nullable Integer readValue(BsonReader bsonReader, BsonType type) {
    // TODO handle casting down to type that cant hold value safer
    if (type == BsonType.INT32) {
      return bsonReader.readInt32();
    } else if (type == BsonType.INT64) {
      return (int) bsonReader.readInt64();
    } else if (type == BsonType.DOUBLE) {
      return (int) bsonReader.readDouble();
    }

    throw new RuntimeException(format("Attempting to read %s bson type as a integer", type));
  }

  @Override
  public void writeValue(BsonWriter bsonWriter, Integer value) {
    bsonWriter.writeInteger(value);
  }
}
