package org.bobstuff.bobbson;

import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CustomListToStringConverter implements BobBsonConverter<String> {
  @Override
  public @Nullable String read(BsonReader bsonReader) {
    System.out.println("using a custom reader for a list");
    StringBuilder sb = new StringBuilder();
    bsonReader.readStartArray();
    while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
      sb.append(bsonReader.readString());
    }
    bsonReader.readEndArray();
    return "custom: " + sb;
  }

  @Override
  public void write(BsonWriter bsonWriter, byte[] key, String value) {
    var data = value.substring("custom: ".length());
    bsonWriter.writeStartArray(key);
    for (var i = 0; i < data.length(); i += 1) {
      char c = data.charAt(i);
      bsonWriter.writeString("" + i, "" + c);
    }
    bsonWriter.writeEndArray();
  }
}
