package org.bobstuff.bobbson.processor;

import static org.bobstuff.bobbson.processor.AnnotationUtils.findAnnotationMirror;
import static org.bobstuff.bobbson.processor.BeanUtils.findGetter;
import static org.bobstuff.bobbson.processor.BeanUtils.findSetter;

import java.util.*;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.bobstuff.bobbson.annotations.BsonAttribute;
import org.bobstuff.bobbson.annotations.BsonConverter;
import org.bobstuff.bobbson.annotations.BsonWriterOptions;
import org.bobstuff.bobbson.annotations.CompiledBson;

public class Analysis {
  private Types types;
  private Elements elements;
  private BobMessager messager;
  private TypeElement compileElement;
  private DeclaredType compileType;
  private TypeElement attributeElement;
  private DeclaredType attributeType;
  private TypeElement converterElement;
  private DeclaredType converterType;
  private TypeElement writerOptionsElement;
  private DeclaredType writerOptionsType;

  public Analysis(Types types, Elements elements, BobMessager bobMessager) {
    this.types = types;
    this.elements = elements;
    this.messager = bobMessager;
    this.compileElement = elements.getTypeElement(CompiledBson.class.getName());
    this.compileType = types.getDeclaredType(compileElement);
    this.attributeElement = elements.getTypeElement(BsonAttribute.class.getName());
    this.attributeType = types.getDeclaredType(attributeElement);
    this.converterElement = elements.getTypeElement(BsonConverter.class.getName());
    this.converterType = types.getDeclaredType(converterElement);
    this.writerOptionsElement = elements.getTypeElement(BsonWriterOptions.class.getName());
    this.writerOptionsType = types.getDeclaredType(writerOptionsElement);
  }

  public Map<String, AttributeResult> getAttributes(TypeElement typeElement) {
    if (typeElement == null) {
      return Collections.emptyMap();
    }

    var results = new HashMap<String, AttributeResult>();
    var methods = ElementFilter.methodsIn(typeElement.getEnclosedElements());
    var fields = ElementFilter.fieldsIn(typeElement.getEnclosedElements());

    for (var field : fields) {
      var modifiers = field.getModifiers();
      if (!(modifiers.contains(Modifier.NATIVE)
          || modifiers.contains(Modifier.STATIC)
          || modifiers.contains(Modifier.TRANSIENT))) {
        messager.debug("field type - " + field.asType());
        TypeMirror t = field.asType();
        TypeMirror comparable = types.erasure(field.asType());

        boolean isList = false;
        boolean isSet = false;
        boolean isMap = false;
        TypeMirror listType = types.erasure(elements.getTypeElement(List.class.getName()).asType());
        TypeMirror setType = types.erasure(elements.getTypeElement(Set.class.getName()).asType());
        TypeMirror mapType = types.erasure(elements.getTypeElement(Map.class.getName()).asType());
        final String leftStr = t.toString();
        final String rightStr = listType.toString();
        if (leftStr.equals(rightStr)) {
          messager.debug("raw collection match");
        }
        if (t.getKind() != listType.getKind()) {
          messager.debug("thing is not a list");
        }
        TypeMirror leftRaw = types.erasure(t);
        if (types.isAssignable(listType, leftRaw)) {
          messager.debug("type is a list");
          isList = true;

          DeclaredType dclt = (DeclaredType) t;
          for (TypeMirror argument : dclt.getTypeArguments()) {
            messager.debug(argument.toString());
          }
        } else if (types.isAssignable(mapType, leftRaw)) {
          messager.debug("type is a map");
          isMap = true;
        } else if (types.isAssignable(setType, leftRaw)) {
          messager.debug("type is a set");
          isSet = true;
        }

        ExecutableElement getter =
            findGetter(methods, field.getSimpleName().toString(), field.asType(), messager);

        ExecutableElement setter =
            findSetter(methods, field.getSimpleName().toString(), field.asType());

        var annotation = findAnnotationMirror(field, attributeType, types);
        var converter = findAnnotationMirror(field, converterType, types);
        var writerOptions = findAnnotationMirror(field, writerOptionsType, types);
        TypeMirror converterType = null;
        if (converter != null) {
          for (ExecutableElement ee : converter.getElementValues().keySet()) {
            if (ee.toString().equals("target()")) {
              messager.debug(converter.getElementValues().get(ee).getValue().toString());
              String clazz = converter.getElementValues().get(ee).getValue().toString();

              TypeElement element = elements.getTypeElement(clazz);
              DeclaredType type = types.getDeclaredType(element);

              messager.debug("found converter");
              messager.debug(clazz);
              messager.debug(type.toString());
              var m = ElementFilter.methodsIn(element.getEnclosedElements());
              for (ExecutableElement e : m) {
                messager.debug(e.toString());
                if (e.getSimpleName().toString().equals("read")) {
                  if (types.isAssignable(e.getReturnType(), field.asType())) {
                    messager.debug("matching return types");
                    converterType = type;
                  } else {
                    messager.debug("need to look at this more");
                  }
                }
              }
            }
          }
        }

        if (getter == null || setter == null) {
          messager.debug(
              "Field (" + field.getSimpleName() + ") excluded due to missing getter or setter");
          continue;
        }

        results.put(
            field.getSimpleName().toString(),
            new AttributeResult(
                field.getSimpleName().toString(),
                getter,
                setter,
                field,
                field.asType(),
                isList,
                isSet,
                isMap,
                annotation,
                converter,
                writerOptions,
                converterType));
      }
    }
    return results;
  }

  public Map<String, StructInfo> analyse(Set<? extends Element> compiledBsonInstances) {
    Map<String, StructInfo> structs = new HashMap<>();

    for (var el : compiledBsonInstances) {
      messager.debug("analysing instance of element " + el.toString());

      if (!(el instanceof TypeElement)) {
        continue;
      }

      var element = (TypeElement) el;
      var annotation = findAnnotationMirror(element, compileType, types);
      if (annotation == null) {
        messager.debug(
            "Compile type annotation " + compileType.toString() + " not found on element " + el);
        continue;
      }

      messager.debug("Binary name: " + elements.getBinaryName(element));
      messager.debug("Qualified name: " + elements.getName(element.getQualifiedName()));

      var attributes = getAttributes(element);

      messager.debug("Found attributes: ");
      for (var attribute : attributes.entrySet()) {
        messager.debug(attribute.getKey() + ": " + attribute.getValue());
      }

      StructInfo structInfo =
          new StructInfo(
              element,
              compileType,
              "struct-" + structs.size(),
              elements.getBinaryName(element).toString(),
              annotation,
              attributes);
      structs.put(el.toString(), structInfo);
    }

    return structs;
  }
}
