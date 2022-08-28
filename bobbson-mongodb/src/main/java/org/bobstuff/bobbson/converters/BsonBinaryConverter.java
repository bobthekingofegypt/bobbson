package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonBinary;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonBinaryConverter implements BobBsonConverter<BsonBinary> {
  @Override
  public @Nullable BsonBinary read(BsonReader bsonReader) {
    var binary = bsonReader.readBinary();
    return new BsonBinary(binary.getType(), binary.getData());
  }

  @Override
  public void write(
      @NonNull BsonWriter bsonWriter, byte @Nullable [] key, @NonNull BsonBinary value) {
    if (key == null) {
      bsonWriter.writeBinary(value.getType(), value.getData());
    } else {
      bsonWriter.writeBinary(key, value.getType(), value.getData());
    }
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, @NonNull BsonBinary value) {
    bsonWriter.writeBinary(value.getType(), value.getData());
  }
}
