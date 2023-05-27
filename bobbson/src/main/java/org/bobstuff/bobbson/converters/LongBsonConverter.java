package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.lang.String.format;

public class LongBsonConverter implements BobBsonConverter<Long> {
  @Override
  public @Nullable Long readValue(BsonReader bsonReader, BsonType type) {
    if (type == BsonType.INT64) {
      return bsonReader.readInt64();
    } else if (type == BsonType.INT32) {
      return (long) bsonReader.readInt32();
    } else if (type == BsonType.DOUBLE) {
      return (long) bsonReader.readDouble();
    } else {
      throw new RuntimeException(format("Attempting to read %s bson type as a long", type));
    }
  }

  @Override
  public void writeValue(BsonWriter bsonWriter, Long value) {
    bsonWriter.writeLong(value);
  }
}
