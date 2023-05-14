package org.bobstuff.bobbson.reflection;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import org.bobstuff.bobbson.BobBson;
import org.bobstuff.bobbson.BobBsonConverterFactory;
import org.checkerframework.checker.nullness.qual.Nullable;

public class EnumConverterFactory implements BobBsonConverterFactory<EnumConverter> {
  @Override
  @Nullable
  @SuppressWarnings({"PMD.AvoidLiteralsInIfCondition", "unchecked"})
  public EnumConverter tryCreate(Type manifest, BobBson bobBson) {
    if (manifest instanceof Class<?> && ((Class<?>) manifest).isEnum()) {
      return analyze(manifest, (Class<Enum>) manifest, bobBson);
    }
    // TODO support paramaterized enums
    return null;
  }

  @Nullable
  @SuppressWarnings({"argument"})
  private static EnumConverter analyze(
      final Type manifest, final Class<Enum> raw, final BobBson bobBson) {
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
