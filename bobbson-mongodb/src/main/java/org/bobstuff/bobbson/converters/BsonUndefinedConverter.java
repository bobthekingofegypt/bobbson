package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonUndefined;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonUndefinedConverter implements BobBsonConverter<BsonUndefined> {
  @Override
  public @Nullable BsonUndefined readValue(BsonReader bsonReader, BsonType type) {
    bsonReader.readUndefined();
    return new BsonUndefined();
  }

  @Override
  public void writeValue(@NonNull BsonWriter bsonWriter, BsonUndefined value) {
    bsonWriter.writeUndefined();
  }
}
