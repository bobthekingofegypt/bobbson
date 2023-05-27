package org.bobstuff.bobbson.converters;

import static java.lang.String.format;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DoubleBsonConverter implements BobBsonConverter<Double> {
  @Override
  public @Nullable Double readValue(BsonReader bsonReader, BsonType type) {
    if (type == BsonType.INT64) {
      return (double) bsonReader.readInt64();
    } else if (type == BsonType.INT32) {
      return (double) bsonReader.readInt32();
    }

    if (type != BsonType.DOUBLE) {
      throw new RuntimeException(format("Attempting to read %s bson type as a double", type));
    }

    return bsonReader.readDouble();
  }

  @Override
  public void writeValue(BsonWriter bsonWriter, Double value) {
    bsonWriter.writeDouble(value);
  }
}
