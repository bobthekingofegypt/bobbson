package org.bobstuff.bobbson.reflection;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.bobstuff.bobbson.*;
import org.bobstuff.bobbson.buffer.BobBsonBuffer;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * ObjectConverter handles reflection based read/write of objects with no specific registered
 * converters.
 */
public class ObjectConverter<@Nullable T> implements BobBsonConverter<T> {
  private final BobBson bobBson;
  private final List<ReflectionField> fields;
  private final Class<?> instanceClazz;

  /**
   * Construct a new object converter. Object converter will ask bobBson instance for converters for
   * each field of the bean as required to read/write its values.
   *
   * @param bobBson instance to query for field converters
   * @param instanceClazz type of class to be read/writen
   * @param fields list of fields on the bean
   */
  public ObjectConverter(BobBson bobBson, Class<?> instanceClazz, List<ReflectionField> fields) {
    this.bobBson = bobBson;
    this.fields = fields;
    this.instanceClazz = instanceClazz;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void writeValue(BsonWriter bsonWriter, T instance) {
    bsonWriter.writeStartDocument();
    for (var field : fields) {
      var converter = field.getConverter();
      if (converter == null) {
        converter = bobBson.tryFindConverter(field.getType());
      }
      if (converter == null) {
        throw new IllegalStateException("broken because no converter for type");
      }
      var value = field.getGetterFunction().apply(instance);
      if (value != null || field.isWriteNull()) {
        converter.write(bsonWriter, field.nameBytes, value);
      }
    }
    bsonWriter.writeEndDocument();
  }

  @Override
  @SuppressWarnings("unchecked")
  public @Nullable T readValue(BsonReader bsonReader, BsonType type) {
    T instance = null;
    try {
      instance = (T) instanceClazz.getConstructor().newInstance();
    } catch (InvocationTargetException
        | InstantiationException
        | IllegalAccessException
        | NoSuchMethodException e) {
      throw new RuntimeException("failed to initialise instance " + instanceClazz, e);
    }

    bsonReader.readStartDocument();

    BobBsonBuffer.ByteRangeComparator nameComparator = bsonReader.getFieldName();
    while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
      if (bsonReader.getCurrentBsonType() == BsonType.NULL) {
        bsonReader.readNull();
        continue;
      }
      boolean found = false;
      for (var field : fields) {
        if (nameComparator.equalsArray(field.nameBytes, field.weakHash)) {
          var converter = field.getConverter();
          if (converter == null) {
            converter = bobBson.tryFindConverter(field.getType());
          }
          if (converter == null) {
            throw new IllegalStateException("broken because no converter for type");
          }

          var setter = field.getBiConsumerSetter();
          if (setter == null) {
            throw new IllegalStateException("setter cannot be null in object converter");
          }
          setter.accept(instance, converter.read(bsonReader));
          found = true;
          break;
        }
      }
      if (!found) {
        bsonReader.skipValue();
      }
    }

    bsonReader.readEndDocument();

    return instance;
  }
}
