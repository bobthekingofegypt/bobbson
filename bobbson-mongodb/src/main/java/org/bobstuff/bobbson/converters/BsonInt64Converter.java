package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonInt64;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonInt64Converter implements BobBsonConverter<BsonInt64> {
  @Override
  public @Nullable BsonInt64 read(BsonReader bsonReader) {
    return new BsonInt64(bsonReader.readInt64());
  }

  @Override
  public void write(
      @NonNull BsonWriter bsonWriter, byte @Nullable [] key, @NonNull BsonInt64 value) {
    if (key == null) {
      bsonWriter.writeLong(value.getValue());
    } else {
      bsonWriter.writeLong(key, value.getValue());
    }
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, @NonNull BsonInt64 value) {
    bsonWriter.writeLong(value.getValue());
  }
}
