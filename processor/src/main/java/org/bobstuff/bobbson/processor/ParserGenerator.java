package org.bobstuff.bobbson.processor;

import static java.util.stream.Collectors.toList;

import com.squareup.javapoet.*;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.bobstuff.bobbson.*;
import org.bobstuff.bobbson.converters.CollectionConverters;
import org.bobstuff.bobbson.converters.PrimitiveConverters;
import org.bobstuff.bobbson.converters.StringBsonConverter;
import org.bobstuff.bobbson.reader.BsonReader;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "dereference.of.nullable"})
public class ParserGenerator {
  public static final String CONVERTER_PRE = "converter_";
  public static final String ESCAPED_DOT = "\\.";
  public static final String ARRAY_BRACKETS = "[]";
  public static final String END_OF_DOCUMENT_POST = ".END_OF_DOCUMENT";
  public static final String RETURN_RESULT = "return result";

  private BobMessager messager;

  public ParserGenerator(BobMessager messager) {
    this.messager = messager;
  }

  private static class ConverterTypeWrapper {
    public TypeMirror type;
    public @Nullable TypeMirror converterType;
    public @Nullable MethodSpec method;

    public ConverterTypeWrapper(
        TypeMirror type, @Nullable TypeMirror converterType, @Nullable MethodSpec method) {
      this.type = type;
      this.converterType = converterType;
      this.method = method;
    }
  }

  protected static class LookupData {
    public Map<String, MethodSpec> lookupMethods = new HashMap<>();
    public List<FieldSpec> fields = new ArrayList<>();
  }

  private TypeName boxSafeTypeName(TypeMirror type, TypeName typeName, Types types) {
    if (typeName.isPrimitive()) {
      return TypeName.get(types.boxedClass((PrimitiveType) type).asType());
    }
    return typeName;
  }

  protected LookupData generateConverterLookupMethods(StructInfo structInfo, Types types) {
    LookupData lookupData = new LookupData();

    var attributes = structInfo.attributes;
    for (var attribute : attributes.values()) {
      messager.debug("LOOKUP FOR ATTRIBUTE: " + attribute);
      TypeMirror attributeType = attribute.getDeclaredType();
      TypeName typeName = TypeName.get(attributeType);
      TypeName boxSafeTypeName = boxSafeTypeName(attributeType, typeName, types);
      TypeName converter =
          ParameterizedTypeName.get(ClassName.get(BobBsonConverter.class), boxSafeTypeName);

      var converterType = attribute.getConverterType();
      messager.debug("conv type: " + converterType);
      if (converterType != null) {
        String fieldName = CONVERTER_PRE + attribute.name;
        messager.debug(fieldName);
        lookupData.fields.add(
            FieldSpec.builder(converter, fieldName, Modifier.PRIVATE)
                .initializer("new $T()", converterType)
                .build());
      } else {
        MethodSpec methodSpec = lookupData.lookupMethods.get(typeName.toString());
        if (methodSpec == null) {
          String fieldName =
              CONVERTER_PRE
                  + typeName
                      .toString()
                      .replaceAll(ESCAPED_DOT, "_")
                      .replace(ARRAY_BRACKETS, "_array_");
          lookupData.fields.add(FieldSpec.builder(converter, fieldName, Modifier.PRIVATE).build());

          methodSpec = generateLookupMethod(attributeType, types);
          lookupData.lookupMethods.put(typeName.toString(), methodSpec);
        }
      }
    }

    return lookupData;
  }

  private MethodSpec generateLookupMethod(TypeMirror type, Types types) {
    TypeName model = TypeName.get(type);
    TypeName boxSafeModel = model;
    String fieldName =
        CONVERTER_PRE
            + model.toString().replaceAll(ESCAPED_DOT, "_").replace(ARRAY_BRACKETS, "_array_");
    if (model.isPrimitive()) {
      boxSafeModel = TypeName.get(types.boxedClass((PrimitiveType) type).asType());
    }
    TypeName converter =
        ParameterizedTypeName.get(ClassName.get(BobBsonConverter.class), boxSafeModel);

    return MethodSpec.methodBuilder(fieldName)
        .addModifiers(Modifier.PUBLIC)
        .beginControlFlow("if ($N == null)", fieldName)
        .addStatement("$N = ($T) bobBson.tryFindConverter($T.class)", fieldName, converter, model)
        .endControlFlow()
        .addStatement("return $N", fieldName)
        .returns(converter)
        .build();
  }

  public CodeBlock generateParserPreamble(StructInfo structInfo, Types types) {
    CodeBlock.Builder block = CodeBlock.builder();
    block.addStatement("var readAllValues = false");
    for (var entry : structInfo.attributes.entrySet()) {
      block.addStatement("boolean $NCheck = false", entry.getKey());
    }
    return block.build();
  }

  protected CodeBlock generateWriterCollectionCode(AttributeResult attribute) {
    return CodeBlock.builder()
        //        .beginControlFlow("")
        .addStatement("writer.writeName($NBytes)", attribute.getName())
        .addStatement(
            "$T.writeList(writer, obj.$N(), $N())",
            CollectionConverters.class,
            attribute.readMethod.getSimpleName(),
            attribute.getConverterFieldName())
        .build();
  }

  protected CodeBlock generateWriterSetCode(AttributeResult attribute) {
    return CodeBlock.builder()
        .addStatement("writer.writeName($NBytes)", attribute.getName())
        .addStatement(
            "$T.writeSet(writer, obj.$N(), $N())",
            CollectionConverters.class,
            attribute.readMethod.getSimpleName(),
            attribute.getConverterFieldName())
        .build();
  }

  protected CodeBlock generateWriterMapCode(AttributeResult attribute) {
    return CodeBlock.builder()
        .addStatement("writer.writeName($NBytes)", attribute.getName())
        .addStatement(
            "$T.writeMap(writer, obj.$N(), $N())",
            CollectionConverters.class,
            attribute.readMethod.getSimpleName(),
            attribute.getConverterFieldName())
        .build();
  }

  public CodeBlock generateWriterCode(StructInfo structInfo) {
    CodeBlock.Builder block = CodeBlock.builder();

    Map<String, AttributeResult> result = structInfo.attributesByOrder();

    for (var entry : result.entrySet()) {
      var attribute = entry.getValue();
      var attributeName = entry.getKey();
      String fieldName = attribute.getConverterFieldName();
      var writeNull = attribute.writerOptionWriteNull();

      if (fieldName == null) {
        throw new RuntimeException("broken fieldName is null");
      }

      if (!writeNull) {
        block.beginControlFlow("if (obj.$N() != null)", attribute.readMethod.getSimpleName());
      }

      if ((attribute.isList()) && attribute.getConverterType() == null) {
        block.add(generateWriterCollectionCode(attribute));
      } else if ((attribute.isSet()) && attribute.getConverterType() == null) {
        block.add(generateWriterSetCode(attribute));
      } else if (attribute.isPrimitive()) {
        if (attribute.getDeclaredType().getKind() == TypeKind.INT) {
          block.addStatement(
              "writer.writeInteger($NBytes, obj.$N())",
              attributeName,
              attribute.readMethod.getSimpleName());
        } else if (attribute.getDeclaredType().getKind() == TypeKind.DOUBLE) {
          block.addStatement(
              "writer.writeDouble($NBytes, obj.$N())",
              attributeName,
              attribute.readMethod.getSimpleName());
        } else if (attribute.getDeclaredType().getKind() == TypeKind.LONG) {
          block.addStatement(
              "writer.writeLong($NBytes, obj.$N())",
              attributeName,
              attribute.readMethod.getSimpleName());
        } else if (attribute.getDeclaredType().getKind() == TypeKind.BOOLEAN) {
          block.addStatement(
              "writer.writeBoolean($NBytes, obj.$N())",
              attributeName,
              attribute.readMethod.getSimpleName());
        } else {
          throw new RuntimeException("Attempting to write unknown primitive type " + attribute);
        }
      } else if (attribute.isMap() && attribute.getConverterType() == null) {
        block.add(generateWriterMapCode(attribute));
      } else {
        if (attribute.getConverterType() != null) {
          block.addStatement(
              "$N.write(writer, $NBytes, obj.$N())",
              attribute.getConverterFieldName(),
              attributeName,
              attribute.readMethod.getSimpleName());
        } else {
          block.addStatement(
              "$N().write(writer, $NBytes, obj.$N())",
              fieldName,
              attributeName,
              attribute.readMethod.getSimpleName());
        }
      }

      if (!writeNull) {
        block.endControlFlow();
      }
    }

    return block.build();
  }

  protected CodeBlock generateParserSetCode(Class<?> clazz, AttributeResult attribute) {
    return CodeBlock.builder()
        .addStatement(
            "result.$N($T.readSet(reader, type, $N()))",
            attribute.writeMethod.getSimpleName(),
            CollectionConverters.class,
            attribute.getConverterFieldName())
        .build();
  }

  protected CodeBlock generateRecordParserSetCode(Class<?> clazz, AttributeResult attribute) {
    return CodeBlock.builder()
        .addStatement(
            "_$N = $T.readSet(reader, type, $N())",
            attribute.getName(),
            CollectionConverters.class,
            attribute.getConverterFieldName())
        .build();
  }

  protected CodeBlock generateParserCollectionCode(Class<?> clazz, AttributeResult attribute) {
    return CodeBlock.builder()
        .addStatement(
            "result.$N($T.readList(reader, type, $N()))",
            attribute.writeMethod.getSimpleName(),
            CollectionConverters.class,
            attribute.getConverterFieldName())
        .build();
  }

  protected CodeBlock generateRecordParserCollectionCode(
      Class<?> clazz, AttributeResult attribute) {
    return CodeBlock.builder()
        .addStatement(
            "_$N = $T.readList(reader, type, $N())",
            attribute.getName(),
            CollectionConverters.class,
            attribute.getConverterFieldName())
        .build();
  }

  protected CodeBlock generateRecordParserMapCode(AttributeResult attribute) {
    return CodeBlock.builder()
        .addStatement(
            "_$N = $T.readMap(reader, type, $N())",
            attribute.getName(),
            CollectionConverters.class,
            attribute.getConverterFieldName())
        .build();
  }

  protected CodeBlock generateParserMapCode(AttributeResult attribute) {
    return CodeBlock.builder()
        .addStatement(
            "result.$N($T.readMap(reader, type, $N()))",
            attribute.writeMethod.getSimpleName(),
            CollectionConverters.class,
            attribute.getConverterFieldName())
        .build();
  }

  protected CodeBlock generateParserFastPath(StructInfo structInfo) {
    CodeBlock.Builder block = CodeBlock.builder();
    block.addStatement("type = reader.readBsonType()");
    block.addStatement("var range = reader.getFieldName()");

    Map<String, AttributeResult> result =
        structInfo.attributes.entrySet().stream()
            .sorted(Comparator.comparingInt(a -> a.getValue().getOrder()))
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (oldValue, newValue) -> oldValue,
                    LinkedHashMap::new));

    for (var entry : result.entrySet()) {
      var attribute = entry.getValue();
      var attributeName = entry.getKey();
      String fieldName = attribute.getConverterFieldName();

      if (fieldName == null) {
        throw new RuntimeException("broken fieldName is null");
      }

      int hash = HashUtils.generateHash(attribute.getAliasName());

      block.beginControlFlow("if (!range.equalsArray($NBytes, $L))", attributeName, hash);
      block.addStatement("readSlow(reader, result, type, fieldsRead)");
      block.addStatement(RETURN_RESULT);
      block.endControlFlow();

      block.addStatement("fieldsRead += 1");
      if (attribute.isList() && attribute.getConverterType() == null) {
        block.add(generateParserCollectionCode(ArrayList.class, attribute));
      } else if (attribute.isSet() && attribute.getConverterType() == null) {
        block.add(generateParserSetCode(HashSet.class, attribute));
      } else if (attribute.isMap() && attribute.getConverterType() == null) {
        block.add(generateParserMapCode(attribute));
      } else if (attribute.isPrimitive()) {
        if (attribute.getDeclaredType().getKind() == TypeKind.INT) {
          block.addStatement(
              "result.$N($T.parseInteger(reader))",
              attribute.writeMethod.getSimpleName(),
              PrimitiveConverters.class);
        } else if (attribute.getDeclaredType().getKind() == TypeKind.DOUBLE) {
          block.addStatement(
              "result.$N($T.parseDouble(reader))",
              attribute.writeMethod.getSimpleName(),
              PrimitiveConverters.class);
        } else if (attribute.getDeclaredType().getKind() == TypeKind.LONG) {
          block.addStatement(
              "result.$N($T.parseLong(reader))",
              attribute.writeMethod.getSimpleName(),
              PrimitiveConverters.class);
        } else if (attribute.getDeclaredType().getKind() == TypeKind.BOOLEAN) {
          block.addStatement(
              "result.$N($T.parseBoolean(reader))",
              attribute.writeMethod.getSimpleName(),
              PrimitiveConverters.class);
        } else {
          throw new RuntimeException("Attempting to read unknown primitive type " + attribute);
        }
      } else {
        if (attribute.getConverterType() != null) {
          block.addStatement(
              "result.$N($N.read(reader))",
              attribute.writeMethod.getSimpleName(),
              attribute.getConverterName());
        } else {
          block.addStatement(
              "result.$N($N().read(reader))", attribute.writeMethod.getSimpleName(), fieldName);
        }
      }

      block.addStatement("type = reader.readBsonType()");
    }
    return block.build();
  }

  protected CodeBlock generateParserCode(StructInfo structInfo) {
    CodeBlock.Builder block = CodeBlock.builder();

    if (structInfo.attributes.size() == 0) {
      return block.build();
    }
    Map<String, AttributeResult> result =
        structInfo.attributes.entrySet().stream()
            .sorted(Comparator.comparingInt(a -> a.getValue().getOrder()))
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (oldValue, newValue) -> oldValue,
                    LinkedHashMap::new));
    var first = true;
    for (var entry : result.entrySet()) {
      var attribute = entry.getValue();
      var attributeName = entry.getKey();
      String fieldName = attribute.getConverterFieldName();

      if (fieldName == null) {
        throw new RuntimeException("broken fieldName is null");
      }

      int hash = HashUtils.generateHash(attribute.getAliasName());

      if (first) {
        block.beginControlFlow("if (range.equalsArray($NBytes, $L))", attributeName, hash);
        first = false;
      } else {
        block.nextControlFlow("else if (range.equalsArray($NBytes, $L))", attributeName, hash);
      }

      block.addStatement("fieldsRead += 1");

      if (attribute.isList() && attribute.getConverterType() == null) {
        block.add(generateParserCollectionCode(ArrayList.class, attribute));
      } else if (attribute.isSet() && attribute.getConverterType() == null) {
        block.add(generateParserSetCode(HashSet.class, attribute));
      } else if (attribute.isMap() && attribute.getConverterType() == null) {
        block.add(generateParserMapCode(attribute));
      } else if (attribute.getConverterType() != null) {
        block.addStatement(
            "result.$N($N.read(reader))",
            attribute.writeMethod.getSimpleName(),
            attribute.getConverterName());
      } else if (attribute.isPrimitive()) {
        if (attribute.getDeclaredType().getKind() == TypeKind.INT) {
          block.addStatement(
              "result.$N($T.parseInteger(reader))",
              attribute.writeMethod.getSimpleName(),
              PrimitiveConverters.class);
        } else if (attribute.getDeclaredType().getKind() == TypeKind.DOUBLE) {
          block.addStatement(
              "result.$N($T.parseDouble(reader))",
              attribute.writeMethod.getSimpleName(),
              PrimitiveConverters.class);
        } else if (attribute.getDeclaredType().getKind() == TypeKind.LONG) {
          block.addStatement(
              "result.$N($T.parseLong(reader))",
              attribute.writeMethod.getSimpleName(),
              PrimitiveConverters.class);
        } else if (attribute.getDeclaredType().getKind() == TypeKind.BOOLEAN) {
          block.addStatement(
              "result.$N($T.parseBoolean(reader))",
              attribute.writeMethod.getSimpleName(),
              PrimitiveConverters.class);
        } else {
          throw new RuntimeException("Attempting to read unknown primitive type " + attribute);
        }
      } else if (ClassName.get(attribute.getType()).toString().equals("java.lang.String")) {
        block.addStatement(
            "result.$N($T.readString(reader))",
            attribute.writeMethod.getSimpleName(),
            StringBsonConverter.class);
      } else {

        block.addStatement(
            "result.$N($N().read(reader))", attribute.writeMethod.getSimpleName(), fieldName);
      }
    }
    block.nextControlFlow("else").addStatement("reader.skipValue()");
    block.endControlFlow();

    block
        .beginControlFlow("if (fieldsRead == EXPECTED_FIELD_COUNT)")
        .addStatement("reader.skipContext()")
        .addStatement("reader.readEndDocument()")
        .addStatement(RETURN_RESULT)
        .endControlFlow();

    return block.build();
  }

  protected List<FieldSpec> generateKeyByteArrays(StructInfo structInfo) {
    var fieldSpecs = new ArrayList<FieldSpec>();
    for (var attribute : structInfo.attributes.values()) {
      fieldSpecs.add(
          FieldSpec.builder(TypeName.get(byte[].class), attribute.getName() + "Bytes")
              .initializer(
                  "$S.getBytes($T.UTF_8)", attribute.getAliasName(), StandardCharsets.class)
              .addModifiers(Modifier.PRIVATE)
              .build());
    }
    return fieldSpecs;
  }

  protected MethodSpec generateReadSlowMethod(StructInfo structInfo, Types types) {
    var type = TypeName.get(structInfo.element.asType());
    return MethodSpec.methodBuilder("readSlow")
        .addModifiers(Modifier.PRIVATE)
        .addParameter(BsonReader.class, "reader")
        .addParameter(type, "result")
        .addParameter(BsonType.class, "type")
        .addParameter(int.class, "fieldsRead")
        .returns(type)
        .beginControlFlow("if (type == $T" + END_OF_DOCUMENT_POST + ")", BsonType.class)
        .addStatement("reader.readEndDocument()")
        .addStatement("return result")
        .endControlFlow()
        .addStatement("var range = reader.getFieldName()")
        .addCode(generateParserCode(structInfo))
        .beginControlFlow(
            "while ((type = reader.readBsonType()) != $T" + END_OF_DOCUMENT_POST + ")",
            BsonType.class)
        .addCode(generateParserCode(structInfo))
        .endControlFlow()
        .addStatement("reader.readEndDocument()")
        .addStatement(RETURN_RESULT)
        .build();
  }

  private CodeBlock generateLocalVariables(StructInfo structInfo) {
    CodeBlock.Builder block = CodeBlock.builder();

    if (structInfo.attributes.size() == 0) {
      return block.build();
    }

    var orderedAttributes = structInfo.attributesByOrder();

    for (var entry : orderedAttributes.entrySet()) {
      var key = entry.getKey();
      var value = entry.getValue();

      if (value.isPrimitive()) {
        block.addStatement("$T _$N = $N", value.getType(), key, primitiveDefault(value));
      } else {
        block.addStatement("$T _$N = null", value.getType(), key);
      }
    }

    return block.build();
  }

  private String primitiveDefault(AttributeResult attribute) {
    var kind = attribute.getDeclaredType().getKind();
    if (kind == TypeKind.INT
        || kind == TypeKind.DOUBLE
        || kind == TypeKind.LONG
        || kind == TypeKind.FLOAT
        || kind == TypeKind.BYTE) {
      return "0";
    }

    throw new UnsupportedOperationException("primitive kind " + kind + " is not currently handled");
  }

  protected CodeBlock generateRecordParserCode(StructInfo structInfo) {
    CodeBlock.Builder block = CodeBlock.builder();

    if (structInfo.attributes.size() == 0) {
      return block.build();
    }

    var attributes = structInfo.attributesByOrder();

    var first = true;
    for (var entry : attributes.entrySet()) {
      var attribute = entry.getValue();
      var attributeName = entry.getKey();
      String fieldName = attribute.getConverterFieldName();

      if (fieldName == null) {
        throw new RuntimeException("broken fieldName is null");
      }

      int hash = HashUtils.generateHash(attribute.getAliasName());

      if (first) {
        block.beginControlFlow("if (range.equalsArray($NBytes, $L))", attributeName, hash);
        first = false;
      } else {
        block.nextControlFlow("else if (range.equalsArray($NBytes, $L))", attributeName, hash);
      }

      block.addStatement("fieldsRead += 1");

      if (attribute.isList() && attribute.getConverterType() == null) {
        block.add(generateRecordParserCollectionCode(ArrayList.class, attribute));
      } else if (attribute.isSet() && attribute.getConverterType() == null) {
        block.add(generateRecordParserSetCode(HashSet.class, attribute));
      } else if (attribute.isMap() && attribute.getConverterType() == null) {
        block.add(generateRecordParserMapCode(attribute));
      } else if (attribute.getConverterType() != null) {
        block.addStatement(
            "_$N = $N.read(reader)", attribute.getName(), attribute.getConverterName());
      } else if (attribute.isPrimitive()) {
        if (attribute.getDeclaredType().getKind() == TypeKind.INT) {
          block.addStatement(
              "_$N = $T.parseInteger(reader)", attribute.getName(), PrimitiveConverters.class);
        } else if (attribute.getDeclaredType().getKind() == TypeKind.DOUBLE) {
          block.addStatement(
              "_$N = $T.parseDouble(reader)", attribute.getName(), PrimitiveConverters.class);
        } else if (attribute.getDeclaredType().getKind() == TypeKind.LONG) {
          block.addStatement(
              "_$N = $T.parseLong(reader)", attribute.getName(), PrimitiveConverters.class);
        } else if (attribute.getDeclaredType().getKind() == TypeKind.BOOLEAN) {
          block.addStatement(
              "_$N = $T.parseBoolean(reader)", attribute.getName(), PrimitiveConverters.class);
        } else {
          throw new RuntimeException("Attempting to read unknown primitive type " + attribute);
        }
      } else if (ClassName.get(attribute.getType()).toString().equals("java.lang.String")) {
        block.addStatement(
            "_$N = $T.readString(reader)", attribute.getName(), StringBsonConverter.class);
      } else {

        block.addStatement("_$N = $N().read(reader)", attribute.getName(), fieldName);
      }
    }
    block.nextControlFlow("else").addStatement("reader.skipValue()");
    block.endControlFlow();

    block
        .beginControlFlow("if (fieldsRead == EXPECTED_FIELD_COUNT)")
        .addStatement("reader.skipContext()")
        .addStatement("reader.readEndDocument()")
        .add(generateReturnConstructor(structInfo))
        .endControlFlow();

    return block.build();
  }

  protected MethodSpec generateReadRecordMethod(StructInfo structInfo, Types types) {
    var type = TypeName.get(structInfo.element.asType());
    return MethodSpec.methodBuilder("readValue")
        .addModifiers(Modifier.PUBLIC)
        .addParameter(BsonReader.class, "reader")
        .addParameter(BsonType.class, "type")
        .returns(type)
        .addCode(generateLocalVariables(structInfo))
        .addStatement("var fieldsRead = 0")
        .addStatement("reader.readStartDocument()")
        .addStatement("var range = reader.getFieldName()")
        .beginControlFlow(
            "while ((type = reader.readBsonType()) != $T" + END_OF_DOCUMENT_POST + ")",
            BsonType.class)
        .addCode(generateRecordParserCode(structInfo))
        .endControlFlow()
        .addStatement("reader.readEndDocument()")
        .addCode(generateReturnConstructor(structInfo))
        .build();
  }

  private CodeBlock generateReturnConstructor(StructInfo structInfo) {
    var codeBlock = CodeBlock.builder();
    var type = TypeName.get(structInfo.element.asType());

    var args =
        structInfo.attributes.values().stream()
            .map(attribute -> "_" + attribute.getName())
            .collect(Collectors.joining(","));

    codeBlock.addStatement("return new $T($N)", type, args);

    return codeBlock.build();
  }

  protected MethodSpec generateReadMethod(StructInfo structInfo, Types types) {
    var type = TypeName.get(structInfo.element.asType());
    return MethodSpec.methodBuilder("readValue")
        .addModifiers(Modifier.PUBLIC)
        .addParameter(BsonReader.class, "reader")
        .addParameter(BsonType.class, "outerType")
        .returns(type)
        .addStatement("var fieldsRead = 0", type, type)
        .addStatement("$T result = new $T()", type, type)
        .addStatement("reader.readStartDocument()")
        .addStatement("var type = $T.NOT_SET", BsonType.class)
        .addCode(generateParserFastPath(structInfo))
        .beginControlFlow("if (type != $T.END_OF_DOCUMENT)", BsonType.class)
        .addStatement("reader.skipContext()")
        .endControlFlow()
        .addStatement("reader.readEndDocument()")
        .addStatement(RETURN_RESULT)
        .build();
  }

  protected MethodSpec generateWriteMethodWithKey(
      StructInfo structInfo, TypeName model, Types types) {
    return MethodSpec.methodBuilder("writeValue")
        .addModifiers(Modifier.PUBLIC)
        .addParameter(BsonWriter.class, "writer")
        .addParameter(model, "obj")
        .addStatement("writer.writeStartDocument()")
        .addCode(generateWriterCode(structInfo))
        .addStatement("writer.writeEndDocument()")
        .build();
  }

  public void generate(StructInfo structInfo, Writer writer, Types types, Elements elements)
      throws Exception {
    var typevars =
        structInfo.element.getTypeParameters().stream()
            .map(TypeVariableName::get)
            .collect(toList());
    TypeName type = TypeName.get(structInfo.element.asType());
    ClassName model = ClassName.get(structInfo.element);
    messager.debug("!!!!! " + structInfo.isRecord());
    MethodSpec readMethod =
        structInfo.isRecord()
            ? generateReadRecordMethod(structInfo, types)
            : generateReadMethod(structInfo, types);
    var writeMethodWithKey = generateWriteMethodWithKey(structInfo, type, types);

    var keyByteArrays = generateKeyByteArrays(structInfo);
    var lookupData = generateConverterLookupMethods(structInfo, types);

    TypeSpec.Builder innerStructBuilder =
        TypeSpec.classBuilder("_" + structInfo.getClassName() + "_BobBsonConverter")
            .addTypeVariables(typevars)
            .addAnnotation(
                AnnotationSpec.builder(SuppressWarnings.class)
                    .addMember("value", "$S", "unchecked")
                    .build())
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addField(BobBson.class, "bobBson", Modifier.PRIVATE)
            .addField(
                FieldSpec.builder(
                        int.class, "EXPECTED_FIELD_COUNT", Modifier.PRIVATE, Modifier.STATIC)
                    .initializer(String.valueOf(structInfo.attributes.size()))
                    .build())
            .addFields(lookupData.fields)
            .addFields(keyByteArrays)
            .addMethods(lookupData.lookupMethods.values())
            .addSuperinterface(
                ParameterizedTypeName.get(
                    ClassName.get(BobBsonConverter.class),
                    TypeName.get(structInfo.element.asType())))
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(BobBson.class, "bobBson")
                    .addStatement("this.bobBson = bobBson")
                    .build())
            //            .addMethod(generateReadSlowMethod(structInfo, types))
            .addMethod(readMethod)
            .addMethod(writeMethodWithKey);

    if (!structInfo.isRecord()) {
      innerStructBuilder.addMethod(generateReadSlowMethod(structInfo, types));
    }
    var innerStruct = innerStructBuilder.build();

    TypeSpec struct =
        TypeSpec.classBuilder("_" + structInfo.getClassName() + "_BobBsonConverterRegister")
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(ClassName.get(BobBsonConverterRegister.class))
            .addMethod(getRegisterMethod(structInfo.getClassName(), model))
            .addType(innerStruct)
            .build();

    JavaFile javaFile = JavaFile.builder(structInfo.getPackageName(), struct).indent("  ").build();

    javaFile.writeTo(writer);
  }

  public static @NonNull MethodSpec getRegisterMethod(String className, ClassName model) {
    return MethodSpec.methodBuilder("register")
        .addModifiers(Modifier.PUBLIC)
        .addParameter(BobBson.class, "bobBson")
        .addStatement(
            "var converter = new _"
                + className.replaceAll(Pattern.quote("$"), Matcher.quoteReplacement("$$"))
                + "_BobBsonConverter(bobBson)")
        .addStatement("bobBson.registerConverter($T.class, converter)", model)
        .build();
  }
}
