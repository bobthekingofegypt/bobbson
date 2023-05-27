package org.bobstuff.bobbson.processor;

import static java.util.stream.Collectors.toList;

import com.squareup.javapoet.*;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.bobstuff.bobbson.*;
import org.bobstuff.bobbson.writer.BsonWriter;
import org.checkerframework.checker.nullness.qual.NonNull;

public class EnumGenerator {
  private List<FieldSpec> getEnumFields(StructInfo structInfo, Types types, Elements elements) {
    List<String> enumValues = structInfo.getEnumConstants();
    List<FieldSpec> fields = new ArrayList<>();
    for (var value : enumValues) {
      var field =
          FieldSpec.builder(
                  TypeName.get(byte[].class), value + "Bytes", Modifier.PRIVATE, Modifier.STATIC)
              .initializer(
                  "$T.$N.name().getBytes($T.UTF_8)",
                  structInfo.element.asType(),
                  value,
                  StandardCharsets.class)
              .build();
      var weakHash = HashUtils.generateHash(value);
      var fieldWeakHash =
          FieldSpec.builder(
                  TypeName.get(int.class), value + "WeakHash", Modifier.PRIVATE, Modifier.STATIC)
              .initializer("$L", weakHash)
              .build();
      fields.add(field);
      fields.add(fieldWeakHash);
    }

    return fields;
  }

  //  @Override
  //  public void write(BsonWriter bsonWriter, String key, Media.Player value) {
  //    bsonWriter.writeString(key, value.name());
  //  }
  //
  //  @Override
  //  public void write(BsonWriter bsonWriter, byte[] key, Media.Player value, boolean
  // writeEnvelope) {
  //    if (value == Media.Player.JAVA) {
  //      bsonWriter.writeString(key, java);
  //    } else {
  //      bsonWriter.writeString(key, flash);
  //    }
  //  }
  //
  //  @Override
  //  public void write(BsonWriter bsonWriter, String key, Media.Player value, boolean
  // writeEnvelope) {
  //    if (value == Media.Player.JAVA) {
  //      bsonWriter.writeString(key, java);
  //    } else {
  //      bsonWriter.writeString(key, flash);
  //    }
  //  }

  private CodeBlock generateWriteEnumClauses(StructInfo structInfo) {
    CodeBlock.Builder block = CodeBlock.builder();

    var first = true;
    for (var entry : structInfo.getEnumConstants()) {
      if (first) {
        block.beginControlFlow("if (obj == $T.$N)", structInfo.element.asType(), entry);
        first = false;
      } else {
        block.nextControlFlow("else if (obj == $T.$N)", structInfo.element.asType(), entry);
      }
      block.addStatement("writer.writeString($NBytes)", entry);
    }
    block.endControlFlow();
    return block.build();
  }

  protected @NonNull MethodSpec generateWriteMethodNoKey(StructInfo structInfo) {
    return MethodSpec.methodBuilder("write")
        .addModifiers(Modifier.PUBLIC)
        .addParameter(BsonWriter.class, "writer")
        .addParameter(TypeName.get(structInfo.element.asType()), "obj")
        .addStatement("this.write(writer, (byte[]) null, obj)")
        .build();
  }

  protected MethodSpec generateWriteMethodWithKey(StructInfo structInfo) {
    TypeName arrayTypeName = ArrayTypeName.of(byte.class);

    return MethodSpec.methodBuilder("writeValue")
        .addModifiers(Modifier.PUBLIC)
        .addParameter(BsonWriter.class, "writer")
        //        .addParameter(ParameterSpec.builder(arrayTypeName, "key").build())
        .addParameter(TypeName.get(structInfo.element.asType()), "obj")
        //        .beginControlFlow("if (obj == null)")
        //        .beginControlFlow("if (key == null)")
        //        .addStatement("throw new $T(\"key and object cannot be null\")",
        // RuntimeException.class)
        //        .endControlFlow()
        //        .addStatement("writer.writeNull(key)")
        //        .addStatement("return")
        //        .endControlFlow()
        .addCode(generateWriteEnumClauses(structInfo))
        .build();
  }

  private CodeBlock generateReadEnumClauses(StructInfo structInfo) {
    CodeBlock.Builder block = CodeBlock.builder();

    var first = true;
    for (var entry : structInfo.getEnumConstants()) {
      if (first) {
        block.beginControlFlow("if (fieldName.equalsArray($NBytes, $NWeakHash))", entry, entry);
        first = false;
      } else {
        block.nextControlFlow("else if (fieldName.equalsArray($NBytes, $NWeakHash))", entry, entry);
      }
      block.addStatement("return $T.$N", structInfo.element.asType(), entry);
    }
    block
        .nextControlFlow("else")
        .addStatement("return $T.valueOf(fieldName.value())", structInfo.element.asType())
        .endControlFlow();
    return block.build();
  }

  private MethodSpec generateReadMethod(StructInfo structInfo, Elements elements) {
    var type = TypeName.get(structInfo.element.asType());
    return MethodSpec.methodBuilder("readValue")
        .addModifiers(Modifier.PUBLIC)
        .addParameter(BsonReader.class, "reader")
        .addParameter(BsonType.class, "type")
        .returns(type)
        //        .beginControlFlow("if (reader.getCurrentBsonType() == $T.NULL)", BsonType.class)
        //        .addStatement("reader.readNull()")
        //        .addStatement("return null")
        //        .endControlFlow()
        .addStatement("var fieldName = reader.getFieldName()")
        .addStatement("reader.readStringRaw()")
        .addCode(generateReadEnumClauses(structInfo))
        .build();
  }

  public void generate(StructInfo structInfo, Writer writer, Types types, Elements elements)
      throws Exception {
    var typeVars =
        structInfo.element.getTypeParameters().stream()
            .map(TypeVariableName::get)
            .collect(toList());
    var converterName =
        "_" + structInfo.getClassName().replaceAll("\\$", "_") + "_BobBsonConverter";
    ClassName model = ClassName.get(structInfo.element);

    var fields = getEnumFields(structInfo, types, elements);
    // create static keys for all enum values
    // create weak hash for each value
    // create read method
    // create write method

    TypeSpec innerStruct =
        TypeSpec.classBuilder("_" + structInfo.getClassName() + "_BobBsonConverter")
            .addTypeVariables(typeVars)
            .addAnnotation(
                AnnotationSpec.builder(SuppressWarnings.class)
                    .addMember("value", "$S", "unchecked")
                    .build())
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addField(BobBson.class, "bobBson", Modifier.PRIVATE)
            .addFields(fields)
            //                    .addFields(keyByteArrays)
            //                    .addMethods(lookupData.lookupMethods.values())
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
            .addMethod(generateReadMethod(structInfo, elements))
            //            .addMethod(generateWriteMethodNoKey(structInfo))
            .addMethod(generateWriteMethodWithKey(structInfo))
            .build();

    TypeSpec struct =
        TypeSpec.classBuilder("_" + structInfo.getClassName() + "_BobBsonConverterRegister")
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(ClassName.get(BobBsonConverterRegister.class))
            .addMethod(ParserGenerator.getRegisterMethod(structInfo.getClassName(), model))
            .addType(innerStruct)
            .build();

    JavaFile javaFile = JavaFile.builder(structInfo.getPackageName(), struct).indent("  ").build();

    javaFile.writeTo(writer);
  }
}
