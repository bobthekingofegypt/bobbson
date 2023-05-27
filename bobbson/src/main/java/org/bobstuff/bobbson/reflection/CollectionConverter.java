package org.bobstuff.bobbson.reflection;

import java.lang.reflect.Type;
import java.util.Collection;
import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CollectionConverter<@Nullable E, T extends Collection<@Nullable E>>
    implements BobBsonConverter<T> {
  private final BobBsonConverter<@Nullable E> converter;
  private final InstanceFactory<T> instanceFactory;
  private final Type manifest;

  public CollectionConverter(
      Type manifest, InstanceFactory<T> instanceFactory, BobBsonConverter<E> converter) {
    this.converter = converter;
    this.instanceFactory = instanceFactory;
    this.manifest = manifest;
  }

  @Override
  public void writeValue(BsonWriter bsonWriter, T values) {
    bsonWriter.writeStartArray();

    for (var value : values) {
      if (value == null) {
        bsonWriter.writeNull();
      } else {
        converter.write(bsonWriter, value);
      }
    }

    bsonWriter.writeEndArray();
  }

  @Override
  public @Nullable T readValue(BsonReader bsonReader, BsonType type) {
    bsonReader.readStartArray();

    T collection;
    try {
      collection = instanceFactory.instance();
    } catch (Exception e) {
      throw new RuntimeException("unable to create new instance of " + manifest, e);
    }
    while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
      var instance = converter.read(bsonReader);
      collection.add(instance);
    }

    bsonReader.readEndArray();

    return collection;
  }
}
