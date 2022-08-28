package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonDbPointer;
import org.bson.types.ObjectId;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonDBPointerConverter implements BobBsonConverter<BsonDbPointer> {
  @Override
  public @Nullable BsonDbPointer read(BsonReader bsonReader) {
    var raw = bsonReader.readDbPointerRaw();
    return new BsonDbPointer(raw.getNamespace(), new ObjectId(raw.getObjectId()));
  }

  @Override
  public void write(
      @NonNull BsonWriter bsonWriter, byte @Nullable [] key, @NonNull BsonDbPointer value) {
    if (key == null) {
      bsonWriter.writeDbPointer(value.getNamespace(), value.getId().toByteArray());
    } else {
      bsonWriter.writeDbPointer(key, value.getNamespace(), value.getId().toByteArray());
    }
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, @NonNull BsonDbPointer value) {
    bsonWriter.writeDbPointer(value.getNamespace(), value.getId().toByteArray());
  }
}
