package org.bobstuff.bobbson.reflection;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.bobstuff.bobbson.BobBson;
import org.bobstuff.bobbson.annotations.BsonAttribute;
import org.bobstuff.bobbson.annotations.BsonConverter;
import org.bobstuff.bobbson.annotations.BsonWriterOptions;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Helpful tools for the reflection converters to use to inspect beans and fields */
public class ReflectionTools {
  public static final Map<Class<?>, Class<?>> map = new HashMap<>();

  static {
    map.put(boolean.class, Boolean.class);
    map.put(byte.class, Byte.class);
    map.put(short.class, Short.class);
    map.put(char.class, Character.class);
    map.put(int.class, Integer.class);
    map.put(long.class, Long.class);
    map.put(float.class, Float.class);
    map.put(double.class, Double.class);
  }

  /**
   * Parse a list of all fields in the requested bean that can be accessed by a bson converter to
   * read/write the classes state.
   *
   * @param clazz bean to be scanned
   * @param bobBson used to find pre-registered converters to fields, if none found they will be
   *     requested again at runtime
   * @return list of scanned fields
   * @param <T> type of bean to be scanned
   * @throws Exception if problem creating reflection getter/setters
   */
  @SuppressWarnings({"unchecked", "PMD.AvoidCatchingThrowable"})
  public static <T> List<ReflectionField> parseBeanFields(Class<T> clazz, BobBson bobBson)
      throws Exception {
    var beanFields = new ArrayList<ReflectionField>();
    var methods = clazz.getMethods();
    var fields = clazz.getDeclaredFields();

    // first check fields with getters and setters
    for (var field : fields) {
      if (Modifier.isTransient(field.getModifiers())) {
        continue;
      }

      var bsonAttribute = field.getAnnotation(BsonAttribute.class);
      var bsonWriterOptions = field.getAnnotation(BsonWriterOptions.class);
      var customConverter = field.getAnnotation(BsonConverter.class);

      var name = field.getName();
      var capitalisedName =
          name.substring(0, 1).toUpperCase(Locale.getDefault()) + name.substring(1);
      Method getter = null;
      Method setter = null;
      for (Method method : methods) {
        // TODO check public and return types etc
        if (method.getName().equals("get" + capitalisedName)
            || (method.getName().equals("is" + capitalisedName))) {
          getter = method;
        }
        if (method.getName().equals("set" + capitalisedName)) {
          setter = method;
        }

        if (getter != null && setter != null) {
          break;
        }
      }

      if (getter != null && setter != null) {
        var lookup = MethodHandles.lookup();
        var setterLambda = createLambdaFactorySetter(clazz, field.getType(), setter, lookup);
        var getterLambda = createLabdaFactoryGetter(clazz, field.getType(), getter, lookup);

        beanFields.add(
            new ReflectionField(
                name,
                field.getGenericType(),
                bsonAttribute,
                bsonWriterOptions,
                customConverter,
                setterLambda,
                getterLambda,
                bobBson));
      }
    }
    // now check getters with annotation but no field
    for (var method : methods) {
      var bsonAttribute = method.getAnnotation(BsonAttribute.class);
      if (bsonAttribute == null) {
        continue;
      }
      var bsonWriterOptions = method.getAnnotation(BsonWriterOptions.class);
      var customConverter = method.getAnnotation(BsonConverter.class);
      var methodName = method.getName();
      var fieldName = extractNameFromMethod(methodName);
      if (fieldName == null) {
        continue;
      }

      var capitalisedName =
          fieldName.substring(0, 1).toUpperCase(Locale.getDefault()) + fieldName.substring(1);
      Method setter = null;
      for (Method otherMethod : methods) {
        // TODO check public and return types etc
        if (otherMethod.getName().equals("set" + capitalisedName)) {
          setter = otherMethod;
        }

        if (setter != null) {
          break;
        }
      }
      if (setter != null) {
        var fieldType = method.getGenericReturnType();
        var lookup = MethodHandles.lookup();
        var setterLambda = createLambdaFactorySetter(clazz, method.getReturnType(), setter, lookup);
        var getterLambda = createLabdaFactoryGetter(clazz, method.getReturnType(), method, lookup);

        beanFields.add(
            new ReflectionField(
                fieldName,
                fieldType,
                bsonAttribute,
                bsonWriterOptions,
                customConverter,
                setterLambda,
                getterLambda,
                bobBson));
      }
    }
    return beanFields;
  }

  /**
   * Extract a field name from its getter method name.
   *
   * <p>ex - getBanana would return banana, or isOld would return old
   *
   * @param methodName name to parse
   * @return lowercase name
   */
  @SuppressWarnings("PMD.UnnecessaryCaseChange")
  private static @Nullable String extractNameFromMethod(String methodName) {
    int getPlusOneLetter = 4;
    int isPlusOneLetter = 3;

    if (methodName.startsWith("get") && methodName.length() > 3) {
      String propertySection = methodName.substring(3);
      if (methodName.length() == getPlusOneLetter) {
        return propertySection.toLowerCase(Locale.getDefault());
      } else {
        // handle values like getDNA as apposed to getDna
        return propertySection.toUpperCase(Locale.getDefault()).equals(propertySection)
            ? propertySection
            : Character.toLowerCase(propertySection.charAt(0)) + propertySection.substring(1);
      }
    } else if (methodName.startsWith("is") && methodName.length() > 2) {
      String propertySection = methodName.substring(2);
      if (methodName.length() == isPlusOneLetter) {
        return propertySection.toLowerCase(Locale.getDefault());
      } else {
        return propertySection.toUpperCase(Locale.getDefault()).equals(propertySection)
            ? propertySection
            : Character.toLowerCase(propertySection.charAt(0)) + propertySection.substring(1);
      }
    }
    return null;
  }

  @SuppressWarnings({"unchecked", "PMD.AvoidCatchingThrowable"})
  private static <T, V> Function createLabdaFactoryGetter(
      Class<T> clazz, Class<?> fieldType, Method getter, MethodHandles.Lookup lookup)
      throws Exception {
    MethodHandle target =
        lookup.findVirtual(clazz, getter.getName(), MethodType.methodType(fieldType));
    MethodType type = target.type();
    CallSite site =
        LambdaMetafactory.metafactory(
            lookup,
            "apply",
            MethodType.methodType(Function.class),
            MethodType.methodType(Object.class, Object.class),
            target,
            type);
    Function<T, V> getterFunction;
    try {
      getterFunction = (Function<T, V>) site.getTarget().invokeExact();
    } catch (Throwable e) {
      throw new RuntimeException("failed to invoke exact on callsite for getter", e);
    }
    if (getterFunction == null) {
      throw new RuntimeException("oh dear");
    }

    return getterFunction;
  }

  @SuppressWarnings({"unchecked", "PMD.AvoidCatchingThrowable"})
  private static <T, V> BiConsumer<T, V> createLambdaFactorySetter(
      Class<T> clazz, Class<?> fieldType, Method setter, MethodHandles.Lookup lookup)
      throws Exception {
    MethodHandle target =
        lookup.findVirtual(
            clazz,
            setter.getName(),
            MethodType.methodType(void.class, setter.getParameterTypes()[0]));
    MethodType type = target.type();
    if (fieldType.isPrimitive()) {
      var primitiveFieldType = map.get(fieldType);
      if (primitiveFieldType == null) {
        throw new RuntimeException("can't find primitive type in type map");
      }
      type = type.changeParameterType(1, primitiveFieldType);
    }

    CallSite site =
        LambdaMetafactory.metafactory(
            lookup,
            "accept",
            MethodType.methodType(BiConsumer.class),
            MethodType.methodType(void.class, Object.class, Object.class),
            target,
            type);
    MethodHandle factory = site.getTarget();
    BiConsumer<T, V> listenerMethod;
    try {
      listenerMethod = (BiConsumer<T, V>) factory.invoke();
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
    if (listenerMethod == null) {
      throw new RuntimeException("biconsumer creation failed for setter method");
    }

    return listenerMethod;
  }
}
