package org.bobstuff.bobbson.converters;

import static java.lang.String.format;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BooleanBsonConverter implements BobBsonConverter<Boolean> {
  @Override
  public @Nullable Boolean readValue(BsonReader bsonReader, BsonType type) {
    if (type == BsonType.BOOLEAN) {
      return bsonReader.readBoolean();
    }

    throw new RuntimeException(format("Attempting to read %s bson type as a boolean", type));
  }

  @Override
  public void writeValue(@NonNull BsonWriter bsonWriter, Boolean value) {
    bsonWriter.writeBoolean(value);
  }
}
