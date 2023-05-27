package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonBinary;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonBinaryConverter implements BobBsonConverter<BsonBinary> {
  @Override
  public @Nullable BsonBinary readValue(BsonReader bsonReader, BsonType type) {
    var binary = bsonReader.readBinary();
    return new BsonBinary(binary.getType(), binary.getData());
  }

  @Override
  public void writeValue(@NonNull BsonWriter bsonWriter, BsonBinary value) {
    bsonWriter.writeBinary(value.getType(), value.getData());
  }
}
