package org.bobstuff.bobbson;

import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CustomConverter implements BobBsonConverter<String> {
  @Override
  public @Nullable String readValue(BsonReader bsonReader, BsonType type) {
    return "custom: " + bsonReader.readString();
  }

  @Override
  public void writeValue(BsonWriter writer, String value) {
    writer.writeString(value.substring("custom: ".length()));
  }
}
