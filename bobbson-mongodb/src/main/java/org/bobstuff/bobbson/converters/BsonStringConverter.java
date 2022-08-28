package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonString;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonStringConverter implements BobBsonConverter<BsonString> {
  @Override
  public @Nullable BsonString read(BsonReader bsonReader) {
    return new BsonString(bsonReader.readString());
  }

  @Override
  public void write(
      @NonNull BsonWriter bsonWriter, byte @Nullable [] key, @NonNull BsonString value) {
    if (key == null) {
      bsonWriter.writeString(value.getValue());
    } else {
      bsonWriter.writeString(key, value.getValue());
    }
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, @NonNull BsonString value) {
    bsonWriter.writeString(value.getValue());
  }
}
