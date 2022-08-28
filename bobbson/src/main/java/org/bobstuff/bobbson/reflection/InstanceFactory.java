package org.bobstuff.bobbson.reflection;

@FunctionalInterface
public interface InstanceFactory<T> {
  T instance() throws Exception;
}
