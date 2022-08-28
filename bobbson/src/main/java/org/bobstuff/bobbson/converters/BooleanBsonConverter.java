package org.bobstuff.bobbson.converters;

import static java.lang.String.format;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BooleanBsonConverter implements BobBsonConverter<Boolean> {
  @Override
  public @Nullable Boolean read(@NonNull BsonReader bsonReader) {
    BsonType type = bsonReader.getCurrentBsonType();

    if (type == BsonType.NULL) {
      bsonReader.readNull();
      return null;
    }

    if (type != BsonType.BOOLEAN) {
      throw new RuntimeException(format("Attempting to read %s bson type as a boolean", type));
    }

    return bsonReader.readBoolean();
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, byte @Nullable [] key, Boolean value) {
    if (key == null) {
      bsonWriter.writeBoolean(value);
    } else {
      bsonWriter.writeBoolean(key, value);
    }
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, @NonNull Boolean value) {
    bsonWriter.writeBoolean(value);
  }
}
