package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonJavaScript;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonJavaScriptConverter implements BobBsonConverter<BsonJavaScript> {
  @Override
  public @Nullable BsonJavaScript readValue(BsonReader bsonReader, BsonType type) {
    return new BsonJavaScript(bsonReader.readString());
  }

  @Override
  public void writeValue(@NonNull BsonWriter bsonWriter, BsonJavaScript value) {
    bsonWriter.writeJavascript(value.getCode());
  }
}
