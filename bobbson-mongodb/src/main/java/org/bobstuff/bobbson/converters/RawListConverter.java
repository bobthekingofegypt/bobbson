package org.bobstuff.bobbson.converters;

import java.util.ArrayList;
import java.util.List;
import org.bobstuff.bobbson.BobBson;
import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class RawListConverter implements BobBsonConverter<List> {
  private BobBson bobBson;

  public RawListConverter(BobBson bobBson) {
    this.bobBson = bobBson;
  }

  @Override
  @SuppressWarnings("PMD.AssignmentInOperand")
  public @Nullable List read(BsonReader bsonReader) {
    List<Object> values = new ArrayList<>();

    bsonReader.readStartArray();
    BsonType type;
    while ((type = bsonReader.readBsonType()) != BsonType.END_OF_DOCUMENT) {
      Class<?> clazz = DocumentConverter.bsonTypeClassMap.get(type);
      if (clazz == null) {
        throw new IllegalStateException(
            String.format("No type map entry registered for value type %s", type.toString()));
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
      values.add(value);
    }
    bsonReader.readEndArray();

    return values;
  }

  @Override
  public void write(@NonNull BsonWriter bsonWriter, byte @Nullable [] key, @NonNull List value) {
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
  public void write(@NonNull BsonWriter bsonWriter, @NonNull List value) {
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
