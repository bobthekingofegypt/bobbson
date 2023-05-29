package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonString;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonStringConverter implements BobBsonConverter<BsonString> {
  @Override
  public @Nullable BsonString readValue(BsonReader bsonReader, BsonType type) {
    return new BsonString(bsonReader.readString());
  }

  @Override
  public void writeValue(@NonNull BsonWriter bsonWriter, BsonString value) {
    bsonWriter.writeString(value.getValue());
  }
}
