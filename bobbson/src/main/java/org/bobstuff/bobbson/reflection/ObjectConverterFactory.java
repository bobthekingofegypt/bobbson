package org.bobstuff.bobbson.reflection;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import org.bobstuff.bobbson.BobBson;
import org.bobstuff.bobbson.BobBsonConverterFactory;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ObjectConverterFactory implements BobBsonConverterFactory<ObjectConverter> {
  @Override
  public @Nullable ObjectConverter tryCreate(Type manifest, BobBson bobBson) {
    // TODO should I deal with parameterized types
    try {
      return analyse((Class<?>) manifest, bobBson);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private @Nullable ObjectConverter analyse(Class<?> clazz, BobBson bobBson) throws Exception {
    if (Map.class.isAssignableFrom(clazz)) {
      return null;
    }
    if (Collection.class.isAssignableFrom(clazz)) {
      return null;
    }

    // lets try to instantiate the class so we get an exception straight await if we can't
    clazz.getConstructor().newInstance();

    var beanFields = ReflectionTools.parseBeanFields(clazz, bobBson);
    return new ObjectConverter(bobBson, clazz, beanFields);
  }
}
