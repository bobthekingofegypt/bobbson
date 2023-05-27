package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonDbPointer;
import org.bson.types.ObjectId;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonDBPointerConverter implements BobBsonConverter<BsonDbPointer> {
  @Override
  public @Nullable BsonDbPointer readValue(BsonReader bsonReader, BsonType type) {
    var raw = bsonReader.readDbPointerRaw();
    return new BsonDbPointer(raw.getNamespace(), new ObjectId(raw.getObjectId()));
  }

  @Override
  public void writeValue(@NonNull BsonWriter bsonWriter, BsonDbPointer value) {
    bsonWriter.writeDbPointer(value.getNamespace(), value.getId().toByteArray());
  }
}
