package org.bobstuff.bobbson.processor;

import com.squareup.javapoet.*;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.bobstuff.bobbson.*;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ParserGenerator {
  public static final String CONVERTER_PRE = "converter_";
  public static final String ESCAPED_DOT = "\\.";
  public static final String END_OF_DOCUMENT_POST = ".END_OF_DOCUMENT";

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
    //    public Map<AttributeResult, ConverterTypeWrapper> converterLookups = new HashMap<>();
    public Map<TypeMirror, MethodSpec> lookupMethods = new HashMap<>();
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
      TypeMirror attributeType = attribute.getDeclaredType();
      TypeName typeName = TypeName.get(attributeType);
      TypeName boxSafeTypeName = boxSafeTypeName(attributeType, typeName, types);
      TypeName converter =
          ParameterizedTypeName.get(ClassName.get(BobBsonConverter.class), boxSafeTypeName);

      var converterType = attribute.getConverterType();
      if (converterType != null) {
        String fieldName = CONVERTER_PRE + attribute.name;
        lookupData.fields.add(
            FieldSpec.builder(converter, fieldName, Modifier.PRIVATE)
                .initializer("new $T()", converterType)
                .build());
      } else {
        MethodSpec methodSpec = lookupData.lookupMethods.get(attributeType);
        if (methodSpec == null) {
          String fieldName = CONVERTER_PRE + attributeType.toString().replaceAll(ESCAPED_DOT, "_");
          lookupData.fields.add(FieldSpec.builder(converter, fieldName, Modifier.PRIVATE).build());

          methodSpec = generateLookupMethod(attributeType, types);
          lookupData.lookupMethods.put(attributeType, methodSpec);
        }
      }
    }

    return lookupData;
  }

  private MethodSpec generateLookupMethod(TypeMirror type, Types types) {
    TypeName model = TypeName.get(type);
    TypeName boxSafeModel = model;
    String fieldName = CONVERTER_PRE + type.toString().replaceAll(ESCAPED_DOT, "_");
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
        .addStatement("writer.writeStartArray($NBytes)", attribute.getName())
        .beginControlFlow("for (var e : obj.$N())", attribute.readMethod.getSimpleName())
        .addStatement("$N().write(writer, e)", attribute.getConverterFieldName())
        .endControlFlow()
        .addStatement("writer.writeEndArray()")
        .build();
  }

  protected CodeBlock generateWriterMapCode(AttributeResult attribute) {
    return CodeBlock.builder()
        .addStatement("writer.writeStartDocument($NBytes)", attribute.getName())
        .beginControlFlow("for (var e : obj.$N().entrySet())", attribute.readMethod.getSimpleName())
        .addStatement(
            "$N().write(writer, e.getKey(), e.getValue())", attribute.getConverterFieldName())
        .endControlFlow()
        .addStatement("writer.writeEndDocument()")
        .build();
  }

  public CodeBlock generateWriterCode(StructInfo structInfo) {
    CodeBlock.Builder block = CodeBlock.builder();
    for (var entry : structInfo.attributes.entrySet()) {
      var attribute = entry.getValue();
      var attributeName = entry.getKey();
      String fieldName = attribute.getConverterFieldName();

      if (fieldName == null) {
        throw new RuntimeException("broken fieldName is null");
      }

      if ((attribute.isList() || attribute.isSet()) && attribute.getConverterType() == null) {
        block.add(generateWriterCollectionCode(attribute));
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
    }

    return block.build();
  }

  protected CodeBlock generateParserCollectionCode(Class<?> clazz, AttributeResult attribute) {
    return CodeBlock.builder()
        .addStatement("var list = new $T<$N>()", clazz, attribute.getParam())
        .addStatement("reader.readStartArray()")
        .addStatement("var type_i = $T.NOT_SET", BsonType.class)
        .beginControlFlow(
            "while ((type_i = reader.readBsonType()) != $T" + END_OF_DOCUMENT_POST + ")",
            BsonType.class)
        .addStatement("list.add($N().read(reader))", attribute.getConverterFieldName())
        .endControlFlow()
        .addStatement("reader.readEndArray()")
        .addStatement("result.$N(list)", attribute.writeMethod.getSimpleName())
        .build();
  }

  protected CodeBlock generateParserMapCode(AttributeResult attribute) {
    return CodeBlock.builder()
        .addStatement(
            "var map = new $T<$T, $N>()", HashMap.class, String.class, attribute.getParam())
        .addStatement("reader.readStartDocument()")
        .addStatement("var type_i = $T.NOT_SET", BsonType.class)
        .beginControlFlow(
            "while ((type_i = reader.readBsonType()) != $T" + END_OF_DOCUMENT_POST + ")",
            BsonType.class)
        .addStatement(
            "map.put(reader.currentFieldName(), $N().read(reader))",
            attribute.getConverterFieldName())
        .endControlFlow()
        .addStatement("reader.readEndDocument()")
        .addStatement("result.$N(map)", attribute.writeMethod.getSimpleName())
        .build();
  }

  protected CodeBlock generateParserCode(StructInfo structInfo) {
    CodeBlock.Builder block = CodeBlock.builder();
    block.addStatement("var range = reader.getFieldName()");

    var first = true;
    for (var entry : structInfo.attributes.entrySet()) {
      var attribute = entry.getValue();
      var attributeName = entry.getKey();
      String fieldName = attribute.getConverterFieldName();

      if (fieldName == null) {
        throw new RuntimeException("broken fieldName is null");
      }

      int hash = HashUtils.generateHash(attribute.getAliasName());

      if (first) {
        block.beginControlFlow(
            "if (!$NCheck && range.equalsArray($NBytes, $L))", attributeName, attributeName, hash);
        first = false;
      } else {
        block.nextControlFlow(
            "else if (!$NCheck && range.equalsArray($NBytes, $L))",
            attributeName,
            attributeName,
            hash);
      }
      block.addStatement("$NCheck = readAllValues ? false : true", attributeName);

      if (attribute.isList() && attribute.getConverterType() == null) {
        block.add(generateParserCollectionCode(ArrayList.class, attribute));
      } else if (attribute.isSet() && attribute.getConverterType() == null) {
        block.add(generateParserCollectionCode(HashSet.class, attribute));
      } else if (attribute.isMap() && attribute.getConverterType() == null) {
        block.add(generateParserMapCode(attribute));
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
    }
    block.nextControlFlow("else").addStatement("reader.skipValue()");
    block.endControlFlow();

    block.beginControlFlow(
        "if (!readAllValues && $L)", structInfo.getAttributeReadAllBooleanLogic());
    block.addStatement("reader.skipContext()");
    block.addStatement("break");
    block.endControlFlow();
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

  protected MethodSpec generateReadMethod(StructInfo structInfo, Types types) {
    ClassName model = ClassName.get(structInfo.element);

    return MethodSpec.methodBuilder("read")
        .addModifiers(Modifier.PUBLIC)
        .addParameter(BsonReader.class, "reader")
        .returns(model)
        .beginControlFlow("if (reader.getCurrentBsonType() == BsonType.NULL)", BsonType.class)
        .addStatement("reader.readNull()")
        .addStatement("return null")
        .endControlFlow()
        .addStatement("$T result = new $T()", model, model)
        .addStatement("reader.readStartDocument()")
        .addStatement("var type = $T.NOT_SET", BsonType.class)
        .addCode(generateParserPreamble(structInfo, types))
        .beginControlFlow(
            "while ((type = reader.readBsonType()) != $T" + END_OF_DOCUMENT_POST + ")",
            BsonType.class)
        .addCode(generateParserCode(structInfo))
        .endControlFlow()
        .addStatement("reader.readEndDocument()")
        .addStatement("return result")
        .build();
  }

  protected MethodSpec generateWriteMethodWithKey(
      StructInfo structInfo, ClassName model, Types types) {
    return MethodSpec.methodBuilder("write")
        .addModifiers(Modifier.PUBLIC)
        .addParameter(BsonWriter.class, "writer")
        .addParameter(byte[].class, "key")
        .addParameter(model, "obj")
        .beginControlFlow("if (obj == null)")
        .addStatement("writer.writeNull(key)")
        .addStatement("return")
        .endControlFlow()
        .beginControlFlow("if (key != null)")
        .addStatement("writer.writeStartDocument(key)")
        .nextControlFlow("else")
        .addStatement("writer.writeStartDocument()")
        .endControlFlow()
        .addCode(generateWriterCode(structInfo))
        .addStatement("writer.writeEndDocument()")
        .build();
  }

  public void generate(StructInfo structInfo, Writer writer, Types types, Elements elements)
      throws Exception {

    ClassName model = ClassName.get(structInfo.element);
    MethodSpec readMethod = generateReadMethod(structInfo, types);
    MethodSpec writeMethod = generateWriteMethodNoKey(model);
    var writeMethodWithKey = generateWriteMethodWithKey(structInfo, model, types);

    var keyByteArrays = generateKeyByteArrays(structInfo);
    var lookupData = generateConverterLookupMethods(structInfo, types);

    TypeSpec innerStruct =
        TypeSpec.classBuilder("_" + structInfo.getClassName() + "_BobBsonConverter")
            .addAnnotation(
                AnnotationSpec.builder(SuppressWarnings.class)
                    .addMember("value", "$S", "unchecked")
                    .build())
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addField(BobBson.class, "bobBson", Modifier.PRIVATE)
            .addFields(lookupData.fields)
            .addFields(keyByteArrays)
            .addMethods(lookupData.lookupMethods.values())
            .addSuperinterface(
                ParameterizedTypeName.get(ClassName.get(BobBsonConverter.class), model))
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(BobBson.class, "bobBson")
                    .addStatement("this.bobBson = bobBson")
                    .build())
            .addMethod(readMethod)
            .addMethod(writeMethod)
            .addMethod(writeMethodWithKey)
            .build();

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

  @NonNull
  protected MethodSpec generateWriteMethodNoKey(ClassName model) {
    return MethodSpec.methodBuilder("write")
        .addModifiers(Modifier.PUBLIC)
        .addParameter(BsonWriter.class, "writer")
        .addParameter(model, "obj")
        .addStatement("this.write(writer, (byte[]) null, obj)")
        .build();
  }

  private @NonNull MethodSpec getRegisterMethod(String className, ClassName model) {
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