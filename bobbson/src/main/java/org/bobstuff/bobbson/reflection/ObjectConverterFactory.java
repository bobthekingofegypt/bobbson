package org.bobstuff.bobbson.reflection;

import java.lang.reflect.Type;
import org.bobstuff.bobbson.BobBson;
import org.bobstuff.bobbson.BobBsonConverterFactory;

public class ObjectConverterFactory implements BobBsonConverterFactory<ReflectionBasedConverter> {
  @Override
  public ReflectionBasedConverter tryCreate(Type manifest, BobBson bobBson) {
    // TODO deal with parameterized types
    try {
      return analyse(manifest, (Class<?>) manifest, bobBson);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private ReflectionBasedConverter analyse(Type manifest, Class<?> clazz, BobBson bobBson)
      throws Exception {
    var beanFields = ReflectionTools.parseBeanFields(clazz);
    return new ReflectionBasedConverter(bobBson, clazz, beanFields);
  }
}
