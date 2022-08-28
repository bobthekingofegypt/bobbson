package org.bobstuff.bobbson.processor;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CustomConverter implements BobBsonConverter<String> {
  @Override
  public @Nullable String read(BsonReader bsonReader) {
    System.out.println("using a custom reader");
    return "custom: " + bsonReader.readString();
  }

  @Override
  public void write(BsonWriter writer, byte[] key, String value) {
    writer.writeString(key, value.substring("custom: ".length()));
  }
}
