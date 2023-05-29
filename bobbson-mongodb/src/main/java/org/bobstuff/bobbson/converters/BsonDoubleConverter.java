package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonDouble;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonDoubleConverter implements BobBsonConverter<BsonDouble> {
  @Override
  public @Nullable BsonDouble readValue(BsonReader bsonReader, BsonType type) {
    return new BsonDouble(bsonReader.readDouble());
  }

  @Override
  public void writeValue(@NonNull BsonWriter bsonWriter, BsonDouble value) {
    bsonWriter.writeDouble(value.getValue());
  }
}
