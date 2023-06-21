package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonNullConverter implements BobBsonConverter<BsonNull> {
  @Override
  public BsonNull read(BsonReader bsonReader) {
    bsonReader.readNull();
    return new BsonNull();
  }

  @Override
  public @Nullable BsonNull readValue(BsonReader bsonReader, BsonType type) {
    throw new UnsupportedOperationException("should never be called directly");
  }

  @Override
  public void writeValue(@NonNull BsonWriter bsonWriter, BsonNull value) {
    bsonWriter.writeNull();
  }
}
