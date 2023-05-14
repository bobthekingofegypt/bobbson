package org.bobstuff.bobbson.processor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

public class StructInfo {
  public final TypeElement element;
  public final DeclaredType discoveredBy;
  public final String name;
  public final String binaryName;
  public final AnnotationMirror annotation;
  public final Map<String, AttributeResult> attributes;
  public boolean isParameterized;

  public StructInfo(
      TypeElement element,
      DeclaredType discoveredBy,
      String name,
      String binaryName,
      AnnotationMirror annotation,
      Map<String, AttributeResult> attributes) {
    this.element = element;
    this.discoveredBy = discoveredBy;
    this.name = name;
    this.binaryName = binaryName;
    this.annotation = annotation;
    this.attributes = attributes;
    this.isParameterized =
        element.getTypeParameters() != null && !element.getTypeParameters().isEmpty();
  }

  public String getClassName() {
    int dotIndex = binaryName.lastIndexOf('.');
    return binaryName.substring(dotIndex + 1);
  }

  public boolean isEnum() {
    return element.getKind() == ElementKind.ENUM;
  }

  public List<String> getEnumConstants() {
    return element.getEnclosedElements().stream()
        .filter(ee -> ee.getKind() == ElementKind.ENUM_CONSTANT)
        .map(ee -> ee.getSimpleName().toString())
        .collect(Collectors.toList());
  }

  public boolean isParameterized() {
    return isParameterized;
  }

  public String getPackageName() {
    int dotIndex = binaryName.lastIndexOf('.');
    return binaryName.substring(0, dotIndex);
  }

  public String getAttributeReadAllBooleanLogic() {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (var key : attributes.keySet()) {
      if (first) {
        sb.append(key).append("Check");
        first = false;
      } else {
        sb.append(" && ").append(key).append("Check");
      }
    }
    return sb.toString();
  }
}
