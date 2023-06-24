package org.bobstuff.bobbson.converters;

import java.util.*;
import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CollectionConverters {
  public static <@Nullable T> @Nullable List<T> readList(
      BsonReader reader, BsonType currentType, BobBsonConverter<T> valueConverter) {
    if (currentType == BsonType.NULL) {
      reader.readNull();
      return null;
    }

    var list = new ArrayList<T>(4);

    reader.readStartArray();
    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
      list.add(valueConverter.read(reader));
    }
    reader.readEndArray();

    return list;
  }

  public static <@Nullable T> @Nullable Set<T> readSet(
      BsonReader reader, BsonType currentType, BobBsonConverter<T> valueConverter) {
    if (currentType == BsonType.NULL) {
      reader.readNull();
      return null;
    }

    var list = new HashSet<T>(4);

    reader.readStartArray();
    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
      list.add(valueConverter.read(reader));
    }
    reader.readEndArray();

    return list;
  }

  public static <@Nullable V> @Nullable Map<String, V> readMap(
      BsonReader reader, BsonType currentType, BobBsonConverter<V> valueConverter) {
    if (currentType == BsonType.NULL) {
      reader.readNull();
      return null;
    }

    var map = new HashMap<String, V>(4);

    reader.readStartDocument();
    while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
      map.put(reader.currentFieldName(), valueConverter.read(reader));
    }
    reader.readEndArray();

    return map;
  }

  public static <@Nullable T> void writeList(
      BsonWriter writer, List<T> list, BobBsonConverter<T> valueConverter) {
    if (list == null) {
      writer.writeNull();
      return;
    }

    writer.writeStartArray();
    for (var i = 0; i < list.size(); i += 1) {
      valueConverter.write(writer, list.get(i));
    }
    writer.writeEndArray();
  }

  public static <@Nullable T> void writeSet(
      BsonWriter writer, Set<T> set, BobBsonConverter<T> valueConverter) {
    if (set == null) {
      writer.writeNull();
      return;
    }

    writer.writeStartArray();
    for (var item : set) {
      valueConverter.write(writer, item);
    }
    writer.writeEndArray();
  }

  public static <@Nullable T> void writeMap(
      BsonWriter writer, Map<String, T> map, BobBsonConverter<T> valueConverter) {
    if (map == null) {
      writer.writeNull();
      return;
    }

    writer.writeStartDocument();
    for (var item : map.entrySet()) {
      valueConverter.write(writer, item.getKey(), item.getValue());
    }
    writer.writeEndDocument();
  }
}
