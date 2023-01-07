package org.bobstuff.bobbson.converters;

import java.util.HexFormat;
import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.UnknownKeyFor;

public class MongoDbObjectIdConverter implements BobBsonConverter<String> {
  @Override
  public @Nullable String read(@UnknownKeyFor @NonNull @Initialized BsonReader bsonReader) {
    return HexFormat.of().formatHex(bsonReader.readObjectId());
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, byte @Nullable [] key, @NonNull String value) {
    if (value == null) {
      if (key == null) {
        bsonWriter.writeNull();
      } else {
        bsonWriter.writeNull(key);
      }
    } else {
      byte[] byteValue = HexFormat.of().parseHex(value);
      if (key == null) {
        bsonWriter.writeObjectId(byteValue);
      } else {
        bsonWriter.writeObjectId(key, byteValue);
      }
    }
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, @NonNull String value) {
    write(bsonWriter, (byte[]) null, value);
  }
}
