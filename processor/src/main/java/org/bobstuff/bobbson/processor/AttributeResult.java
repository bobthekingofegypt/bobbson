package org.bobstuff.bobbson.processor;

import com.squareup.javapoet.ClassName;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AttributeResult {
  public static final String CONVERTER_PRE = "converter_";
  public static final String ESCAPED_DOT = "\\.";

  public static final int SINGLE_GENERIC_PARAMATER = 1;
  public static final int TWO_GENERIC_PARAMETERS = 2;
  public static final String ARRAY_TEXT = "_array_";
  public final String name;
  public final ExecutableElement readMethod;
  public final @Nullable ExecutableElement writeMethod;
  public final TypeMirror type;
  public final FieldType fieldType;
  public final AttributeAnnotation attributeAnnotation;
  public final @Nullable ConverterAnnotation converterAnnotation;
  public final @MonotonicNonNull AnnotationMirror writerOptionsAnnotation;
  public final String converterFieldName;
  public final String param;

  public AttributeResult(
      String name,
      ExecutableElement readMethod,
      @Nullable ExecutableElement writeMethod,
      TypeMirror type,
      FieldType fieldType,
      AttributeAnnotation attributeAnnotation,
      @Nullable ConverterAnnotation converterAnnotation,
      @Nullable AnnotationMirror writerOptionsAnnotation) {
    this.name = name;
    this.readMethod = readMethod;
    this.writeMethod = writeMethod;
    this.type = type;
    this.fieldType = fieldType;
    this.attributeAnnotation = attributeAnnotation;
    this.converterAnnotation = converterAnnotation;
    this.writerOptionsAnnotation = writerOptionsAnnotation;

    String fieldName1 = null;
    String param1 = null;

    if (fieldType == FieldType.LIST || fieldType == FieldType.MAP || fieldType == FieldType.SET) {
      var dclt = (DeclaredType) type;
      var typeArguments = dclt.getTypeArguments();
      if (fieldType == FieldType.LIST) {
        if (typeArguments.size() == AttributeResult.SINGLE_GENERIC_PARAMATER) {
          fieldName1 =
              CONVERTER_PRE
                  + typeArguments
                      .get(0)
                      .toString()
                      .replaceAll(ESCAPED_DOT, "_")
                      .replace("[]", ARRAY_TEXT);
          param1 = typeArguments.get(0).toString();
        }
      } else if (fieldType == FieldType.MAP) {
        if (typeArguments.size() == AttributeResult.TWO_GENERIC_PARAMETERS) {
          fieldName1 =
              CONVERTER_PRE
                  + typeArguments
                      .get(1)
                      .toString()
                      .replaceAll(ESCAPED_DOT, "_")
                      .replace("[]", ARRAY_TEXT);
          param1 = typeArguments.get(1).toString();
        }
      } else if (fieldType == FieldType.SET) {
        if (typeArguments.size() == AttributeResult.SINGLE_GENERIC_PARAMATER) {
          fieldName1 =
              CONVERTER_PRE
                  + typeArguments
                      .get(0)
                      .toString()
                      .replaceAll(ESCAPED_DOT, "_")
                      .replace("[]", ARRAY_TEXT);
          param1 = typeArguments.get(0).toString();
        }
      }
    } else {
      fieldName1 =
          CONVERTER_PRE
              + ClassName.get(type)
                  .toString()
                  .replaceAll(ESCAPED_DOT, "_")
                  .replace("[]", ARRAY_TEXT);
      param1 = "";
    }

    if (fieldName1 == null || param1 == null) {
      throw new IllegalStateException("converterfieldname is null for attribute name: " + name);
    }

    this.param = param1;
    if (converterAnnotation != null) {
      this.converterFieldName = CONVERTER_PRE + name;
    } else {
      this.converterFieldName = fieldName1;
    }
  }

  public String getConverterFieldName() {
    return converterFieldName;
  }

  public String getParam() {
    return param;
  }

  public String getConverterName() {
    return CONVERTER_PRE + name;
  }

  public String getName() {
    return name;
  }

  public ExecutableElement getReadMethod() {
    return readMethod;
  }

  public ExecutableElement getWriteMethod() {
    // TODO fix this later, just stopping checkerframework
    var wm = writeMethod;
    if (wm == null) {
      throw new IllegalStateException("no write method here");
    }
    return wm;
  }

  public boolean isMap() {
    return fieldType == FieldType.MAP;
  }

  public TypeMirror getType() {
    return type;
  }

  public TypeMirror getDeclaredType() {
    if ((isList() || isSet()) && converterAnnotation == null) {
      DeclaredType dclt = (DeclaredType) type;
      if (dclt.getTypeArguments().size() == SINGLE_GENERIC_PARAMATER) {
        return dclt.getTypeArguments().get(0);
      }
    } else if (isMap() && converterAnnotation == null) {
      DeclaredType dclt = (DeclaredType) type;
      if (dclt.getTypeArguments().size() == TWO_GENERIC_PARAMETERS) {
        return dclt.getTypeArguments().get(1);
      }
    }
    return getType();
  }

  public @Nullable TypeMirror getConverterType() {
    return converterAnnotation == null ? null : converterAnnotation.getType();
  }

  public boolean isPrimitive() {
    return type.getKind().isPrimitive();
  }

  public String getAliasName() {
    return attributeAnnotation.getAlias();
  }

  public boolean writerOptionWriteNull() {
    if (writerOptionsAnnotation == null) {
      return true;
    }

    for (ExecutableElement ee : writerOptionsAnnotation.getElementValues().keySet()) {
      if (ee.toString().equals("writeNull()")) {
        return (boolean) writerOptionsAnnotation.getElementValues().get(ee).getValue();
      }
    }

    return true;
  }

  public int getOrder() {
    return attributeAnnotation.getOrder();
  }

  public boolean isList() {
    return fieldType == FieldType.LIST;
  }

  public boolean isSet() {
    return fieldType == FieldType.SET;
  }

  @Override
  public String toString() {
    return "AttributeResult{"
        + "name='"
        + name
        + '\''
        + ", readMethod="
        + readMethod
        + ", writeMethod="
        + writeMethod
        //        + ", field="
        //        + field
        + ", type="
        + type
        + '}';
  }
}
