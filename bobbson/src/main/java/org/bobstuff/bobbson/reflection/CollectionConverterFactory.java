package org.bobstuff.bobbson.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import org.bobstuff.bobbson.BobBson;
import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BobBsonConverterFactory;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CollectionConverterFactory<@Nullable E, T extends Collection<@Nullable E>>
    implements BobBsonConverterFactory<CollectionConverter<E, T>> {
  private static final int SINGLE_ARG_LENGTH = 1;

  @Override
  public @Nullable CollectionConverter<E, T> tryCreate(Type manifest, BobBson bobBson) {
    if (manifest instanceof ParameterizedType) {
      var pt = (ParameterizedType) manifest;
      if (pt.getActualTypeArguments().length == SINGLE_ARG_LENGTH) {
        return analyzeDecoding(
            manifest, pt.getActualTypeArguments()[0], (Class<?>) pt.getRawType(), bobBson);
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private @Nullable CollectionConverter<E, T> analyzeDecoding(
      final Type manifest, final Type element, final Class<?> collection, final BobBson bobBson) {
    if (!Collection.class.isAssignableFrom(collection)) return null;
    final InstanceFactory<?> newInstance;
    if (!collection.isInterface()) {
      newInstance =
          () -> {
            try {
              return collection.getConstructor().newInstance();
            } catch (Exception ex) {
              return null;
            }
          };
    } else if (Set.class.isAssignableFrom(collection)) {
      newInstance = () -> new LinkedHashSet<>(4);
    } else if (List.class.isAssignableFrom(collection) || Collection.class == collection) {
      newInstance = () -> new ArrayList<>(4);
    } else if (Queue.class.isAssignableFrom(collection)) {
      newInstance = LinkedList::new;
    } else {
      return null;
    }
    final BobBsonConverter<?> typeConverter = bobBson.tryFindConverter(element);
    if (typeConverter == null) {
      return null;
    }
    final CollectionConverter converter =
        new CollectionConverter(manifest, newInstance, typeConverter);
    bobBson.registerConverter(manifest, converter);
    return converter;
  }
}
