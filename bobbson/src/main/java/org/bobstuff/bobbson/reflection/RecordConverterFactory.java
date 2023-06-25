package org.bobstuff.bobbson.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import org.bobstuff.bobbson.BobBson;
import org.bobstuff.bobbson.BobBsonConverterFactory;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Factory to return record converters configured for requested record type. */
public class RecordConverterFactory<@Nullable T>
    implements BobBsonConverterFactory<RecordConverter<T>> {
  @Override
  public @Nullable RecordConverter<T> tryCreate(Type manifest, BobBson bobBson) {
    // TODO should I deal with parameterized types
    if (manifest instanceof ParameterizedType) {
      return null;
    }

    try {
      return analyse((Class<?>) manifest, bobBson);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private @Nullable RecordConverter<T> analyse(Class<?> clazz, BobBson bobBson) throws Exception {
    if (Map.class.isAssignableFrom(clazz)) {
      return null;
    }
    if (Collection.class.isAssignableFrom(clazz)) {
      return null;
    }
    if (!clazz.isRecord()) {
      return null;
    }

    var beanFields = ReflectionTools.parseRecordFields(clazz, bobBson);
    return new RecordConverter<>(bobBson, clazz, beanFields);
  }
}
