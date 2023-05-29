package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonObjectId;
import org.bson.types.ObjectId;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonObjectIdConverter implements BobBsonConverter<BsonObjectId> {
  @Override
  public @Nullable BsonObjectId readValue(BsonReader bsonReader, BsonType type) {
    return new BsonObjectId(new ObjectId(bsonReader.readObjectId()));
  }

  @Override
  public void writeValue(@NonNull BsonWriter bsonWriter, BsonObjectId value) {
    bsonWriter.writeObjectId(value.getValue().toByteArray());
  }
}
