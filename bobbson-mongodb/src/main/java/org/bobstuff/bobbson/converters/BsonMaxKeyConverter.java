package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonMaxKey;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonMaxKeyConverter implements BobBsonConverter<BsonMaxKey> {
  @Override
  public @Nullable BsonMaxKey readValue(BsonReader bsonReader, BsonType type) {
    bsonReader.readNull();
    return new BsonMaxKey();
  }

  @Override
  public void writeValue(@NonNull BsonWriter bsonWriter, BsonMaxKey value) {
    bsonWriter.writeMaxKey();
  }
}
