package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonRegularExpression;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonRegularExpressionConverter implements BobBsonConverter<BsonRegularExpression> {
  @Override
  public @Nullable BsonRegularExpression read(BsonReader bsonReader) {
    var value = bsonReader.readRegex();
    return new BsonRegularExpression(value.getRegex(), value.getOptions());
  }

  @Override
  public void write(
      @NonNull BsonWriter bsonWriter, byte @Nullable [] key, @NonNull BsonRegularExpression value) {
    if (key == null) {
      bsonWriter.writeRegex(value.getPattern(), value.getOptions());
    } else {
      bsonWriter.writeRegex(key, value.getPattern(), value.getOptions());
    }
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, @NonNull BsonRegularExpression value) {
    bsonWriter.writeRegex(value.getPattern(), value.getOptions());
  }
}
