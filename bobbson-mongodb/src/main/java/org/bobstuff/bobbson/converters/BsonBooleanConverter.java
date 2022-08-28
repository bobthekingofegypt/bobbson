package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonBoolean;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonBooleanConverter implements BobBsonConverter<BsonBoolean> {
  @Override
  public @Nullable BsonBoolean read(BsonReader bsonReader) {
    return new BsonBoolean(bsonReader.readBoolean());
  }

  @Override
  public void write(
      @NonNull BsonWriter bsonWriter, byte @Nullable [] key, @NonNull BsonBoolean value) {
    if (key == null) {
      bsonWriter.writeBoolean(value.getValue());
    } else {
      bsonWriter.writeBoolean(key, value.getValue());
    }
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, @NonNull BsonBoolean value) {
    bsonWriter.writeBoolean(value.getValue());
  }
}
