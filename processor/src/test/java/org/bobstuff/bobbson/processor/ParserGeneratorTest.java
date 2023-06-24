package org.bobstuff.bobbson.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.karuslabs.elementary.junit.Cases;
import com.karuslabs.elementary.junit.Tools;
import com.karuslabs.elementary.junit.ToolsExtension;
import com.karuslabs.elementary.junit.annotations.Case;
import com.karuslabs.elementary.junit.annotations.Introspect;
import com.squareup.javapoet.ClassName;
import java.util.*;
import javax.lang.model.element.Element;
import org.bobstuff.bobbson.annotations.BsonAttribute;
import org.bobstuff.bobbson.annotations.BsonConverter;
import org.bobstuff.bobbson.annotations.GenerateBobBsonConverter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ToolsExtension.class)
@Introspect
public class ParserGeneratorTest {
  Element sample;
  Element sampleMultipleFields;

  Element sampleMultipleFieldsOrderedOne;
  Element sampleMultipleFieldsOrderedTwo;
  Element sampleCollections;
  Element sampleConverter;
  Element sampleGenericClass;
  Element multipleListNonListDepBug;
  StructInfo sampleStructInfo;
  StructInfo sampleMultipleFieldsStructInfo;
  StructInfo sampleMultipleFieldsOrderedOneStructInfo;
  StructInfo sampleMultipleFieldsOrderedTwoStructInfo;
  StructInfo sampleCollectionsStructInfo;
  StructInfo sampleConverterStructInfo;
  StructInfo multipleListNonListDepBugStructInfo;
  StructInfo sampleGenericClassStructInfo;
  ParserGenerator sut;

  @BeforeEach
  public void setUp(Cases cases) {
    sample = cases.one("first");
    sampleMultipleFields = cases.one("multipleFields");
    sampleMultipleFieldsOrderedOne = cases.one("multipleFieldsOrdered1");
    sampleMultipleFieldsOrderedTwo = cases.one("multipleFieldsOrdered2");
    sampleGenericClass = cases.one("genericClass");
    multipleListNonListDepBug = cases.one("multipleListNonListDepBug");
    sampleCollections = cases.one("collections");
    sampleConverter = cases.one("converter");
    var messager = new BobMessager(Tools.messager(), false);
    var analysis = new Analysis(Tools.types(), Tools.elements(), messager);
    var result =
        analysis.analyse(
            new HashSet<>(
                List.of(
                    sample,
                    sampleMultipleFields,
                    sampleConverter,
                    sampleCollections,
                    sampleMultipleFieldsOrderedOne,
                    sampleMultipleFieldsOrderedTwo,
                    sampleGenericClass,
                    multipleListNonListDepBug)));
    sampleStructInfo = result.get("org.bobstuff.bobbson.processor.ParserGeneratorTest.Sample");
    sampleMultipleFieldsStructInfo =
        result.get("org.bobstuff.bobbson.processor.ParserGeneratorTest.SampleMultipleFields");
    sampleMultipleFieldsOrderedOneStructInfo =
        result.get(
            "org.bobstuff.bobbson.processor.ParserGeneratorTest.SampleMultipleFieldsOrdered1");
    sampleMultipleFieldsOrderedTwoStructInfo =
        result.get(
            "org.bobstuff.bobbson.processor.ParserGeneratorTest.SampleMultipleFieldsOrdered2");
    sampleCollectionsStructInfo =
        result.get("org.bobstuff.bobbson.processor.ParserGeneratorTest.SampleCollections");
    sampleConverterStructInfo =
        result.get("org.bobstuff.bobbson.processor.ParserGeneratorTest.SampleConverter");
    sampleGenericClassStructInfo =
        result.get("org.bobstuff.bobbson.processor.ParserGeneratorTest.SampleGenericClass");
    multipleListNonListDepBugStructInfo =
        result.get(
            "org.bobstuff.bobbson.processor.ParserGeneratorTest.SampleMultipleListNonListDepBug");

    sut = new ParserGenerator(messager);
  }

  @Test
  public void testGenerateKeyByteArrays() {
    var results = sut.generateKeyByteArrays(sampleStructInfo);
    assertEquals(
        "private byte[] nameBytes = \"name\".getBytes(java.nio.charset.StandardCharsets.UTF_8);\n",
        results.get(0).toString());
  }

  @Test
  public void testGenerateKeyByteArraysMultipleFields() {
    var results = sut.generateKeyByteArrays(sampleMultipleFieldsStructInfo);
    assertEquals(
        "private byte[] nameBytes = \"name\".getBytes(java.nio.charset.StandardCharsets.UTF_8);\n",
        results.get(1).toString());
    assertEquals(
        "private byte[] ageBytes = \"age\".getBytes(java.nio.charset.StandardCharsets.UTF_8);\n",
        results.get(2).toString());
    assertEquals(
        "private byte[] occupationBytes ="
            + " \"occupation\".getBytes(java.nio.charset.StandardCharsets.UTF_8);\n",
        results.get(0).toString());
  }

  @Test
  public void testGenerateParserPreamble() {
    var results = sut.generateParserPreamble(sampleStructInfo, Tools.types());
    assertEquals(
        """
                    var readAllValues = false;
                    boolean nameCheck = false;
                      """,
        results.toString());
  }

  @Test
  public void testGenerateParserPreambleMultipleFields() {
    var results = sut.generateParserPreamble(sampleMultipleFieldsStructInfo, Tools.types());
    assertEquals(
        """
                    var readAllValues = false;
                    boolean occupationCheck = false;
                    boolean nameCheck = false;
                    boolean ageCheck = false;
                      """,
        results.toString());
  }

  @Test
  public void testGenerateParserMapCode() {
    var code = sut.generateParserMapCode(sampleCollectionsStructInfo.attributes.get("map"));
    assertEquals(
        """
   result.setMap(org.bobstuff.bobbson.converters.CollectionConverters.readMap(reader, type, converter_java_lang_String()));
                         """,
        code.toString());
  }

  @Test
  public void testGenerateParserMapCodeDouble() {
    var code = sut.generateParserMapCode(sampleCollectionsStructInfo.attributes.get("mapDouble"));
    assertEquals(
        """
   result.setMapDouble(org.bobstuff.bobbson.converters.CollectionConverters.readMap(reader, type, converter_java_lang_Double()));
                         """,
        code.toString());
  }

  @Test
  public void testGenerateParserCollectionCode() {
    var code =
        sut.generateParserCollectionCode(
            ArrayList.class, sampleCollectionsStructInfo.attributes.get("list"));
    assertEquals(
        """
      result.setList(org.bobstuff.bobbson.converters.CollectionConverters.readList(reader, type, converter_java_lang_String()));
                         """,
        code.toString());
  }

  @Test
  public void testGenerateParserCollectionCodeListDouble() {
    var code =
        sut.generateParserCollectionCode(
            ArrayList.class, sampleCollectionsStructInfo.attributes.get("listDouble"));
    assertEquals(
        """
    result.setListDouble(org.bobstuff.bobbson.converters.CollectionConverters.readList(reader, type, converter_java_lang_Double()));
                         """,
        code.toString());
  }

  @Test
  public void testGenerateParserCollectionCodeSet() {
    var code =
        sut.generateParserCollectionCode(
            HashSet.class, sampleCollectionsStructInfo.attributes.get("set"));
    assertEquals(
        """
    result.setSet(org.bobstuff.bobbson.converters.CollectionConverters.readList(reader, type, converter_java_lang_String()));
                         """,
        code.toString());
  }

  @Test
  public void testGenerateParserCollectionCodeSetDouble() {
    var code =
        sut.generateParserCollectionCode(
            HashSet.class, sampleCollectionsStructInfo.attributes.get("setDouble"));
    assertEquals(
        """
    result.setSetDouble(org.bobstuff.bobbson.converters.CollectionConverters.readList(reader, type, converter_java_lang_Double()));
                         """,
        code.toString());
  }

  @Test
  public void testGenerateParserCode() {
    var code = sut.generateParserCode(sampleStructInfo);
    assertEquals(
        """
                    if (range.equalsArray(nameBytes, 417)) {
                      fieldsRead += 1;
                      result.setName(org.bobstuff.bobbson.converters.StringBsonConverter.readString(reader));
                    } else {
                      reader.skipValue();
                    }
                    if (fieldsRead == EXPECTED_FIELD_COUNT) {
                      reader.skipContext();
                      reader.readEndDocument();
                      return result;
                    }
                        """,
        code.toString());
  }

  @Test
  public void testGenerateParserCodeMultipleFields() {
    var code = sut.generateParserCode(sampleMultipleFieldsStructInfo);
    assertEquals(
        """
                    if (range.equalsArray(occupationBytes, 1077)) {
                      fieldsRead += 1;
                      result.setOccupation(org.bobstuff.bobbson.converters.StringBsonConverter.readString(reader));
                    } else if (range.equalsArray(nameBytes, 417)) {
                      fieldsRead += 1;
                      result.setName(org.bobstuff.bobbson.converters.StringBsonConverter.readString(reader));
                    } else if (range.equalsArray(ageBytes, 301)) {
                      fieldsRead += 1;
                      result.setAge(org.bobstuff.bobbson.converters.PrimitiveConverters.parseInteger(reader));
                    } else {
                      reader.skipValue();
                    }
                    if (fieldsRead == EXPECTED_FIELD_COUNT) {
                      reader.skipContext();
                      reader.readEndDocument();
                      return result;
                    }
                        """,
        code.toString());
  }

  @Test
  public void testGenerateParserCodeMultipleFieldsOrderedOne() {
    var code = sut.generateParserCode(sampleMultipleFieldsOrderedOneStructInfo);
    assertEquals(
        """
                    if (range.equalsArray(occupationBytes, 1077)) {
                      fieldsRead += 1;
                      result.setOccupation(org.bobstuff.bobbson.converters.StringBsonConverter.readString(reader));
                    } else if (range.equalsArray(ageBytes, 301)) {
                      fieldsRead += 1;
                      result.setAge(org.bobstuff.bobbson.converters.PrimitiveConverters.parseInteger(reader));
                    } else if (range.equalsArray(nameBytes, 417)) {
                      fieldsRead += 1;
                      result.setName(org.bobstuff.bobbson.converters.StringBsonConverter.readString(reader));
                    } else {
                      reader.skipValue();
                    }
                    if (fieldsRead == EXPECTED_FIELD_COUNT) {
                      reader.skipContext();
                      reader.readEndDocument();
                      return result;
                    }
                            """,
        code.toString());
  }

  @Test
  public void testGenerateParserCodeCollectionFields() {
    var code = sut.generateParserCode(sampleCollectionsStructInfo);
    assertEquals(
        """
       if (range.equalsArray(setDoubleBytes, 935)) {
         fieldsRead += 1;
         result.setSetDouble(org.bobstuff.bobbson.converters.CollectionConverters.readSet(reader, type, converter_java_lang_Double()));
       } else if (range.equalsArray(setBytes, 332)) {
         fieldsRead += 1;
         result.setSet(org.bobstuff.bobbson.converters.CollectionConverters.readSet(reader, type, converter_java_lang_String()));
       } else if (range.equalsArray(mapDoubleBytes, 921)) {
         fieldsRead += 1;
         result.setMapDouble(org.bobstuff.bobbson.converters.CollectionConverters.readMap(reader, type, converter_java_lang_Double()));
       } else if (range.equalsArray(listBytes, 444)) {
         fieldsRead += 1;
         result.setList(org.bobstuff.bobbson.converters.CollectionConverters.readList(reader, type, converter_java_lang_String()));
       } else if (range.equalsArray(listDoubleBytes, 1047)) {
         fieldsRead += 1;
         result.setListDouble(org.bobstuff.bobbson.converters.CollectionConverters.readList(reader, type, converter_java_lang_Double()));
       } else if (range.equalsArray(mapBytes, 318)) {
         fieldsRead += 1;
         result.setMap(org.bobstuff.bobbson.converters.CollectionConverters.readMap(reader, type, converter_java_lang_String()));
       } else {
         reader.skipValue();
       }
       if (fieldsRead == EXPECTED_FIELD_COUNT) {
         reader.skipContext();
         reader.readEndDocument();
         return result;
       }
                                """,
        code.toString());
  }

  @Test
  public void testGenerateParserCodeConverter() {
    var code = sut.generateParserCode(sampleConverterStructInfo);
    assertEquals(
        """
                    if (range.equalsArray(nameBytes, 417)) {
                      fieldsRead += 1;
                      result.setName(converter_name.read(reader));
                    } else {
                      reader.skipValue();
                    }
                    if (fieldsRead == EXPECTED_FIELD_COUNT) {
                      reader.skipContext();
                      reader.readEndDocument();
                      return result;
                    }
                            """,
        code.toString());
  }

  @Test
  public void testGenerateWriterCodeMultipleFieldsOrderedOne() {
    var code = sut.generateWriterCode(sampleMultipleFieldsOrderedOneStructInfo);
    assertEquals(
        """
                    converter_java_lang_String().write(writer, occupationBytes,
 obj.getOccupation());
                    writer.writeInteger(ageBytes, obj.getAge());
                    converter_java_lang_String().write(writer, nameBytes, obj.getName());
            """
            .replaceAll("\\s+", ""),
        code.toString().replaceAll("\\s+", ""));
  }

  @Test
  public void testGenerateWriterCodeMutlipleFieldsOrderedTwo() {
    var code = sut.generateWriterCode(sampleMultipleFieldsOrderedTwoStructInfo);
    assertEquals(
        """
                    converter_java_lang_String().write(writer, nameBytes, obj.getName());
                    converter_java_lang_String().write(writer, occupationBytes,
 obj.getOccupation());
                    writer.writeInteger(ageBytes, obj.getAge());
            """
            .replaceAll("\\s+", ""),
        code.toString().replaceAll("\\s+", ""));
  }

  @Test
  public void testGenerateReadMethod() {
    var code = sut.generateReadMethod(sampleStructInfo, Tools.types());
    assertEquals(
        """
                        public org.bobstuff.bobbson.processor.ParserGeneratorTest.Sample readValue(
                            org.bobstuff.bobbson.reader.BsonReader reader, org.bobstuff.bobbson.BsonType outerType) {
                          var fieldsRead = 0;
                          org.bobstuff.bobbson.processor.ParserGeneratorTest.Sample result = new org.bobstuff.bobbson.processor.ParserGeneratorTest.Sample();
                          reader.readStartDocument();
                          var type = org.bobstuff.bobbson.BsonType.NOT_SET;
                          type = reader.readBsonType();
                          var range = reader.getFieldName();
                          if (!range.equalsArray(nameBytes, 417)) {
                            readSlow(reader, result, type, fieldsRead);
                            return result;
                          }
                          fieldsRead += 1;
                          result.setName(converter_java_lang_String().read(reader));
                          type = reader.readBsonType();
                          if (type != org.bobstuff.bobbson.BsonType.END_OF_DOCUMENT) {
                            reader.skipContext();
                          }
                          reader.readEndDocument();
                          return result;
                        }
                                          """,
        code.toString());
  }

  //  @Test
  //  public void testGenerateWriteMethodNoKey() {
  //    var result = sut.generateWriteMethodNoKey(ClassName.get(String.class));
  //    assertEquals(
  //        """
  //       public void write(org.bobstuff.bobbson.writer.BsonWriter writer, java.lang.String obj) {
  //         this.write(writer, (byte[]) null, obj);
  //       }
  //                         """,
  //        result.toString());
  //  }

  //  @Test
  //  public void testGenerateWriteMethodNoKeyDifferentType() {
  //    var result = sut.generateWriteMethodNoKey(ClassName.get(URL.class));
  //    assertEquals(
  //        """
  //       public void write(org.bobstuff.bobbson.writer.BsonWriter writer, java.net.URL obj) {
  //         this.write(writer, (byte[]) null, obj);
  //       }
  //                         """,
  //        result.toString());
  //  }

  @Test
  public void testGenerateWriterMapCode() {
    var result = sut.generateWriterMapCode(sampleCollectionsStructInfo.attributes.get("map"));
    assertEquals(
        """
            writer.writeName(mapBytes);
            org.bobstuff.bobbson.converters.CollectionConverters.writeMap(writer, obj.getMap(), converter_java_lang_String());
                                       """,
        result.toString());
  }

  @Test
  public void testGenerateWriterMapDoubleCode() {
    var result = sut.generateWriterMapCode(sampleCollectionsStructInfo.attributes.get("mapDouble"));
    assertEquals(
        """
        writer.writeName(mapDoubleBytes);
        org.bobstuff.bobbson.converters.CollectionConverters.writeMap(writer, obj.getMapDouble(), converter_java_lang_Double());
                         """,
        result.toString());
  }

  @Test
  public void testGenerateWriterListCode() {
    var result =
        sut.generateWriterCollectionCode(sampleCollectionsStructInfo.attributes.get("list"));
    assertEquals(
        """
            writer.writeName(listBytes);
            org.bobstuff.bobbson.converters.CollectionConverters.writeList(writer, obj.getList(), converter_java_lang_String());
                                               """,
        result.toString());
  }

  @Test
  public void testGenerateWriterListDoubleCode() {
    var result =
        sut.generateWriterCollectionCode(sampleCollectionsStructInfo.attributes.get("listDouble"));
    assertEquals(
        """
        writer.writeName(listDoubleBytes);
        org.bobstuff.bobbson.converters.CollectionConverters.writeList(writer, obj.getListDouble(), converter_java_lang_Double());
                                           """,
        result.toString());
  }

  @Test
  public void testGenerateWriterSetCode() {
    var result =
        sut.generateWriterCollectionCode(sampleCollectionsStructInfo.attributes.get("set"));
    assertEquals(
        """
            writer.writeName(setBytes);
            org.bobstuff.bobbson.converters.CollectionConverters.writeList(writer, obj.getSet(), converter_java_lang_String());
                                               """,
        result.toString());
  }

  @Test
  public void testGenerateWriterSetDoubleCode() {
    var result =
        sut.generateWriterCollectionCode(sampleCollectionsStructInfo.attributes.get("setDouble"));
    assertEquals(
        """
        writer.writeName(setDoubleBytes);
        org.bobstuff.bobbson.converters.CollectionConverters.writeList(writer, obj.getSetDouble(), converter_java_lang_Double());
                                               """,
        result.toString());
  }

  @Test
  public void testGenerateWriteMethodWithKey() {
    var result =
        sut.generateWriteMethodWithKey(
            sampleStructInfo, ClassName.get(Sample.class), Tools.types());
    assertEquals(
        """
                    public void writeValue(org.bobstuff.bobbson.writer.BsonWriter writer,
                        org.bobstuff.bobbson.processor.ParserGeneratorTest.Sample obj) {
                      writer.writeStartDocument();
                      converter_java_lang_String().write(writer, nameBytes, obj.getName());
                      writer.writeEndDocument();
                    }
                                 """,
        result.toString());
  }

  @Test
  public void testGenerateConverterLookupMethods() {
    var result = sut.generateConverterLookupMethods(sampleStructInfo, Tools.types());
    assertEquals(1, result.fields.size());
    assertEquals(
        "private org.bobstuff.bobbson.BobBsonConverter<java.lang.String>"
            + " converter_java_lang_String;",
        result.fields.get(0).toString().strip());
    assertEquals(1, result.lookupMethods.size());
    assertEquals(
        """
    public org.bobstuff.bobbson.BobBsonConverter<java.lang.String> converter_java_lang_String() {
      if (converter_java_lang_String == null) {
        converter_java_lang_String = (org.bobstuff.bobbson.BobBsonConverter<java.lang.String>) bobBson.tryFindConverter(java.lang.String.class);
      }
      return converter_java_lang_String;
    }
                             """,
        result.lookupMethods.get(Tools.typeMirrors().type(String.class).toString()).toString());
  }

  @Test
  public void testGenerateConverterLookupMethodsCollections() {
    var result = sut.generateConverterLookupMethods(sampleCollectionsStructInfo, Tools.types());
    assertEquals(2, result.fields.size());
    assertEquals(
        "private org.bobstuff.bobbson.BobBsonConverter<java.lang.String>"
            + " converter_java_lang_String;",
        result.fields.get(1).toString().strip());
    assertEquals(
        "private org.bobstuff.bobbson.BobBsonConverter<java.lang.Double>"
            + " converter_java_lang_Double;",
        result.fields.get(0).toString().strip());
    assertEquals(2, result.lookupMethods.size());
    assertEquals(
        """
public org.bobstuff.bobbson.BobBsonConverter<java.lang.String> converter_java_lang_String() {
  if (converter_java_lang_String == null) {
    converter_java_lang_String = (org.bobstuff.bobbson.BobBsonConverter<java.lang.String>) bobBson.tryFindConverter(java.lang.String.class);
  }
  return converter_java_lang_String;
}
                         """,
        result.lookupMethods.get(Tools.typeMirrors().type(String.class).toString()).toString());
    assertEquals(
        """
 public org.bobstuff.bobbson.BobBsonConverter<java.lang.Double> converter_java_lang_Double() {
   if (converter_java_lang_Double == null) {
     converter_java_lang_Double = (org.bobstuff.bobbson.BobBsonConverter<java.lang.Double>) bobBson.tryFindConverter(java.lang.Double.class);
   }
   return converter_java_lang_Double;
 }
                         """,
        result.lookupMethods.get(Tools.typeMirrors().type(Double.class).toString()).toString());
  }

  @Test
  public void testGenerateSingleConverterDefOnListNonListBug() {
    var result =
        sut.generateConverterLookupMethods(multipleListNonListDepBugStructInfo, Tools.types());
    assertEquals(1, result.fields.size());
    assertEquals(
        "private org.bobstuff.bobbson.BobBsonConverter<java.lang.String>"
            + " converter_java_lang_String;",
        result.fields.get(0).toString().strip());
  }

  @Case("first")
  @GenerateBobBsonConverter
  static class Sample {
    private String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  @Case("converter")
  @GenerateBobBsonConverter
  static class SampleConverter {
    @BsonConverter(CustomConverter.class)
    private String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  @Case("multipleFields")
  @GenerateBobBsonConverter
  static class SampleMultipleFields {
    private String name;
    private int age;
    private String occupation;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getAge() {
      return age;
    }

    public void setAge(int age) {
      this.age = age;
    }

    public String getOccupation() {
      return occupation;
    }

    public void setOccupation(String occupation) {
      this.occupation = occupation;
    }
  }

  @Case("multipleFieldsOrdered1")
  @GenerateBobBsonConverter
  static class SampleMultipleFieldsOrdered1 {
    @BsonAttribute(value = "name", order = 3)
    private String name;

    @BsonAttribute(value = "age", order = 2)
    private int age;

    @BsonAttribute(value = "occupation", order = 1)
    private String occupation;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getAge() {
      return age;
    }

    public void setAge(int age) {
      this.age = age;
    }

    public String getOccupation() {
      return occupation;
    }

    public void setOccupation(String occupation) {
      this.occupation = occupation;
    }
  }

  @Case("multipleFieldsOrdered2")
  @GenerateBobBsonConverter
  static class SampleMultipleFieldsOrdered2 {
    @BsonAttribute(value = "name", order = 1)
    private String name;

    @BsonAttribute(value = "age", order = 3)
    private int age;

    @BsonAttribute(value = "occupation", order = 2)
    private String occupation;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getAge() {
      return age;
    }

    public void setAge(int age) {
      this.age = age;
    }

    public String getOccupation() {
      return occupation;
    }

    public void setOccupation(String occupation) {
      this.occupation = occupation;
    }
  }

  @Case("multipleListNonListDepBug")
  @GenerateBobBsonConverter
  static class SampleMultipleListNonListDepBug {
    @BsonAttribute(value = "name", order = 1)
    private @Nullable String name;

    @BsonAttribute(value = "names", order = 2)
    private @Nullable List<String> names;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public List<String> getNames() {
      return names;
    }

    public void setNames(List<String> names) {
      this.names = names;
    }
  }

  @Case("genericClass")
  @GenerateBobBsonConverter
  static class SampleGenericClass<TModelCrazyName> {
    @BsonAttribute(value = "name", order = 1)
    private @Nullable String name;

    @BsonAttribute(value = "names", order = 2)
    private @Nullable List<String> names;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public List<String> getNames() {
      return names;
    }

    public void setNames(List<String> names) {
      this.names = names;
    }
  }

  @Case("collections")
  @GenerateBobBsonConverter
  static class SampleCollections {
    private Map<String, String> map;
    private Map<String, Double> mapDouble;
    private Set<String> set;
    private Set<Double> setDouble;
    private List<String> list;
    private List<Double> listDouble;

    public Map<String, String> getMap() {
      return map;
    }

    public void setMap(Map<String, String> map) {
      this.map = map;
    }

    public Set<String> getSet() {
      return set;
    }

    public void setSet(Set<String> set) {
      this.set = set;
    }

    public List<String> getList() {
      return list;
    }

    public void setList(List<String> list) {
      this.list = list;
    }

    public Map<String, Double> getMapDouble() {
      return mapDouble;
    }

    public void setMapDouble(Map<String, Double> mapDouble) {
      this.mapDouble = mapDouble;
    }

    public Set<Double> getSetDouble() {
      return setDouble;
    }

    public void setSetDouble(Set<Double> setDouble) {
      this.setDouble = setDouble;
    }

    public List<Double> getListDouble() {
      return listDouble;
    }

    public void setListDouble(List<Double> listDouble) {
      this.listDouble = listDouble;
    }
  }
}
