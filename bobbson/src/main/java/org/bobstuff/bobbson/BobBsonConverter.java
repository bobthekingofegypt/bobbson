package org.bobstuff.bobbson;

import java.nio.charset.StandardCharsets;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface BobBsonConverter<T> {

  default @Nullable T read(BsonReader bsonReader) {
    BsonType type = bsonReader.getCurrentBsonType();

    if (type != null) {
      return readValue(bsonReader, type);
    }

    bsonReader.readNull();
    return null;
  }

  @Nullable T readValue(BsonReader bsonReader, BsonType type);

  default void write(BsonWriter bsonWriter, @Nullable T value) {
    if (value != null) {
      writeValue(bsonWriter, value);
      return;
    }

    bsonWriter.writeNull();
  }

  void writeValue(BsonWriter bsonWriter, T value);

  default void write(BsonWriter bsonWriter, byte[] key, @Nullable T value) {
    bsonWriter.writeName(key);

    if (value != null) {
      writeValue(bsonWriter, value);
      return;
    }

    bsonWriter.writeNull();
  }

  default void write(BsonWriter bsonWriter, String key, @Nullable T value) {
    bsonWriter.writeName(key);

    if (value != null) {
      writeValue(bsonWriter, value);
      return;
    }

    bsonWriter.writeNull();
  }
}
