package org.bobstuff.bobbson.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.bobstuff.bobbson.BobBson;
import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.annotations.BsonAttribute;
import org.bobstuff.bobbson.annotations.BsonConverter;
import org.bobstuff.bobbson.annotations.BsonWriterOptions;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ReflectionField {
  public byte[] nameBytes;
  public int weakHash;
  private String alias;
  private String fieldName;

  private Type type;
  //  private transient Field field;
  private transient Method getter;
  private transient Method setter;
  private transient BiConsumer biConsumerSetter;
  private transient Function getterFunction;
  private transient @Nullable BobBsonConverter converter;

  private boolean writeNull = true;

  public ReflectionField(
      //      Field field,
      String fieldName,
      Type type,
      Method getter,
      Method setter,
      @Nullable BsonAttribute bsonAttribute,
      @Nullable BsonWriterOptions bsonWriterOptions,
      @Nullable BsonConverter customConverter,
      BiConsumer biConsumerSetter,
      Function getterFunction,
      BobBson bobBson) {
    //    this.field = field;
    this.getter = getter;
    this.setter = setter;
    this.biConsumerSetter = biConsumerSetter;
    this.getterFunction = getterFunction;
    this.fieldName = fieldName;
    this.type = type;

    if (bsonWriterOptions != null) {
      this.writeNull = bsonWriterOptions.writeNull();
    }

    if (customConverter != null && customConverter.target() != null) {
      try {
        converter = customConverter.target().getConstructor().newInstance();
      } catch (NoSuchMethodException
          | IllegalAccessException
          | InstantiationException
          | InvocationTargetException e) {
        throw new RuntimeException("failed to create instance of " + customConverter.target());
      }
    } else {
      var t = bobBson.tryFindConverter(type);
      if (t == null) {
        throw new RuntimeException("no converter found for type " + type);
      }
      converter = t;
    }

    if (converter == null) {
      throw new RuntimeException("converter shouldn't be null");
    }
    if (bsonAttribute != null && bsonAttribute.value() != null) {
      nameBytes = bsonAttribute.value().getBytes(StandardCharsets.UTF_8);
      alias = bsonAttribute.value();
    } else {
      nameBytes = fieldName.getBytes(StandardCharsets.UTF_8);
      alias = fieldName;
    }
    weakHash = 0;
    for (byte b : nameBytes) {
      weakHash += b;
    }
  }

  public String getName() {
    return fieldName;
  }

  public String getAlias() {
    return alias;
  }

  public Type getType() {
    return type;
  }

  public @Nullable BobBsonConverter getConverter() {
    return converter;
  }

  public Class<?> getClazz() {
    return type.getClass();
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

  public Function getGetterFunction() {
    return getterFunction;
  }

  public boolean isWriteNull() {
    return writeNull;
  }
}
