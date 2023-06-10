package org.bobstuff.bobbson.converters;

import static java.lang.String.format;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class StringBsonConverter implements BobBsonConverter<String> {
  @Override
  public @Nullable String readValue(BsonReader bsonReader, BsonType type) {
    if (type == BsonType.STRING) {
      return bsonReader.readString();
    }

    throw new RuntimeException(format("Attempting to read %s bson type as a string", type));
  }

  @Override
  public void writeValue(BsonWriter bsonWriter, String value) {
    bsonWriter.writeString(value);
  }

  public static @Nullable String readString(@NonNull BsonReader bsonReader) {
    var type = bsonReader.getCurrentBsonType();
    if (type == BsonType.STRING) {
      return bsonReader.readString();
    } else if (type == BsonType.NULL) {
      bsonReader.readNull();
      return null;
    }

    throw new RuntimeException(format("Attempting to read %s bson type as a string", type));
  }
}
