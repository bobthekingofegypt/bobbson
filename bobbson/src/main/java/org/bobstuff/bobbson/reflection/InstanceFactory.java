package org.bobstuff.bobbson.reflection;

/**
 * Factory that returns instances of the generic type.
 *
 * <p>Allows passing of instances creators to other converter chains to aid in reflection creation
 * code
 *
 * @param <T> type of instances created
 */
@FunctionalInterface
public interface InstanceFactory<T> {
  /**
   * @return an instance of type T
   * @throws Exception if instantiation fails
   */
  T instance() throws Exception;
}
