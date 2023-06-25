package org.bobstuff.bobbson.processor;

import static org.bobstuff.bobbson.processor.AnnotationUtils.findAnnotationMirror;
import static org.bobstuff.bobbson.processor.BeanUtils.findGetter;
import static org.bobstuff.bobbson.processor.BeanUtils.findSetter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.bobstuff.bobbson.annotations.BsonAttribute;
import org.bobstuff.bobbson.annotations.BsonConverter;
import org.bobstuff.bobbson.annotations.BsonWriterOptions;
import org.bobstuff.bobbson.annotations.GenerateBobBsonConverter;

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
    this.compileElement = elements.getTypeElement(GenerateBobBsonConverter.class.getName());
    this.compileType = types.getDeclaredType(compileElement);
    this.attributeElement = elements.getTypeElement(BsonAttribute.class.getName());
    this.attributeType = types.getDeclaredType(attributeElement);
    this.converterElement = elements.getTypeElement(BsonConverter.class.getName());
    this.converterType = types.getDeclaredType(converterElement);
    this.writerOptionsElement = elements.getTypeElement(BsonWriterOptions.class.getName());
    this.writerOptionsType = types.getDeclaredType(writerOptionsElement);
  }

  private FieldType fieldType(TypeMirror type) {
    TypeMirror listType = types.erasure(elements.getTypeElement(List.class.getName()).asType());
    TypeMirror setType = types.erasure(elements.getTypeElement(Set.class.getName()).asType());
    TypeMirror mapType = types.erasure(elements.getTypeElement(Map.class.getName()).asType());

    final String leftStr = type.toString();
    final String rightStr = listType.toString();
    if (leftStr.equals(rightStr)) {
      messager.debug("raw collection match");
    }
    TypeMirror leftRaw = types.erasure(type);
    if (types.isAssignable(listType, leftRaw)) {
      messager.debug("type is a list");
      return FieldType.LIST;
    } else if (types.isAssignable(mapType, leftRaw)) {
      messager.debug("type is a map");
      return FieldType.MAP;
    } else if (types.isAssignable(setType, leftRaw)) {
      messager.debug("type is a set");
      return FieldType.SET;
    }

    return FieldType.OBJECT;
  }

  private Map<String, AttributeResult> getFieldAttributes(
      List<VariableElement> fields, List<ExecutableElement> methods) {
    var results = new HashMap<String, AttributeResult>();

    for (var field : fields) {
      var modifiers = field.getModifiers();
      if ((modifiers.contains(Modifier.NATIVE)
          || modifiers.contains(Modifier.STATIC)
          || modifiers.contains(Modifier.TRANSIENT))) {
        continue;
      }

      var simpleName = field.getSimpleName().toString();

      messager.debug("field type - " + field.asType());
      var fieldType = fieldType(field.asType());

      var getter = findGetter(methods, simpleName, field.asType(), messager);

      var setter = findSetter(methods, simpleName, field.asType());

      var attributeAnnotation =
          AttributeAnnotation.parse(field, attributeType, simpleName, types, elements, messager);
      var converterAnnotation =
          ConverterAnnotation.parse(field, converterType, types, elements, messager);
      var writerOptionsAnnotation = findAnnotationMirror(field, writerOptionsType, types);

      if (attributeAnnotation.isIgnore()) {
        continue;
      }

      if (getter == null || setter == null) {
        messager.debug("ignoring field due to missing getter/setter");
        continue;
      }

      results.put(
          simpleName,
          new AttributeResult(
              simpleName,
              getter,
              setter,
              field.asType(),
              fieldType,
              attributeAnnotation,
              converterAnnotation,
              writerOptionsAnnotation));
    }

    return results;
  }

  @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
  public Map<String, AttributeResult> getAttributes(TypeElement typeElement, boolean isRecord) {
    if (typeElement == null) {
      return Collections.emptyMap();
    }

    var methods = ElementFilter.methodsIn(typeElement.getEnclosedElements());
    var fields = ElementFilter.fieldsIn(typeElement.getEnclosedElements());

    var results = new HashMap<>(getFieldAttributes(fields, methods));

    messager.debug("results" + results);
    for (var method : methods) {
      // TODO ignore if already found in fields
      var annotation = findAnnotationMirror(method, attributeType, types);
      //      var writerOptions = findAnnotationMirror(method, writerOptionsType, types);
      var methodName = method.getSimpleName().toString();
      messager.debug("METHOD NAME: " + methodName);
      if (annotation == null) {
        // method doesn't have an annotation so ignore it for now
        continue;
      }

      var fieldType = fieldType(method.getReturnType());

      messager.debug("fieldtype " + fieldType);

      var propertyName = BeanUtils.extractPropertyNameFromGetter(methodName);
      if (propertyName == null) {
        continue;
      }
      messager.debug("PROPERTY NAME: " + propertyName);

      var attributeAnnotation =
          AttributeAnnotation.parse(method, attributeType, propertyName, types, elements, messager);
      var converterAnnotation =
          ConverterAnnotation.parse(method, converterType, types, elements, messager);
      var writerOptionsAnnotation = findAnnotationMirror(method, writerOptionsType, types);

      if (attributeAnnotation.isIgnore()) {
        continue;
      }

      var setter = findSetter(methods, propertyName, method.getReturnType());

      if (setter == null) {
        messager.debug("Field (" + propertyName + ") excluded due to missing setter");
        continue;
      }

      results.put(
          propertyName,
          new AttributeResult(
              propertyName,
              method,
              setter,
              method.getReturnType(),
              fieldType,
              attributeAnnotation,
              converterAnnotation,
              writerOptionsAnnotation));
    }
    return results;
  }

  public Map<String, AttributeResult> getRecordAttributes(TypeElement el) {
    var recordComponents = el.getRecordComponents();

    var fields = ElementFilter.fieldsIn(el.getEnclosedElements());
    var fieldsMap =
        fields.stream()
            .collect(Collectors.toMap(VariableElement::getSimpleName, Function.identity()));

    var results = new LinkedHashMap<String, AttributeResult>();
    for (var recordComponent : recordComponents) {
      var field = fieldsMap.get(recordComponent.getSimpleName());
      if (field == null) {
        throw new UnsupportedOperationException("cant have missing params on record");
      }
      messager.debug("field type - " + field.asType());

      // collection identification etc
      var name = field.getSimpleName().toString();
      var fieldType = fieldType(field.asType());

      var attributeAnnotation =
          AttributeAnnotation.parse(field, attributeType, name, types, elements, messager);
      var converterAnnotation =
          ConverterAnnotation.parse(field, converterType, types, elements, messager);
      var writerOptionsAnnotation = findAnnotationMirror(field, writerOptionsType, types);

      if (attributeAnnotation.isIgnore()) {
        continue;
      }

      var getter = recordComponent.getAccessor();

      results.put(
          field.getSimpleName().toString(),
          new AttributeResult(
              field.getSimpleName().toString(),
              getter,
              null,
              field.asType(),
              fieldType,
              attributeAnnotation,
              converterAnnotation,
              writerOptionsAnnotation));
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

      var isRecord = el.getKind() == ElementKind.RECORD;
      messager.debug("Element is a record: " + isRecord);

      var element = (TypeElement) el;
      var annotation = findAnnotationMirror(element, compileType, types);
      if (annotation == null) {
        messager.debug(
            "Compile type annotation " + compileType.toString() + " not found on element " + el);
        continue;
      }

      messager.debug("Binary name: " + elements.getBinaryName(element));
      messager.debug("Qualified name: " + elements.getName(element.getQualifiedName()));

      Map<String, AttributeResult> attributes;
      if (isRecord) {
        attributes = getRecordAttributes((TypeElement) el);
      } else {
        attributes = getAttributes(element, isRecord);
      }

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
