package org.bobstuff.bobbson.processor;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Types;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AnnotationUtils {
  public static @Nullable AnnotationMirror findAnnotationMirror(
      Element element, DeclaredType declaredType, Types types) {
    for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
      if (types.isSameType(mirror.getAnnotationType(), declaredType)) {
        return mirror;
      }
    }

    return null;
  }

  public static @Nullable AnnotationValue findAnnotationValue(
      AnnotationMirror annotationMirror, String key) {
    for (ExecutableElement ee : annotationMirror.getElementValues().keySet()) {
      if (ee.toString().equals(key + "()")) {
        return annotationMirror.getElementValues().get(ee);
      }
    }

    return null;
  }
}
