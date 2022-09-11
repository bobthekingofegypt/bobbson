package org.bobstuff.bobbson.reflection;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.bobstuff.bobbson.*;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ReflectionBasedConverter implements BobBsonConverter<Object> {
  private BobBson bobBson;
  private List<ReflectionField> fields;
  private Class<?> instanceClazz;

  public ReflectionBasedConverter(
      BobBson bobBson, Class<?> instanceClazz, List<ReflectionField> fields) {
    this.bobBson = bobBson;
    this.fields = fields;
    this.instanceClazz = instanceClazz;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void write(BsonWriter bsonWriter, byte @Nullable [] key, Object instance) {
    if (instance == null) {
      if (key == null) {
        bsonWriter.writeNull();
      } else {
        bsonWriter.writeNull(key);
      }
      return;
    }

    if (key == null) {
      bsonWriter.writeStartDocument();
    } else {
      bsonWriter.writeStartDocument(key);
    }

    for (var field : fields) {
      var converter = field.getConverter();
      if (converter == null) {
        converter = bobBson.tryFindConverter(field.getType());
      }
      if (converter == null) {
        throw new IllegalStateException("broken because no converter for type");
      }
      var value = field.getGetterFunction().apply(instance);
      converter.write(bsonWriter, field.nameBytes, value);
    }
    bsonWriter.writeEndDocument();
  }

  @Override
  public void write(BsonWriter bsonWriter, Object instance) {
    this.write(bsonWriter, (byte[]) null, instance);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object read(BsonReader bsonReader) {
    Object instance = null;
    try {
      instance = instanceClazz.getConstructor().newInstance();
    } catch (InvocationTargetException
        | InstantiationException
        | IllegalAccessException
        | NoSuchMethodException e) {
      throw new RuntimeException("failed to initialise instance " + instanceClazz, e);
    }

    bsonReader.readStartDocument();

    BobBsonBuffer.ByteRangeComparitor nameComparitor = bsonReader.getFieldName();
    while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
      if (bsonReader.getCurrentBsonType() == BsonType.NULL) {
        bsonReader.readNull();
        continue;
      }
      boolean found = false;
      for (var field : fields) {
        if (nameComparitor.equalsArray(field.nameBytes, field.weakHash)) {
          var converter = field.getConverter();
          if (converter == null) {
            converter = bobBson.tryFindConverter(field.getType());
          }
          if (converter == null) {
            throw new IllegalStateException("broken because no converter for type");
          }

          field.getBiConsumerSetter().accept(instance, converter.read(bsonReader));
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
