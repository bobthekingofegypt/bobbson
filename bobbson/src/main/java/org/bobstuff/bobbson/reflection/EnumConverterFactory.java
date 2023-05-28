package org.bobstuff.bobbson.reflection;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import org.bobstuff.bobbson.BobBson;
import org.bobstuff.bobbson.BobBsonConverterFactory;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Reflection based factory that returns an enum converter if possible to create one, null
 * otherwise.
 *
 * @param <T> enum type
 */
public class EnumConverterFactory<T extends Enum<T>>
    implements BobBsonConverterFactory<EnumConverter<T>> {
  @Override
  @SuppressWarnings({"PMD.AvoidLiteralsInIfCondition", "unchecked"})
  public @Nullable EnumConverter<T> tryCreate(Type manifest, BobBson bobBson) {
    if (manifest instanceof Class<?> && ((Class<?>) manifest).isEnum()) {
      return analyze(manifest, (Class<T>) manifest, bobBson);
    }
    return null;
  }

  @SuppressWarnings({"argument"})
  private @Nullable EnumConverter<T> analyze(
      final Type manifest, final Class<T> raw, final BobBson bobBson) {
    if (raw.isArray()
        || Collection.class.isAssignableFrom(raw)
        || (raw.getModifiers() & Modifier.ABSTRACT) != 0
        || (raw.getDeclaringClass() != null && (raw.getModifiers() & Modifier.STATIC) == 0)) {
      return null;
    }
    var converter = new EnumConverter<>(raw, raw.getEnumConstants());
    bobBson.registerConverter(manifest, converter);
    return converter;
  }
}
