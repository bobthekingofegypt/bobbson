package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonDateTime;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonDateTimeConverter implements BobBsonConverter<BsonDateTime> {
  @Override
  public @Nullable BsonDateTime read(BsonReader bsonReader) {
    return new BsonDateTime(bsonReader.readInt64());
  }

  @Override
  public void write(
      @NonNull BsonWriter bsonWriter, byte @Nullable [] key, @NonNull BsonDateTime value) {
    if (key == null) {
      bsonWriter.writeDateTime(value.getValue());
    } else {
      bsonWriter.writeDateTime(key, value.getValue());
    }
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, @NonNull BsonDateTime value) {
    bsonWriter.writeDateTime(value.getValue());
  }
}
