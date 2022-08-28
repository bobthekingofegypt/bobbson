package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class LongBsonConverter implements BobBsonConverter<Long> {
  @Override
  public @Nullable Long read(@NonNull BsonReader bsonReader) {
    BsonType type = bsonReader.getCurrentBsonType();
    if (type == BsonType.NULL) {
      bsonReader.readNull();
      return null;
    } else if (type == BsonType.INT64) {
      return bsonReader.readInt64();
    } else if (type == BsonType.INT32) {
      return (long) bsonReader.readInt32();
    } else if (type == BsonType.DOUBLE) {
      return (long) bsonReader.readDouble();
    } else {
      throw new RuntimeException("Oh noes should be a long");
    }
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, byte @Nullable [] key, Long value) {
    if (key == null) {
      bsonWriter.writeLong(value);
    } else {
      bsonWriter.writeLong(key, value);
    }
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, @NonNull Long value) {
    bsonWriter.writeLong(value);
  }
}
