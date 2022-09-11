package org.bobstuff.bobbson.reflection;

import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.bobstuff.bobbson.BobBson;
import org.bobstuff.bobbson.annotations.BsonAttribute;

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

  @SuppressWarnings("PMD.AvoidCatchingThrowable")
  public static <T> List<ReflectionField> parseBeanFields(Class<T> clazz, BobBson bobBson)
      throws Exception {
    var beanFields = new ArrayList<ReflectionField>();
    var methods = clazz.getMethods();
    var fields = clazz.getDeclaredFields();

    for (var field : fields) {
      if (Modifier.isTransient(field.getModifiers())) {
        continue;
      }

      var bsonAttribute = field.getAnnotation(BsonAttribute.class);

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
        var setterLambda = createLambdaFactorySetter(clazz, field, setter, lookup);
        var getterLambda = createLabdaFactoryGetter(clazz, field, getter, lookup);

        beanFields.add(
            new ReflectionField(field, getter, setter, setterLambda, getterLambda, bobBson));
      }
    }
    return beanFields;
  }

  @SuppressWarnings({"unchecked", "PMD.AvoidCatchingThrowable"})
  private static <T, V> Function createLabdaFactoryGetter(
      Class<T> clazz, Field field, Method getter, MethodHandles.Lookup lookup) throws Exception {
    MethodHandle target =
        lookup.findVirtual(clazz, getter.getName(), MethodType.methodType(field.getType()));
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
      Class<T> clazz, Field field, Method setter, MethodHandles.Lookup lookup) throws Exception {
    MethodHandle target =
        lookup.findVirtual(
            clazz,
            setter.getName(),
            MethodType.methodType(void.class, setter.getParameterTypes()[0]));
    MethodType type = target.type();
    if (field.getType().isPrimitive()) {
      var fieldType = map.get(field.getType());
      if (fieldType == null) {
        throw new RuntimeException("can't find primitive type in type map");
      }
      type = type.changeParameterType(1, fieldType);
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
