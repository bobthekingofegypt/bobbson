package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonDouble;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonDoubleConverter implements BobBsonConverter<BsonDouble> {
  @Override
  public @Nullable BsonDouble read(BsonReader bsonReader) {
    return new BsonDouble(bsonReader.readDouble());
  }

  @Override
  public void write(
      @NonNull BsonWriter bsonWriter, byte @Nullable [] key, @NonNull BsonDouble value) {
    if (key == null) {
      bsonWriter.writeDouble(value.getValue());
    } else {
      bsonWriter.writeDouble(key, value.getValue());
    }
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, @NonNull BsonDouble value) {
    bsonWriter.writeDouble(value.getValue());
  }
}
