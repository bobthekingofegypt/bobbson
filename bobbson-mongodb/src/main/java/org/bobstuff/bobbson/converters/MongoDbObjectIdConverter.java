package org.bobstuff.bobbson.converters;

import java.util.HexFormat;
import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MongoDbObjectIdConverter implements BobBsonConverter<String> {
  @Override
  public @Nullable String readValue(BsonReader bsonReader, BsonType type) {
    return HexFormat.of().formatHex(bsonReader.readObjectId());
  }

  @Override
  public void writeValue(BsonWriter bsonWriter, String value) {
    byte[] byteValue = HexFormat.of().parseHex(value);
    bsonWriter.writeObjectId(byteValue);
  }
}
