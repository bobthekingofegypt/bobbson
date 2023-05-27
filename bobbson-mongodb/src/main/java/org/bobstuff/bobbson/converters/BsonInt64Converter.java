package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonInt64;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonInt64Converter implements BobBsonConverter<BsonInt64> {
  @Override
  public @Nullable BsonInt64 readValue(BsonReader bsonReader, BsonType type) {
    return new BsonInt64(bsonReader.readInt64());
  }

  @Override
  public void writeValue(@NonNull BsonWriter bsonWriter, BsonInt64 value) {
    bsonWriter.writeLong(value.getValue());
  }
}
