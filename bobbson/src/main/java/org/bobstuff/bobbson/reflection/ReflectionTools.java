package org.bobstuff.bobbson.reflection;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiConsumer;

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
  public static List<ReflectionField> parseBeanFields(Class<?> clazz) throws Exception {
    var beanFields = new ArrayList<ReflectionField>();
    var methods = clazz.getMethods();
    var fields = clazz.getDeclaredFields();

    for (var field : fields) {
      if (Modifier.isTransient(field.getModifiers())) {
        continue;
      }

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
        MethodHandles.Lookup caller = MethodHandles.lookup();
        MethodHandle target =
            caller.findVirtual(
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
                caller,
                "accept",
                MethodType.methodType(BiConsumer.class),
                MethodType.methodType(void.class, Object.class, Object.class),
                target,
                type);
        MethodHandle factory = site.getTarget();
        BiConsumer listenerMethod = null;
        try {
          listenerMethod = (BiConsumer) factory.invoke();
        } catch (Throwable e) {
          throw new RuntimeException(e);
        }
        if (listenerMethod == null) {
          throw new RuntimeException("biconsumer creation failed");
        }
        beanFields.add(new ReflectionField(field, getter, setter, listenerMethod));
      }
    }
    return beanFields;
  }
}
