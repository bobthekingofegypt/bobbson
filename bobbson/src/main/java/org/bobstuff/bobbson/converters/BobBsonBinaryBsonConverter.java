package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonBinary;
import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.BsonType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BobBsonBinaryBsonConverter implements BobBsonConverter<BobBsonBinary> {
  @Override
  public @Nullable BobBsonBinary read(@NonNull BsonReader bsonReader) {
    BsonType type = bsonReader.getCurrentBsonType();
    if (type == BsonType.NULL) {
      bsonReader.readNull();
      return null;
    }

    if (type != BsonType.BINARY) {
      throw new RuntimeException("Oh noes should be double");
    }

    return bsonReader.readBinary();
  }
}
