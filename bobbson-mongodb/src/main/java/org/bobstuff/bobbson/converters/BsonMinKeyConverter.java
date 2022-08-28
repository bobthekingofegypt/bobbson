package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonMinKey;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonMinKeyConverter implements BobBsonConverter<BsonMinKey> {
  @Override
  public @Nullable BsonMinKey read(BsonReader bsonReader) {
    bsonReader.readNull();
    return new BsonMinKey();
  }

  @Override
  public void write(
      @NonNull BsonWriter bsonWriter, byte @Nullable [] key, @NonNull BsonMinKey value) {
    if (key == null) {
      bsonWriter.writeMinKey();
    } else {
      bsonWriter.writeMinKey(key);
    }
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, @NonNull BsonMinKey value) {
    bsonWriter.writeMinKey();
  }
}
