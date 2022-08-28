package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonMaxKey;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonMaxKeyConverter implements BobBsonConverter<BsonMaxKey> {
  @Override
  public @Nullable BsonMaxKey read(BsonReader bsonReader) {
    bsonReader.readNull();
    return new BsonMaxKey();
  }

  @Override
  public void write(
      @NonNull BsonWriter bsonWriter, byte @Nullable [] key, @NonNull BsonMaxKey value) {
    if (key == null) {
      bsonWriter.writeMaxKey();
    } else {
      bsonWriter.writeMaxKey(key);
    }
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, @NonNull BsonMaxKey value) {
    bsonWriter.writeMaxKey();
  }
}
