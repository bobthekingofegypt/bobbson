package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonTimestamp;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonTimestampConverter implements BobBsonConverter<BsonTimestamp> {
  @Override
  public @Nullable BsonTimestamp read(BsonReader bsonReader) {
    return new BsonTimestamp(bsonReader.readInt64());
  }

  @Override
  public void write(
      @NonNull BsonWriter bsonWriter, byte @Nullable [] key, @NonNull BsonTimestamp value) {
    if (key == null) {
      bsonWriter.writeTimestamp(value.getValue());
    } else {
      bsonWriter.writeTimestamp(key, value.getValue());
    }
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, @NonNull BsonTimestamp value) {
    bsonWriter.writeTimestamp(value.getValue());
  }
}
