package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonTimestamp;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonTimestampConverter implements BobBsonConverter<BsonTimestamp> {
  @Override
  public @Nullable BsonTimestamp readValue(BsonReader bsonReader, BsonType type) {
    return new BsonTimestamp(bsonReader.readInt64());
  }

  @Override
  public void writeValue(@NonNull BsonWriter bsonWriter, BsonTimestamp value) {
    bsonWriter.writeTimestamp(value.getValue());
  }
}
