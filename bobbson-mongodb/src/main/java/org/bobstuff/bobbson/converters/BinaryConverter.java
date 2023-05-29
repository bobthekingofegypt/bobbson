package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BinaryConverter implements BobBsonConverter<Binary> {
  @Override
  public @Nullable Binary readValue(BsonReader bsonReader, BsonType type) {
    var binary = bsonReader.readBinary();
    return new Binary(BsonBinarySubType.BINARY, binary.getData());
  }

  @Override
  public void writeValue(@NonNull BsonWriter bsonWriter, Binary value) {
    bsonWriter.writeBinary(value.getType(), value.getData());
  }
}
