package org.bobstuff.bobbson.reflection;

import java.lang.reflect.Type;
import java.util.Map;
import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MapConverter<V, T extends Map<String, V>> implements BobBsonConverter<Map<String, V>> {
  private BobBsonConverter<V> valueConverter;
  private InstanceFactory<T> instanceFactory;
  private Type manifest;

  public MapConverter(
      Type manifest, InstanceFactory<T> instanceFactory, BobBsonConverter<V> valueConverter) {
    this.valueConverter = valueConverter;
    this.instanceFactory = instanceFactory;
    this.manifest = manifest;
  }

  @Override
  public void writeValue(BsonWriter bsonWriter, Map<String, V> values) {
    bsonWriter.writeStartDocument();

    for (Map.Entry<String, V> value : values.entrySet()) {
      if (value == null) {
        throw new RuntimeException("entry should never be null");
      } else {
        var keyValue = value.getKey();
        var valueValue = value.getValue();
        if (keyValue == null || valueValue == null) {
          throw new RuntimeException("key and value cannot be null");
        }
        bsonWriter.writeName(keyValue);
        valueConverter.write(bsonWriter, valueValue);
      }
    }

    bsonWriter.writeEndDocument();
  }

  @Override
  public @Nullable T readValue(BsonReader bsonReader, BsonType type) {
    bsonReader.readStartDocument();

    T map = null;
    try {
      map = instanceFactory.instance();
    } catch (Exception e) {
      throw new RuntimeException("unable to create new instance of " + manifest, e);
    }
    while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
      var key = bsonReader.currentFieldName();
      if (key == null) {
        throw new RuntimeException(
            "cant handle null for now because checker framework blocking it");
      }

      var value = valueConverter.read(bsonReader);
      if (value == null) {
        throw new RuntimeException(
            "cant handle null for now because checker framework blocking it");
      }
      map.put(key, value);
    }

    bsonReader.readEndDocument();

    return map;
  }
}
