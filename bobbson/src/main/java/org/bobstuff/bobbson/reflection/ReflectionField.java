package org.bobstuff.bobbson.reflection;

import java.lang.reflect.InvocationTargetException;
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

/**
 * Class to hold the break down of a field found in a bean. This includes information about
 * getters/setters, name, alias custom converters etc.
 *
 * @param <Model> the model the field is on
 * @param <Field> the field in type
 */
public class ReflectionField<Model, Field> {
  public byte[] nameBytes;
  public int weakHash;

  private int order;
  private final String alias;
  private final String fieldName;
  private final Type type;
  private final transient BiConsumer<Model, Field> biConsumerSetter;
  private final transient Function<Model, Field> getterFunction;
  private final transient @Nullable BobBsonConverter<Field> converter;
  private boolean writeNull = true;

  @SuppressWarnings("unchecked")
  public ReflectionField(
      String fieldName,
      Type type,
      @Nullable BsonAttribute bsonAttribute,
      @Nullable BsonWriterOptions bsonWriterOptions,
      @Nullable BsonConverter customConverter,
      BiConsumer<Model, Field> biConsumerSetter,
      Function<Model, Field> getterFunction,
      BobBson bobBson) {
    this.biConsumerSetter = biConsumerSetter;
    this.getterFunction = getterFunction;
    this.fieldName = fieldName;
    this.type = type;

    if (bsonWriterOptions != null) {
      this.writeNull = bsonWriterOptions.writeNull();
    }

    if (customConverter != null && customConverter.value() != null) {
      try {
        converter =
            (BobBsonConverter<Field>) customConverter.value().getConstructor().newInstance();
      } catch (NoSuchMethodException
          | IllegalAccessException
          | InstantiationException
          | InvocationTargetException e) {
        throw new RuntimeException("failed to create instance of " + customConverter.value());
      }
    } else {
      converter = (BobBsonConverter<Field>) bobBson.tryFindConverter(type);
    }

    if (bsonAttribute != null) {
      order = bsonAttribute.order();
    }

    if (bsonAttribute != null && !BsonAttribute.DEFAULT_NON_VALID_ALIAS.equals(bsonAttribute.value())) {
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

  public int getOrder() {
    return order;
  }

  public Type getType() {
    return type;
  }

  public @Nullable BobBsonConverter<Field> getConverter() {
    return converter;
  }

  public Class<?> getClazz() {
    return (Class<?>) type;
  }

  public BiConsumer<Model, Field> getBiConsumerSetter() {
    return biConsumerSetter;
  }

  public Function<Model, Field> getGetterFunction() {
    return getterFunction;
  }

  public boolean isWriteNull() {
    return writeNull;
  }
}
