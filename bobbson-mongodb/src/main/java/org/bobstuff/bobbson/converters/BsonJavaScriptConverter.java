package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonJavaScript;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonJavaScriptConverter implements BobBsonConverter<BsonJavaScript> {
  @Override
  public @Nullable BsonJavaScript read(BsonReader bsonReader) {
    return new BsonJavaScript(bsonReader.readString());
  }

  @Override
  public void write(
      @NonNull BsonWriter bsonWriter, byte @Nullable [] key, @NonNull BsonJavaScript value) {
    if (key == null) {
      bsonWriter.writeJavascript(value.getCode());
    } else {
      bsonWriter.writeJavascript(key, value.getCode());
    }
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, @NonNull BsonJavaScript value) {
    bsonWriter.writeJavascript(value.getCode());
  }
}
