package org.bobstuff.bobbson;

import java.lang.reflect.Type;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Factories are used to add more extended dynamic functionality to converter retrieval.
 *
 * <p>Factories are registered with BobBson and asked in order of registration to provide a
 * converter implementation for the given type if non of the build in converters are capable. This
 * allows the implementation of reflection converters, or non-reusable converters for example.
 *
 * @param <T>
 */
public interface BobBsonConverterFactory<T> {
  /**
   * Return a converter for the type requested. If null is returned BobBson will carry on trying
   * other factories to find a converter.
   *
   * @param manifest the type for the converter
   * @param bobBson the active bobbson instance
   * @return a compatible converter or null
   */
  @Nullable T tryCreate(Type manifest, BobBson bobBson);
}
