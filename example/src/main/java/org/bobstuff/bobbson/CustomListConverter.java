package org.bobstuff.bobbson;

import java.util.ArrayList;
import java.util.List;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CustomListConverter implements BobBsonConverter<List<String>> {
  @Override
  public @Nullable List<String> read(BsonReader bsonReader) {
    System.out.println("using a custom reader for a list");
    List<String> results = new ArrayList<>();
    bsonReader.readStartArray();
    while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
      results.add(bsonReader.readString());
    }
    bsonReader.readEndArray();
    return results;
  }

  @Override
  public void write(BsonWriter bsonWriter, byte[] key, List<String> value) {
    bsonWriter.writeStartArray(key);
    var index = 0;
    for (var entry : value) {
      bsonWriter.writeString("" + index, entry);
      index += 1;
    }
    bsonWriter.writeEndArray();
  }
}
