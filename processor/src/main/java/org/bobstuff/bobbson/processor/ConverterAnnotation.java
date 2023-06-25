package org.bobstuff.bobbson.processor;

import static org.bobstuff.bobbson.processor.AnnotationUtils.findAnnotationMirror;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ConverterAnnotation {
  private TypeMirror type;

  public ConverterAnnotation(TypeMirror type) {
    this.type = type;
  }

  public TypeMirror getType() {
    return type;
  }

  public static @Nullable ConverterAnnotation parse(
      Element element,
      DeclaredType declaredType,
      Types types,
      Elements elements,
      BobMessager messager) {
    var converter = findAnnotationMirror(element, declaredType, types);
    if (converter == null) {
      return null;
    }

    TypeMirror converterType = null;
    for (ExecutableElement ee : converter.getElementValues().keySet()) {
      if (ee.toString().equals("value()")) {
        String clazz = converter.getElementValues().get(ee).getValue().toString();

        TypeElement typeElement = elements.getTypeElement(clazz);
        DeclaredType type = types.getDeclaredType(typeElement);
        var m = ElementFilter.methodsIn(typeElement.getEnclosedElements());
        for (ExecutableElement e : m) {
          if (e.getSimpleName().toString().equals("readValue")) {
            if (types.isAssignable(e.getReturnType(), element.asType())) {
              converterType = type;
            } else {
              messager.error(
                  "Converter for type ("
                      + element.asType()
                      + ") returns different type "
                      + e.getReturnType());
            }
          }
        }
      }
    }

    if (converterType == null) {
      throw new IllegalStateException("Converter cannot be declared with null type");
    }

    return new ConverterAnnotation(converterType);
  }
}
