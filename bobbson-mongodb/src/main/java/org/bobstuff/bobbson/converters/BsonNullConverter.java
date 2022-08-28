package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonNullConverter implements BobBsonConverter<BsonNull> {
  @Override
  public @Nullable BsonNull read(BsonReader bsonReader) {
    bsonReader.readNull();
    return new BsonNull();
  }

  @Override
  public void write(
      @NonNull BsonWriter bsonWriter, byte @Nullable [] key, @NonNull BsonNull value) {
    if (key == null) {
      bsonWriter.writeNull();
    } else {
      bsonWriter.writeNull(key);
    }
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, @NonNull BsonNull value) {
    bsonWriter.writeNull();
  }
}
