package org.bobstuff.bobbson.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;

public class ReflectionField {
  public byte[] nameBytes;
  public int weakHash;
  private transient Field field;
  private transient Method getter;
  private transient Method setter;
  private transient BiConsumer biConsumerSetter;

  public ReflectionField(Field field, Method getter, Method setter, BiConsumer biConsumerSetter) {
    this.field = field;
    this.getter = getter;
    this.setter = setter;
    this.biConsumerSetter = biConsumerSetter;

    nameBytes = field.getName().getBytes(StandardCharsets.UTF_8);
    weakHash = 0;
    for (byte b : nameBytes) {
      weakHash += b;
    }
  }

  public String getName() {
    return field.getName();
  }

  public Type getType() {
    return field.getGenericType();
  }

  public Class<?> getClazz() {
    return field.getGenericType().getClass();
  }

  public Method getGetter() {
    return getter;
  }

  public Method getSetter() {
    return setter;
  }

  public BiConsumer getBiConsumerSetter() {
    return biConsumerSetter;
  }
}
