package org.bobstuff.bobbson.converters;

import java.util.List;
import java.util.Map;
import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.BobBsonConverterFactory;

public interface BobBsonConvertersModule {
  Map<Class<?>, BobBsonConverter> converters();

  List<BobBsonConverterFactory> factories();
}
