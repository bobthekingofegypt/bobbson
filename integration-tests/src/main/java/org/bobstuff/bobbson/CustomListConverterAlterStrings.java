package org.bobstuff.bobbson;

import java.util.List;
import org.bobstuff.bobbson.converters.CollectionConverters;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.UnknownKeyFor;

public class CustomListConverterAlterStrings implements BobBsonConverter<List<String>> {
  private AlterStrings stringBsonConverter = new AlterStrings();

  @Override
  public @Nullable List<String> readValue(
      @UnknownKeyFor @NonNull @Initialized BsonReader bsonReader,
      @UnknownKeyFor @NonNull @Initialized BsonType bsonType) {
    return CollectionConverters.readList(bsonReader, bsonType, stringBsonConverter);
  }

  @Override
  public void writeValue(
      @UnknownKeyFor @NonNull @Initialized BsonWriter bsonWriter, List<String> s) {
    CollectionConverters.writeList(bsonWriter, s, stringBsonConverter);
  }

  public static class AlterStrings implements BobBsonConverter<String> {

    @Override
    public @Nullable String readValue(BsonReader bsonReader, BsonType type) {
      var s = bsonReader.readString();
      return s.substring(1);
    }

    @Override
    public void writeValue(BsonWriter bsonWriter, String value) {
      bsonWriter.writeString("!" + value);
    }
  }
}
