package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonDateTime;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonDateTimeConverter implements BobBsonConverter<BsonDateTime> {
  @Override
  public @Nullable BsonDateTime readValue(BsonReader bsonReader, BsonType type) {
    return new BsonDateTime(bsonReader.readInt64());
  }

  @Override
  public void writeValue(@NonNull BsonWriter bsonWriter, BsonDateTime value) {
    bsonWriter.writeDateTime(value.getValue());
  }
}
