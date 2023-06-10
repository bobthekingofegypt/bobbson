package org.bobstuff.bobbson.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bobstuff.bobbson.BobBson;
import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BobBsonConverterFactory;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MapConverterFactory<@Nullable E, T extends Map<String, E>>
    implements BobBsonConverterFactory<MapConverter<E, T>> {
  private static final int TWO_ARG_GENERIC_TYPE = 2;

  @Override
  public @Nullable MapConverter<E, T> tryCreate(Type manifest, BobBson bobBson) {
    // TODO what to do about non paramaterised maps, we can't really convert them here
    if (manifest instanceof ParameterizedType) {
      final ParameterizedType pt = (ParameterizedType) manifest;
      if (pt.getActualTypeArguments().length == TWO_ARG_GENERIC_TYPE) {
        if (!String.class.isAssignableFrom((Class<?>) pt.getActualTypeArguments()[0])) {
          return null;
        }
        return analyzeConverter(
            manifest,
            pt.getActualTypeArguments()[0],
            pt.getActualTypeArguments()[1],
            (Class<?>) pt.getRawType(),
            bobBson);
      }
    }
    return null;
  }

  private static boolean canNew(final Class<?> map) {
    try {
      map.getConstructor().newInstance();
    } catch (Exception ex) {
      return false;
    }
    return true;
  }

  @SuppressWarnings("unchecked")
  private @Nullable MapConverter<E, T> analyzeConverter(
      final Type manifest,
      final Type key,
      final Type value,
      final Class<?> map,
      final BobBson bobBson) {
    if (!Map.class.isAssignableFrom(map)) return null;
    final InstanceFactory<?> newInstance;
    if (!map.isInterface() && canNew(map)) {
      newInstance =
          () -> {
            try {
              return map.getConstructor().newInstance();
            } catch (Exception ex) {
              return null;
            }
          };
    } else if (map.isAssignableFrom(LinkedHashMap.class)) {
      newInstance = () -> new LinkedHashMap<>(4);
    } else {
      return null;
    }
    final InstanceFactory<T> n = (InstanceFactory<T>) newInstance;

    final BobBsonConverter<E> valueConverter =
        (BobBsonConverter<E>) bobBson.tryFindConverter(value);
    if (valueConverter == null) {
      return null;
    }
    final MapConverter<E, T> converter = new MapConverter<>(manifest, n, valueConverter);
    bobBson.registerConverter(manifest, converter);
    return converter;
  }
}
