package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonSymbol;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonSymbolConverter implements BobBsonConverter<BsonSymbol> {
  @Override
  public @Nullable BsonSymbol read(BsonReader bsonReader) {
    return new BsonSymbol(bsonReader.readString());
  }

  @Override
  public void write(
      @NonNull BsonWriter bsonWriter, byte @Nullable [] key, @NonNull BsonSymbol value) {
    if (key == null) {
      bsonWriter.writeSymbol(value.getSymbol());
    } else {
      bsonWriter.writeSymbol(key, value.getSymbol());
    }
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, @NonNull BsonSymbol value) {
    bsonWriter.writeSymbol(value.getSymbol());
  }
}
