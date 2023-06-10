package org.bobstuff.bobbson.reflection;

import java.lang.reflect.Type;
import java.util.Map;
import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * MapConverter handles reflection based read/write of maps to bson. Maps must have a string key and
 * should have a single value type
 *
 * @param <V> type of value in the map
 * @param <T> type of Map
 */
public class MapConverter<@Nullable V, T extends Map<String, V>>
    implements BobBsonConverter<Map<String, V>> {
  private final BobBsonConverter<@Nullable V> valueConverter;
  private final InstanceFactory<T> instanceFactory;
  private final Type manifest;

  public MapConverter(
      Type manifest,
      InstanceFactory<T> instanceFactory,
      BobBsonConverter<@Nullable V> valueConverter) {
    this.valueConverter = valueConverter;
    this.instanceFactory = instanceFactory;
    this.manifest = manifest;
  }

  @Override
  public void writeValue(BsonWriter bsonWriter, Map<String, V> values) {
    bsonWriter.writeStartDocument();

    for (Map.Entry<String, V> value : values.entrySet()) {
      var keyValue = value.getKey();
      var valueValue = value.getValue();
      bsonWriter.writeName(keyValue);
      valueConverter.write(bsonWriter, valueValue);
    }

    bsonWriter.writeEndDocument();
  }

  @Override
  public @Nullable T readValue(BsonReader bsonReader, BsonType type) {
    bsonReader.readStartDocument();

    T map;
    try {
      map = instanceFactory.instance();
    } catch (Exception e) {
      throw new RuntimeException("unable to create new instance of " + manifest, e);
    }
    while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
      var key = bsonReader.currentFieldName();
      var value = valueConverter.read(bsonReader);
      map.put(key, value);
    }

    bsonReader.readEndDocument();

    return map;
  }
}
