package org.bobstuff.bobbson.reflection;

import java.util.List;
import org.bobstuff.bobbson.*;

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
  public Object read(BsonReader bsonReader) {
    Object instance = null;
    try {
      instance = instanceClazz.newInstance();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    if (instance == null) {
      throw new RuntimeException("failed to initialise instance");
    }

    bsonReader.readStartDocument();

    BobBsonBuffer.ByteRangeComparitor nameComparitor = bsonReader.getFieldName();
    while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
      boolean found = false;
      for (var field : fields) {
        if (nameComparitor.equalsArray(field.nameBytes, field.weakHash)) {
          var converter = bobBson.tryFindConverter(field.getType());
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
