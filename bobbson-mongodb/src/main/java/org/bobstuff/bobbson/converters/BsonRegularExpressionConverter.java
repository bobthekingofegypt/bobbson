package org.bobstuff.bobbson.converters;

import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.BsonRegularExpression;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonRegularExpressionConverter implements BobBsonConverter<BsonRegularExpression> {
  @Override
  public @Nullable BsonRegularExpression readValue(BsonReader bsonReader, BsonType type) {
    var value = bsonReader.readRegex();
    return new BsonRegularExpression(value.getRegex(), value.getOptions());
  }

  @Override
  public void writeValue(@NonNull BsonWriter bsonWriter, BsonRegularExpression value) {
    bsonWriter.writeRegex(value.getPattern(), value.getOptions());
  }
}
