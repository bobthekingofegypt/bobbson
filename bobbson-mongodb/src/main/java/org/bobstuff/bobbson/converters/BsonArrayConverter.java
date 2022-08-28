package org.bobstuff.bobbson.converters;

import java.util.ArrayList;
import java.util.List;
import org.bobstuff.bobbson.BobBson;
import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.bson.*;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BsonArrayConverter implements BobBsonConverter<BsonArray> {
  private BobBson bobBson;

  public BsonArrayConverter(BobBson bobBson) {
    this.bobBson = bobBson;
  }

  @Override
  @SuppressWarnings("PMD.AssignmentInOperand")
  public @Nullable BsonArray read(BsonReader bsonReader) {
    List<BsonValue> values = new ArrayList<>();

    bsonReader.readStartArray();
    BsonType type;
    while ((type = bsonReader.readBsonType()) != BsonType.END_OF_DOCUMENT) {
      Class<?> clazz = BsonDocumentConverter.bsonTypeClassMap.get(type);
      if (clazz == null) {
        throw new IllegalStateException(
            String.format(
                "No BSONValue type map entry registered for BSONValue type %s", type.toString()));
      }
      var converter = bobBson.tryFindConverter(clazz);
      if (converter == null) {
        throw new IllegalStateException(
            String.format("No converter registered for %s", clazz.getSimpleName()));
      }
      var value = converter.read(bsonReader);
      if (value == null) {
        throw new IllegalStateException(String.format("Decoded BSONValue value is null"));
      }
      values.add((BsonValue) value);
    }
    bsonReader.readEndArray();

    return new BsonArray(values);
  }

  @Override
  public void write(
      @NonNull BsonWriter bsonWriter, byte @Nullable [] key, @NonNull BsonArray value) {
    if (key == null) {
      bsonWriter.writeStartArray();
    } else {
      bsonWriter.writeStartArray(key);
    }
    int i = 0;
    for (var entry : value) {
      bsonWriter.writeName(String.valueOf(i));
      var clazz = entry.getClass();
      var converter = (BobBsonConverter) bobBson.tryFindConverter(clazz);
      if (converter == null) {
        throw new IllegalStateException(
            String.format("No converter registered for %s", clazz.getSimpleName()));
      }
      converter.write(bsonWriter, entry);
      i += 1;
    }
    bsonWriter.writeEndArray();
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, @NonNull BsonArray value) {
    bsonWriter.writeStartArray();
    int i = 0;
    for (var entry : value) {
      bsonWriter.writeName(String.valueOf(i));
      var clazz = entry.getClass();
      var converter = (BobBsonConverter) bobBson.tryFindConverter(clazz);
      if (converter == null) {
        throw new IllegalStateException(
            String.format("No converter registered for %s", clazz.getSimpleName()));
      }
      converter.write(bsonWriter, entry);
      i += 1;
    }
    bsonWriter.writeEndArray();
  }
}
