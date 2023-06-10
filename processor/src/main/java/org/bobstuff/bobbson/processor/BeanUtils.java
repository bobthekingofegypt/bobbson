package org.bobstuff.bobbson.processor;

import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BeanUtils {
  public static @Nullable ExecutableElement findGetter(
      List<ExecutableElement> methods, String fieldName, TypeMirror type, BobMessager messager) {
    for (var method : methods) {
      var modifiers = method.getModifiers();
      if (modifiers.contains(Modifier.PUBLIC)
          && !modifiers.contains(Modifier.STATIC)
          && !modifiers.contains(Modifier.TRANSIENT)
          && !modifiers.contains(Modifier.ABSTRACT)
          && method.getReturnType().getKind().equals(type.getKind())
          && ((method.getSimpleName().toString().equals("get" + StringUtils.capitalize(fieldName))
                  || method
                      .getSimpleName()
                      .toString()
                      .equals("is" + StringUtils.capitalize(fieldName)))
              || (fieldName.startsWith("is")
                  && method.getSimpleName().toString().equals(fieldName)))) {
        return method;
      }
    }

    messager.debug(type.getKind().toString());
    for (var method : methods) {
      messager.debug(method.getSimpleName() + " - " + method.getReturnType().getKind().toString());
    }

    return null;
  }

  public static @Nullable ExecutableElement findSetter(
      List<ExecutableElement> methods, String fieldName, TypeMirror type) {
    for (var method : methods) {
      var modifiers = method.getModifiers();
      if (modifiers.contains(Modifier.PUBLIC)
          && !modifiers.contains(Modifier.STATIC)
          && !modifiers.contains(Modifier.TRANSIENT)
          && !modifiers.contains(Modifier.ABSTRACT)
          && method.getReturnType().getKind() == TypeKind.VOID
          && (method.getSimpleName().toString().equals("set" + StringUtils.capitalize(fieldName))
              || (fieldName.startsWith("is")
                  && method
                      .getSimpleName()
                      .toString()
                      .equals("set" + StringUtils.capitalize(fieldName.substring(2)))))) {
        return method;
      }
    }

    return null;
  }
}
