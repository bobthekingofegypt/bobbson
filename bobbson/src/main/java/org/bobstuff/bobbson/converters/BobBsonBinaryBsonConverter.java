package org.bobstuff.bobbson.converters;

import static java.lang.String.format;

import org.bobstuff.bobbson.models.BobBsonBinary;
import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Converter for BobBsonBinary instances. */
public class BobBsonBinaryBsonConverter implements BobBsonConverter<BobBsonBinary> {
  @Override
  public @Nullable BobBsonBinary readValue(@NonNull BsonReader bsonReader, BsonType type) {
    if (type != BsonType.BINARY) {
      throw new RuntimeException(format("Attempting to read %s bson type as a bsonbinary", type));
    }

    return bsonReader.readBinary();
  }

  @Override
  public void writeValue(BsonWriter bsonWriter, BobBsonBinary value) {
    throw new UnsupportedOperationException("Write is not supported");
  }
}
