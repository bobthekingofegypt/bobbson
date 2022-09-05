package org.bobstuff.bobbson.reflection;

import java.lang.invoke.*;
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
  public static List<ReflectionField> parseBeanFields(Class<?> clazz, BobBson bobBson) throws Exception {
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
        MethodHandle target2 = null;

        if (field.getType().isPrimitive()) {
          var fieldType = map.get(field.getType());
          if (fieldType == null) {
            throw new RuntimeException("can't find primitive type in type map");
          }
          target2 =
              caller.findVirtual(clazz, getter.getName(), MethodType.methodType(field.getType()));
        } else {
          target2 =
              caller.findVirtual(clazz, getter.getName(), MethodType.methodType(field.getType()));
        }
        MethodType type2 = target2.type();
        //        if (field.getType().isPrimitive()) {
        //          var fieldType = map.get(field.getType());
        //          if (fieldType == null) {
        //            throw new RuntimeException("can't find primitive type in type map");
        //          }
        //          type2 = type2.changeParameterType(0, fieldType);
        //        }
        CallSite site2 =
            LambdaMetafactory.metafactory(
                caller,
                "apply",
                MethodType.methodType(Function.class),
                MethodType.methodType(Object.class, Object.class),
                target2,
                type2);
        Function getterFunction = null;
        try {
          getterFunction = (Function) site2.getTarget().invokeExact();
        } catch (Throwable e) {
          e.printStackTrace();
        }
        if (getterFunction == null) {
          throw new RuntimeException("oh dear");
        }
        beanFields.add(new ReflectionField(field, getter, setter, listenerMethod, getterFunction, bobBson));
      }
    }
    return beanFields;
  }
}
