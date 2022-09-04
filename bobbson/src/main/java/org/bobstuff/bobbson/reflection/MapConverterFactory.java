package org.bobstuff.bobbson.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bobstuff.bobbson.BobBson;
import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BobBsonConverterFactory;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MapConverterFactory implements BobBsonConverterFactory<MapConverter> {
  @Override
  @Nullable
  @SuppressWarnings({"PMD.AvoidLiteralsInIfCondition", "unchecked"})
  public MapConverter tryCreate(Type manifest, BobBson bobBson) {
    // TODO what to do about non paramaterised maps, we can't really convert them here
    if (manifest instanceof ParameterizedType) {
      final ParameterizedType pt = (ParameterizedType) manifest;
      if (pt.getActualTypeArguments().length == 2) {
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
      map.newInstance();
      return true;
    } catch (Exception ignore) {
      return false;
    }
  }

  @Nullable
  @SuppressWarnings("unchecked")
  private static MapConverter analyzeConverter(
      final Type manifest,
      final Type key,
      final Type value,
      final Class<?> map,
      final BobBson bobBson) {
    if (!Map.class.isAssignableFrom(map)) return null;
    final InstanceFactory newInstance;
    if (!map.isInterface() && canNew(map)) {
      newInstance = map::newInstance;
    } else if (map.isAssignableFrom(LinkedHashMap.class)) {
      newInstance = () -> new LinkedHashMap<>(4);
    } else {
      return null;
    }

    final BobBsonConverter<?> valueConverter = bobBson.tryFindConverter(value);
    if (valueConverter == null) {
      return null;
    }
    var converter = new MapConverter(manifest, newInstance, valueConverter);
    bobBson.registerConverter(manifest, converter);
    return converter;
  }
}
