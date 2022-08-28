package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonInt32;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonInt32Converter implements BobBsonConverter<BsonInt32> {
  @Override
  public @Nullable BsonInt32 read(BsonReader bsonReader) {
    return new BsonInt32(bsonReader.readInt32());
  }

  @Override
  public void write(
      @NonNull BsonWriter bsonWriter, byte @Nullable [] key, @NonNull BsonInt32 value) {
    if (key == null) {
      bsonWriter.writeInteger(value.getValue());
    } else {
      bsonWriter.writeInteger(key, value.getValue());
    }
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, @NonNull BsonInt32 value) {
    bsonWriter.writeInteger(value.getValue());
  }
}
