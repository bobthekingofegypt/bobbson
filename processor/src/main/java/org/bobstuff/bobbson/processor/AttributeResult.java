package org.bobstuff.bobbson.processor;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AttributeResult {
  public static final String CONVERTER_PRE = "converter_";
  public static final String ESCAPED_DOT = "\\.";

  public static final int SINGLE_GENERIC_PARAMATER = 1;
  public static final int TWO_GENERIC_PARAMETERS = 2;
  public final String name;
  public final ExecutableElement readMethod;
  public final ExecutableElement writeMethod;
  public final VariableElement field;
  public final TypeMirror type;
  public final boolean list;
  public final boolean set;
  public final boolean map;
  public final @Nullable AnnotationMirror annotation;
  public final @Nullable AnnotationMirror converter;
  public final @MonotonicNonNull AnnotationMirror writerOptions;
  public final @Nullable TypeMirror converterType;
  public final String converterFieldName;
  public final String param;

  public AttributeResult(
      String name,
      ExecutableElement readMethod,
      ExecutableElement writeMethod,
      VariableElement field,
      TypeMirror type,
      boolean list,
      boolean set,
      boolean map,
      @Nullable AnnotationMirror annotation,
      @Nullable AnnotationMirror converter,
      @Nullable AnnotationMirror writerOptions,
      @Nullable TypeMirror converterType) {
    this.name = name;
    this.readMethod = readMethod;
    this.writeMethod = writeMethod;
    this.field = field;
    this.type = type;
    this.list = list;
    this.set = set;
    this.map = map;
    this.annotation = annotation;
    this.converter = converter;
    this.writerOptions = writerOptions;
    this.converterType = converterType;

    String fieldName1 = null;
    String param1 = null;

    if (list || map || set) {
      var dclt = (DeclaredType) type;
      var typeArguments = dclt.getTypeArguments();
      if (list) {
        if (typeArguments.size() == AttributeResult.SINGLE_GENERIC_PARAMATER) {
          fieldName1 = CONVERTER_PRE + typeArguments.get(0).toString().replaceAll(ESCAPED_DOT, "_");
          param1 = typeArguments.get(0).toString();
        }
      } else if (map) {
        if (typeArguments.size() == AttributeResult.TWO_GENERIC_PARAMETERS) {
          fieldName1 = CONVERTER_PRE + typeArguments.get(1).toString().replaceAll(ESCAPED_DOT, "_");
          param1 = typeArguments.get(1).toString();
        }
      } else if (set) {
        if (typeArguments.size() == AttributeResult.SINGLE_GENERIC_PARAMATER) {
          fieldName1 = CONVERTER_PRE + typeArguments.get(0).toString().replaceAll(ESCAPED_DOT, "_");
          param1 = typeArguments.get(0).toString();
        }
      }
    } else {
      fieldName1 = CONVERTER_PRE + type.toString().replaceAll(ESCAPED_DOT, "_");
      param1 = "";
    }

    if (fieldName1 == null || param1 == null) {
      throw new IllegalStateException("converterfieldname is null for attribute name: " + name);
    }

    this.param = param1;
    if (converter != null) {
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
    return writeMethod;
  }

  public VariableElement getField() {
    return field;
  }

  public boolean isMap() {
    return map;
  }

  public TypeMirror getType() {
    return type;
  }

  public TypeMirror getDeclaredType() {
    if ((isList() || isSet()) && converterType == null) {
      DeclaredType dclt = (DeclaredType) type;
      if (dclt.getTypeArguments().size() == SINGLE_GENERIC_PARAMATER) {
        return dclt.getTypeArguments().get(0);
      }
    } else if (isMap() && converterType == null) {
      DeclaredType dclt = (DeclaredType) type;
      if (dclt.getTypeArguments().size() == TWO_GENERIC_PARAMETERS) {
        return dclt.getTypeArguments().get(1);
      }
    }
    return getType();
  }

  public @Nullable AnnotationMirror getAnnotation() {
    return annotation;
  }

  public @Nullable AnnotationMirror getConverter() {
    return converter;
  }

  public @Nullable TypeMirror getConverterType() {
    return converterType;
  }

  public String getAliasName() {
    if (annotation == null) {
      return name;
    }

    for (ExecutableElement ee : annotation.getElementValues().keySet()) {
      if (ee.toString().equals("value()")) {
        return annotation.getElementValues().get(ee).getValue().toString();
      }
    }

    return name;
  }

  public boolean writerOptionWriteNull() {
    if (writerOptions == null) {
      return true;
    }

    for (ExecutableElement ee : writerOptions.getElementValues().keySet()) {
      if (ee.toString().equals("writeNull()")) {
        return (boolean) writerOptions.getElementValues().get(ee).getValue();
      }
    }

    return true;
  }

  public int getOrder() {
    if (annotation == null) {
      return Integer.MAX_VALUE;
    }

    for (ExecutableElement ee : annotation.getElementValues().keySet()) {
      if (ee.toString().equals("order()")) {
        return (int) annotation.getElementValues().get(ee).getValue();
      }
    }

    return Integer.MAX_VALUE;
  }

  public boolean isList() {
    return list;
  }

  public boolean isSet() {
    return set;
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
        + ", field="
        + field
        + ", type="
        + type
        + ", list="
        + list
        + ", set="
        + set
        + ", annotation="
        + annotation
        + ", converter="
        + converter
        + '}';
  }
}
