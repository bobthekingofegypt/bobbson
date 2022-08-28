package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class IntegerBsonConverter implements BobBsonConverter<Integer> {
  @Override
  public @Nullable Integer read(@NonNull BsonReader bsonReader) {
    // TODO handle casting down to type that cant hold value
    BsonType type = bsonReader.getCurrentBsonType();
    if (type == BsonType.NULL) {
      bsonReader.readNull();
      return null;
    } else if (type == BsonType.INT64) {
      return (int) bsonReader.readInt64();
    } else if (type == BsonType.DOUBLE) {
      return (int) bsonReader.readDouble();
    }

    if (type != BsonType.INT32) {
      throw new RuntimeException("Oh noes should be int32");
    }
    return bsonReader.readInt32();
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, byte @Nullable [] key, Integer value) {
    if (key == null) {
      bsonWriter.writeInteger(value);
    } else {
      bsonWriter.writeInteger(key, value);
    }
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, @NonNull Integer value) {
    bsonWriter.writeInteger(value);
  }
}
