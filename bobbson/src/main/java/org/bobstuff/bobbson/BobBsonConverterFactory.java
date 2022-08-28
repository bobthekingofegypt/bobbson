package org.bobstuff.bobbson;

import java.lang.reflect.Type;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface BobBsonConverterFactory<T> {
  @Nullable T tryCreate(Type manifest, BobBson bobBson);
}
