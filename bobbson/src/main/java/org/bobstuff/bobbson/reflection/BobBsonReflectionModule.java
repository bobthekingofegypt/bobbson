package org.bobstuff.bobbson.reflection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BobBsonConverterFactory;
import org.bobstuff.bobbson.converters.BobBsonConvertersModule;

public class BobBsonReflectionModule implements BobBsonConvertersModule {
  private static List<BobBsonConverterFactory> registeredFactories;

  static {
    registeredFactories = new ArrayList<>();
    registeredFactories.add(new EnumConverterFactory<>());
    registeredFactories.add(new CollectionConverterFactory<>());
    registeredFactories.add(new MapConverterFactory<>());
    registeredFactories.add(new ObjectConverterFactory<>());
  }

  @Override
  public Map<Class<?>, BobBsonConverter> converters() {
    return Collections.emptyMap();
  }

  @Override
  public List<BobBsonConverterFactory> factories() {
    return registeredFactories;
  }
}
