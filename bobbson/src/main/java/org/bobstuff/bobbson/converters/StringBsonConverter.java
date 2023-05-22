package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class StringBsonConverter implements BobBsonConverter<String> {
  @Override
  public @Nullable String read(@NonNull BsonReader bsonReader) {
    if (bsonReader.getCurrentBsonType() == BsonType.STRING) {
      return bsonReader.readString();
    } else if (bsonReader.getCurrentBsonType() == BsonType.NULL) {
      bsonReader.readNull();
      return null;
    }
    throw new RuntimeException(
        "trying to read a string from something that isn't a string or a null");
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, byte @Nullable [] key, @NonNull String value) {
    if (value == null && key == null) {
      bsonWriter.writeNull();
    } else if (value == null && key != null) {
      bsonWriter.writeNull(key);
    } else if (key == null) {
      bsonWriter.writeString(value);
    } else {
      bsonWriter.writeString(key, value);
    }
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, @NonNull String value) {
    bsonWriter.writeString(value);
  }

  public static @Nullable String readString(@NonNull BsonReader bsonReader) {
    if (bsonReader.getCurrentBsonType() == BsonType.STRING) {
      return bsonReader.readString();
    } else if (bsonReader.getCurrentBsonType() == BsonType.NULL) {
      bsonReader.readNull();
      return null;
    }
    throw new RuntimeException(
            "trying to read a string from something that isn't a string or a null");
  }
}
