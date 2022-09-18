package org.bobstuff.bobbson;

import java.nio.charset.StandardCharsets;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface BobBsonConverter<T> {
//  boolean containsMongoId();
//
//  ObjectId mongoId
//
  default @Nullable T read(BsonReader bsonReader) {
    throw new IllegalStateException("override read to create custom reader");
  }

  default void write(BsonWriter bsonWriter, @NonNull T value) {
    write(bsonWriter, (byte[]) null, value);
  }

  default void write(BsonWriter bsonWriter, byte @Nullable [] key, @NonNull T value) {
    throw new IllegalStateException("override write to create custom writer");
  }

  default void write(BsonWriter bsonWriter, String key, @NonNull T value) {
    write(bsonWriter, key.getBytes(StandardCharsets.UTF_8), value);
  }
}
