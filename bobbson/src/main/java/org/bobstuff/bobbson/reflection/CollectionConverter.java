package org.bobstuff.bobbson.reflection;

import java.lang.reflect.Type;
import java.util.Collection;
import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BsonReader;
import org.bobstuff.bobbson.BsonType;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CollectionConverter<E, T extends Collection<E>> implements BobBsonConverter<T> {
  private BobBsonConverter<E> converter;
  private InstanceFactory<T> instanceFactory;
  private Type manifest;

  public CollectionConverter(
      Type manifest, InstanceFactory<T> instanceFactory, BobBsonConverter<E> converter) {
    this.converter = converter;
    this.instanceFactory = instanceFactory;
    this.manifest = manifest;
  }

  @Override
  public void write(BsonWriter bsonWriter, byte @Nullable [] key, T values) {
    if (key != null) {
      bsonWriter.writeStartArray(key);
    } else {
      bsonWriter.writeStartArray();
    }

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
  public @Nullable T read(BsonReader bsonReader) {
    bsonReader.readStartArray();

    T collection = null;
    try {
      collection = instanceFactory.instance();
    } catch (Exception e) {
      throw new RuntimeException("unable to create new instance of " + manifest, e);
    }
    while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
      var instance = converter.read(bsonReader);
      if (instance == null) {
        throw new RuntimeException(
            "cant handle null for now because checker framework blocking it");
      }
      collection.add(instance);
    }

    bsonReader.readEndArray();

    return collection;
  }
}
