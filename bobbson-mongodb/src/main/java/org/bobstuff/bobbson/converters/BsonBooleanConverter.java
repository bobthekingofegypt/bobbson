package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonBoolean;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonBooleanConverter implements BobBsonConverter<BsonBoolean> {
  @Override
  public @Nullable BsonBoolean readValue(BsonReader bsonReader, BsonType type) {
    return new BsonBoolean(bsonReader.readBoolean());
  }

  @Override
  public void writeValue(@NonNull BsonWriter bsonWriter, BsonBoolean value) {
    bsonWriter.writeBoolean(value.getValue());
  }
}
