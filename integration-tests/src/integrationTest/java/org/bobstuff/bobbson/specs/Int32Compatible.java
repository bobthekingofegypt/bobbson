package org.bobstuff.bobbson.specs;

import org.bobstuff.bobbson.BsonDataProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

public class Int32Compatible {
  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(BsonDataProvider.class)
  public void testInt32Encoding() {}
}
