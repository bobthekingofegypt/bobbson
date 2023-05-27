package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonDecimal128;
import org.bson.types.Decimal128;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonDecimal128Converter implements BobBsonConverter<BsonDecimal128> {
  @Override
  public @Nullable BsonDecimal128 readValue(BsonReader bsonReader, BsonType type) {
    org.bobstuff.bobbson.Decimal128 value = bsonReader.readDecimal128();
    return new BsonDecimal128(Decimal128.fromIEEE754BIDEncoding(value.getHigh(), value.getLow()));
  }

  @Override
  public void writeValue(@NonNull BsonWriter bsonWriter, BsonDecimal128 value) {
    bsonWriter.writeDecimal128(value.getValue().getHigh(), value.getValue().getLow());
  }
}
