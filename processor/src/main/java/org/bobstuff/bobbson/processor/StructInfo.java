package org.bobstuff.bobbson.processor;

import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

public class StructInfo {
  public final TypeElement element;
  public final DeclaredType discoveredBy;
  public final String name;
  public final String binaryName;
  public final AnnotationMirror annotation;
  public final Map<String, AttributeResult> attributes;

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
  }

  public String getClassName() {
    int dotIndex = binaryName.lastIndexOf('.');
    return binaryName.substring(dotIndex + 1);
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
