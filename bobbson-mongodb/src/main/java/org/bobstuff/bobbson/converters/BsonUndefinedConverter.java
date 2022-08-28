package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonUndefined;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonUndefinedConverter implements BobBsonConverter<BsonUndefined> {
  @Override
  public @Nullable BsonUndefined read(BsonReader bsonReader) {
    bsonReader.readUndefined();
    return new BsonUndefined();
  }

  @Override
  public void write(
      @NonNull BsonWriter bsonWriter, byte @Nullable [] key, @NonNull BsonUndefined value) {
    if (key == null) {
      bsonWriter.writeUndefined();
    } else {
      bsonWriter.writeUndefined(key);
    }
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, @NonNull BsonUndefined value) {
    bsonWriter.writeUndefined();
  }
}
