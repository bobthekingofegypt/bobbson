package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BinaryConverter implements BobBsonConverter<Binary> {
  @Override
  public @Nullable Binary read(BsonReader bsonReader) {
    var binary = bsonReader.readBinary();
    return new Binary(BsonBinarySubType.BINARY, binary.getData());
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, byte @Nullable [] key, @NonNull Binary value) {
    if (key == null) {
      bsonWriter.writeBinary(value.getType(), value.getData());
    } else {
      bsonWriter.writeBinary(key, value.getType(), value.getData());
    }
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, @NonNull Binary value) {
    bsonWriter.writeBinary(value.getType(), value.getData());
  }
}
