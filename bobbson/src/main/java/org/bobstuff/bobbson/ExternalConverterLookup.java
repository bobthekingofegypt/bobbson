package org.bobstuff.bobbson;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ExternalConverterLookup {
  private final Set<String> lookedUpClasses = new HashSet<String>();
  private final ClassLoader[] classLoaders;

  ExternalConverterLookup(Collection<ClassLoader> classLoaders) {
    this.classLoaders = classLoaders.toArray(new ClassLoader[0]);
  }

  public synchronized boolean lookupFromClasspath(Class<?> clazz, BobBson bobBson) {
    final String className = clazz.getName();
    if (!lookedUpClasses.add(className)) return true;
    String[] converterClassNames = resolveExternalConverterClassNames(className);
    for (ClassLoader cl : classLoaders) {
      for (String ccn : converterClassNames) {
        try {
          Class<?> converterClass = cl.loadClass(ccn);
          if (!BobBsonConverterRegister.class.isAssignableFrom(converterClass)) continue;
          BobBsonConverterRegister register =
              (BobBsonConverterRegister) converterClass.getConstructor().newInstance();
          register.register(bobBson);
          return true;
        } catch (ClassNotFoundException
            | InstantiationException
            | IllegalAccessException
            | InvocationTargetException
            | NoSuchMethodException ignored) {
        }
      }
    }
    return false;
  }

  private String[] resolveExternalConverterClassNames(final String fullClassName) {
    int dotIndex = fullClassName.lastIndexOf('.');
    if (dotIndex == -1) {
      return new String[] {String.format("_%s_BobBsonConverterRegister", fullClassName)};
    }
    String packageName = fullClassName.substring(0, dotIndex);
    String className = fullClassName.substring(dotIndex + 1);
    return new String[] {String.format("%s._%s_BobBsonConverterRegister", packageName, className)};
  }
}
