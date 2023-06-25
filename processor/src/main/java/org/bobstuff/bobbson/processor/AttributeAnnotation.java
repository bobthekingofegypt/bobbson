package org.bobstuff.bobbson.processor;

import static org.bobstuff.bobbson.processor.AnnotationUtils.findAnnotationMirror;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.bobstuff.bobbson.annotations.BsonAttribute;

public class AttributeAnnotation {
  private boolean ignore;
  private int order;
  private String alias;

  public AttributeAnnotation(boolean ignore, int order, String alias) {
    this.ignore = ignore;
    this.order = order;
    this.alias = alias;
  }

  public boolean isIgnore() {
    return ignore;
  }

  public int getOrder() {
    return order;
  }

  public String getAlias() {
    return alias;
  }

  public static AttributeAnnotation parse(
      Element element,
      DeclaredType declaredType,
      String name,
      Types types,
      Elements elements,
      BobMessager messager) {
    var attributeAnnotation = findAnnotationMirror(element, declaredType, types);
    if (attributeAnnotation == null) {
      return new AttributeAnnotation(false, Integer.MAX_VALUE, name);
    }

    var ignoreValue = AnnotationUtils.findAnnotationValue(attributeAnnotation, "ignore");
    var ignore = ignoreValue == null ? false : (boolean) ignoreValue.getValue();
    var orderValue = AnnotationUtils.findAnnotationValue(attributeAnnotation, "order");
    var order = orderValue == null ? Integer.MAX_VALUE : (int) orderValue.getValue();
    var aliasValue = AnnotationUtils.findAnnotationValue(attributeAnnotation, "value");
    var aliasValueString =
        aliasValue == null ? BsonAttribute.DEFAULT_NON_VALID_ALIAS : (String) aliasValue.getValue();
    var alias =
        BsonAttribute.DEFAULT_NON_VALID_ALIAS.equals(aliasValueString) ? name : aliasValueString;

    return new AttributeAnnotation(ignore, order, alias);
  }
}
