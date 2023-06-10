package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonSymbol;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonSymbolConverter implements BobBsonConverter<BsonSymbol> {
  @Override
  public @Nullable BsonSymbol readValue(BsonReader bsonReader, BsonType type) {
    return new BsonSymbol(bsonReader.readString());
  }

  @Override
  public void writeValue(BsonWriter bsonWriter, BsonSymbol value) {
    bsonWriter.writeSymbol(value.getSymbol());
  }
}
