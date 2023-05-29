package org.bobstuff.bobbson;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bobstuff.bobbson.converters.*;
import org.bobstuff.bobbson.models.BobBsonBinary;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BobBson {
  private final ConcurrentMap<Type, BobBsonConverter> converters;
  private final List<BobBsonConverterFactory<BobBsonConverter>> converterFactories;
  private final ExternalConverterLookup externalConverterLookup;
  private final BobBsonConfig config;

  public BobBson() {
    this(new BobBsonConfig(true));
  }

  public BobBson(BobBsonConfig config) {
    this.config = config;
    converterFactories = new CopyOnWriteArrayList<>();

    converters = new ConcurrentHashMap<>();
    converters.put(String.class, new StringBsonConverter());
    converters.put(Integer.class, new IntegerBsonConverter());
    converters.put(int.class, new IntegerBsonConverter());
    converters.put(double.class, new DoubleBsonConverter());
    converters.put(Double.class, new DoubleBsonConverter());
    converters.put(boolean.class, new BooleanBsonConverter());
    converters.put(Boolean.class, new BooleanBsonConverter());
    converters.put(Long.class, new LongBsonConverter());
    converters.put(long.class, new LongBsonConverter());
    converters.put(BobBsonBinary.class, new BobBsonBinaryBsonConverter());

    var classLoader = Thread.currentThread().getContextClassLoader();
    if (classLoader == null) {
      throw new IllegalStateException("class loader cannot be null");
    }
    var classLoaders = List.of(classLoader);
    externalConverterLookup = new ExternalConverterLookup(classLoaders);
  }

  @SuppressWarnings("unchecked")
  public @Nullable <T> BobBsonConverter<T> tryFindConverter(Class<T> clazz) {
    return (BobBsonConverter<T>) tryFindConverter((Type) clazz);
  }

  public @Nullable BobBsonConverter<?> tryFindConverter(final Type manifest) {
    var converter = converters.get(manifest);
    if (converter != null) {
      return converter;
    }

    if (manifest instanceof Class<?> && config.isAllowExternalLookup()) {
      var found = externalConverterLookup.lookupFromClasspath((Class<?>) manifest, this);
      if (found) {
        converter = converters.get(manifest);
        if (converter != null) {
          return converter;
        }
      }
    }

    // look up reflection based solutions
    for (var factory : converterFactories) {
      converter = factory.tryCreate(manifest, this);
      if (converter != null) {
        converters.put(manifest, converter);
        return converter;
      }
    }

    throw new RuntimeException("Failed to find converter for type " + manifest);
  }

  public void registerConverter(Type type, BobBsonConverter converter) {
    converters.put(type, converter);
  }

  @SuppressWarnings("unchecked")
  public void registerFactory(BobBsonConverterFactory factory) {
    converterFactories.add(factory);
  }

  @SuppressWarnings("unchecked")
  public <T> @Nullable T deserialise(Class<T> manifest, BsonReader reader) throws Exception {
    if (manifest == null) {
      throw new IllegalArgumentException("manifest can't be null");
    }

    BobBsonConverter<T> converter = tryFindConverter(manifest);
    if (converter == null) {
      throw new IllegalStateException("no converter found for type " + manifest);
    }
    return converter.read(reader);
  }

  public <T> void serialise(T obj, Class<T> manifest, BsonWriter writer) throws Exception {
    BobBsonConverter<T> converter = tryFindConverter(manifest);
    if (converter == null) {
      throw new IllegalStateException("no converter found for type " + manifest);
    }
    if (obj == null) {
      // TODO this isn't true, I should handle nullable
      throw new IllegalArgumentException("Can't serialise null");
    }
    converter.write(writer, obj);
  }
}
