package org.bobstuff.bobbson.processor;

import java.beans.Introspector;
import java.util.List;
import java.util.Locale;
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
          && ((method.getSimpleName().toString().equals("get" + casedVariableName(fieldName))
                  || method.getSimpleName().toString().equals("is" + casedVariableName(fieldName)))
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

  @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
  private static String casedVariableName(String field) {
    if (field.length() > 1) {
      if (Character.isLowerCase(field.charAt(0)) && Character.isUpperCase(field.charAt(1))) {
        return field;
      }
      return StringUtils.capitalize(field);
    }
    return field.toUpperCase(Locale.getDefault());
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
          && (method.getSimpleName().toString().equals("set" + casedVariableName(fieldName))
              || (fieldName.startsWith("is")
                  && method
                      .getSimpleName()
                      .toString()
                      .equals("set" + casedVariableName(fieldName.substring(2)))))) {
        return method;
      }
    }

    return null;
  }

  @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
  public static @Nullable String extractPropertyNameFromGetter(String methodName) {
    if (methodName.startsWith("get") && methodName.length() > 3) {
      return Introspector.decapitalize(methodName.substring(3));
      //      String propertySection = methodName.substring(3);
      //      if (methodName.length() == 4) {
      //        return propertySection.toLowerCase(Locale.getDefault());
      //      } else {
      //        return propertySection.toUpperCase(Locale.getDefault()).equals(propertySection)
      //            ? propertySection
      //            : Character.toLowerCase(propertySection.charAt(0)) +
      // propertySection.substring(1);
      //      }
    } else if (methodName.startsWith("is") && methodName.length() > 2) {
      return Introspector.decapitalize(methodName.substring(2));
      //      String propertySection = methodName.substring(2);
      //      if (methodName.length() == 3) {
      //        return propertySection.toLowerCase(Locale.getDefault());
      //      } else {
      //        return propertySection.toUpperCase(Locale.getDefault()).equals(propertySection)
      //            ? propertySection
      //            : Character.toLowerCase(propertySection.charAt(0)) +
      // propertySection.substring(1);
      //      }
    }
    return null;
  }
}
