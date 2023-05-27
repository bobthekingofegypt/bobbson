package org.bobstuff.bobbson;

import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CustomObjectIdConverter implements BobBsonConverter<byte[]> {
  @Override
  public byte @Nullable [] readValue(BsonReader bsonReader, BsonType type) {
    return bsonReader.readObjectId();
  }

  @Override
  public void writeValue(BsonWriter bsonWriter, byte[] value) {
    bsonWriter.writeObjectId(value);
  }
}
