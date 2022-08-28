package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonObjectId;
import org.bson.types.ObjectId;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonObjectIdConverter implements BobBsonConverter<BsonObjectId> {
  @Override
  public @Nullable BsonObjectId read(BsonReader bsonReader) {
    return new BsonObjectId(new ObjectId(bsonReader.readObjectId()));
  }

  @Override
  public void write(
      @NonNull BsonWriter bsonWriter, byte @Nullable [] key, @NonNull BsonObjectId value) {
    if (key == null) {
      bsonWriter.writeObjectId(value.getValue().toByteArray());
    } else {
      bsonWriter.writeObjectId(key, value.getValue().toByteArray());
    }
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, @NonNull BsonObjectId value) {
    bsonWriter.writeObjectId(value.getValue().toByteArray());
  }
}
