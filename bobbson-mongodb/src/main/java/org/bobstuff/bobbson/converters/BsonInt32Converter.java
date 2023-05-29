package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonInt32;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonInt32Converter implements BobBsonConverter<BsonInt32> {
  @Override
  public @Nullable BsonInt32 readValue(BsonReader bsonReader, BsonType type) {
    return new BsonInt32(bsonReader.readInt32());
  }

  @Override
  public void writeValue(@NonNull BsonWriter bsonWriter, BsonInt32 value) {
    bsonWriter.writeInteger(value.getValue());
  }
}
