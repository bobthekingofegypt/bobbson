package org.bobstuff.bobbson.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.karuslabs.elementary.junit.Cases;
import com.karuslabs.elementary.junit.Tools;
import com.karuslabs.elementary.junit.ToolsExtension;
import com.karuslabs.elementary.junit.annotations.Case;
import com.karuslabs.elementary.junit.annotations.Introspect;
import com.squareup.javapoet.ClassName;
import java.net.URL;
import java.util.*;
import javax.lang.model.element.Element;
import org.bobstuff.bobbson.annotations.BsonConverter;
import org.bobstuff.bobbson.annotations.CompiledBson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ToolsExtension.class)
@Introspect
public class ParserGeneratorTest {
  Element sample;
  Element sampleMultipleFields;
  Element sampleCollections;
  Element sampleConverter;
  StructInfo sampleStructInfo;
  StructInfo sampleMultipleFieldsStructInfo;
  StructInfo sampleCollectionsStructInfo;
  StructInfo sampleConverterStructInfo;
  ParserGenerator sut;

  @BeforeEach
  public void setUp(Cases cases) {
    sample = cases.one("first");
    sampleMultipleFields = cases.one("multipleFields");
    sampleCollections = cases.one("collections");
    sampleConverter = cases.one("converter");
    var analysis =
        new Analysis(Tools.types(), Tools.elements(), new BobMessager(Tools.messager(), false));
    var result =
        analysis.analyse(
            new HashSet<>(
                List.of(sample, sampleMultipleFields, sampleConverter, sampleCollections)));
    sampleStructInfo = result.get("org.bobstuff.bobbson.processor.ParserGeneratorTest.Sample");
    sampleMultipleFieldsStructInfo =
        result.get("org.bobstuff.bobbson.processor.ParserGeneratorTest.SampleMultipleFields");
    sampleCollectionsStructInfo =
        result.get("org.bobstuff.bobbson.processor.ParserGeneratorTest.SampleCollections");
    sampleConverterStructInfo =
        result.get("org.bobstuff.bobbson.processor.ParserGeneratorTest.SampleConverter");

    sut = new ParserGenerator();
  }
  //
  //  @Test
  //  public void testWriteSimpleClass(Cases cases) throws Exception {
  //    var sample = cases.one("first");
  //    var analysis =
  //        new Analysis(Tools.types(), Tools.elements(), new BobMessager(Tools.messager(), false));
  //    var result = analysis.analyse(new HashSet<>(List.of(sample)));
  //    var si = result.get("org.bobstuff.bobbson.processor.ParserGeneratorTest.Sample");
  //
  //    var writer = new StringWriter();
  //    var sut = new ParserGenerator();
  //    sut.generate(si, writer, Tools.types(), Tools.elements());
  //    System.out.print(writer.toString());
  //  }

  @Test
  public void testGenerateKeyByteArrays() {
    var results = sut.generateKeyByteArrays(sampleStructInfo);
    assertEquals(
        """
                    private byte[] nameBytes = "name".getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    """,
        results.get(0).toString());
  }

  @Test
  public void testGenerateKeyByteArraysMultipleFields() {
    var results = sut.generateKeyByteArrays(sampleMultipleFieldsStructInfo);
    assertEquals(
        """
                    private byte[] nameBytes = "name".getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    """,
        results.get(1).toString());
    assertEquals(
        """
                    private byte[] ageBytes = "age".getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    """,
        results.get(2).toString());
    assertEquals(
        """
                    private byte[] occupationBytes = "occupation".getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    """,
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
     var map = new java.util.HashMap<java.lang.String, java.lang.String>();
     reader.readStartDocument();
     var type_i = org.bobstuff.bobbson.BsonType.NOT_SET;
     while ((type_i = reader.readBsonType()) != org.bobstuff.bobbson.BsonType.END_OF_DOCUMENT) {
       map.put(reader.currentFieldName(), converter_java_lang_String().read(reader));
     }
     reader.readEndDocument();
     result.setMap(map);
                         """,
        code.toString());
  }

  @Test
  public void testGenerateParserMapCodeDouble() {
    var code = sut.generateParserMapCode(sampleCollectionsStructInfo.attributes.get("mapDouble"));
    assertEquals(
        """
     var map = new java.util.HashMap<java.lang.String, java.lang.Double>();
     reader.readStartDocument();
     var type_i = org.bobstuff.bobbson.BsonType.NOT_SET;
     while ((type_i = reader.readBsonType()) != org.bobstuff.bobbson.BsonType.END_OF_DOCUMENT) {
       map.put(reader.currentFieldName(), converter_java_lang_Double().read(reader));
     }
     reader.readEndDocument();
     result.setMapDouble(map);
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
     var list = new java.util.ArrayList<java.lang.String>();
     reader.readStartArray();
     var type_i = org.bobstuff.bobbson.BsonType.NOT_SET;
     while ((type_i = reader.readBsonType()) != org.bobstuff.bobbson.BsonType.END_OF_DOCUMENT) {
       list.add(converter_java_lang_String().read(reader));
     }
     reader.readEndArray();
     result.setList(list);
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
     var list = new java.util.ArrayList<java.lang.Double>();
     reader.readStartArray();
     var type_i = org.bobstuff.bobbson.BsonType.NOT_SET;
     while ((type_i = reader.readBsonType()) != org.bobstuff.bobbson.BsonType.END_OF_DOCUMENT) {
       list.add(converter_java_lang_Double().read(reader));
     }
     reader.readEndArray();
     result.setListDouble(list);
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
     var list = new java.util.HashSet<java.lang.String>();
     reader.readStartArray();
     var type_i = org.bobstuff.bobbson.BsonType.NOT_SET;
     while ((type_i = reader.readBsonType()) != org.bobstuff.bobbson.BsonType.END_OF_DOCUMENT) {
       list.add(converter_java_lang_String().read(reader));
     }
     reader.readEndArray();
     result.setSet(list);
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
     var list = new java.util.HashSet<java.lang.Double>();
     reader.readStartArray();
     var type_i = org.bobstuff.bobbson.BsonType.NOT_SET;
     while ((type_i = reader.readBsonType()) != org.bobstuff.bobbson.BsonType.END_OF_DOCUMENT) {
       list.add(converter_java_lang_Double().read(reader));
     }
     reader.readEndArray();
     result.setSetDouble(list);
                         """,
        code.toString());
  }

  @Test
  public void testGenerateParserCode() {
    var code = sut.generateParserCode(sampleStructInfo);
    assertEquals(
        """
                    var range = reader.getFieldName();
                    if (!nameCheck && range.equalsArray(nameBytes, 417)) {
                      nameCheck = readAllValues ? false : true;
                      result.setName(converter_java_lang_String().read(reader));
                    } else {
                      reader.skipValue();
                    }
                    if (!readAllValues && nameCheck) {
                      reader.skipContext();
                      break;
                    }
                    """,
        code.toString());
  }

  @Test
  public void testGenerateParserCodeMultipleFields() {
    var code = sut.generateParserCode(sampleMultipleFieldsStructInfo);
    assertEquals(
        """
                    var range = reader.getFieldName();
                    if (!occupationCheck && range.equalsArray(occupationBytes, 1077)) {
                      occupationCheck = readAllValues ? false : true;
                      result.setOccupation(converter_java_lang_String().read(reader));
                    } else if (!nameCheck && range.equalsArray(nameBytes, 417)) {
                      nameCheck = readAllValues ? false : true;
                      result.setName(converter_java_lang_String().read(reader));
                    } else if (!ageCheck && range.equalsArray(ageBytes, 301)) {
                      ageCheck = readAllValues ? false : true;
                      result.setAge(converter_int().read(reader));
                    } else {
                      reader.skipValue();
                    }
                    if (!readAllValues && occupationCheck && nameCheck && ageCheck) {
                      reader.skipContext();
                      break;
                    }
                        """,
        code.toString());
  }

  @Test
  public void testGenerateParserCodeCollectionFields() {
    var code = sut.generateParserCode(sampleCollectionsStructInfo);
    assertEquals(
        """
      var range = reader.getFieldName();
      if (!setDoubleCheck && range.equalsArray(setDoubleBytes, 935)) {
        setDoubleCheck = readAllValues ? false : true;
        var list = new java.util.HashSet<java.lang.Double>();
        reader.readStartArray();
        var type_i = org.bobstuff.bobbson.BsonType.NOT_SET;
        while ((type_i = reader.readBsonType()) != org.bobstuff.bobbson.BsonType.END_OF_DOCUMENT) {
          list.add(converter_java_lang_Double().read(reader));
        }
        reader.readEndArray();
        result.setSetDouble(list);
      } else if (!setCheck && range.equalsArray(setBytes, 332)) {
        setCheck = readAllValues ? false : true;
        var list = new java.util.HashSet<java.lang.String>();
        reader.readStartArray();
        var type_i = org.bobstuff.bobbson.BsonType.NOT_SET;
        while ((type_i = reader.readBsonType()) != org.bobstuff.bobbson.BsonType.END_OF_DOCUMENT) {
          list.add(converter_java_lang_String().read(reader));
        }
        reader.readEndArray();
        result.setSet(list);
      } else if (!mapDoubleCheck && range.equalsArray(mapDoubleBytes, 921)) {
        mapDoubleCheck = readAllValues ? false : true;
        var map = new java.util.HashMap<java.lang.String, java.lang.Double>();
        reader.readStartDocument();
        var type_i = org.bobstuff.bobbson.BsonType.NOT_SET;
        while ((type_i = reader.readBsonType()) != org.bobstuff.bobbson.BsonType.END_OF_DOCUMENT) {
          map.put(reader.currentFieldName(), converter_java_lang_Double().read(reader));
        }
        reader.readEndDocument();
        result.setMapDouble(map);
      } else if (!listCheck && range.equalsArray(listBytes, 444)) {
        listCheck = readAllValues ? false : true;
        var list = new java.util.ArrayList<java.lang.String>();
        reader.readStartArray();
        var type_i = org.bobstuff.bobbson.BsonType.NOT_SET;
        while ((type_i = reader.readBsonType()) != org.bobstuff.bobbson.BsonType.END_OF_DOCUMENT) {
          list.add(converter_java_lang_String().read(reader));
        }
        reader.readEndArray();
        result.setList(list);
      } else if (!listDoubleCheck && range.equalsArray(listDoubleBytes, 1047)) {
        listDoubleCheck = readAllValues ? false : true;
        var list = new java.util.ArrayList<java.lang.Double>();
        reader.readStartArray();
        var type_i = org.bobstuff.bobbson.BsonType.NOT_SET;
        while ((type_i = reader.readBsonType()) != org.bobstuff.bobbson.BsonType.END_OF_DOCUMENT) {
          list.add(converter_java_lang_Double().read(reader));
        }
        reader.readEndArray();
        result.setListDouble(list);
      } else if (!mapCheck && range.equalsArray(mapBytes, 318)) {
        mapCheck = readAllValues ? false : true;
        var map = new java.util.HashMap<java.lang.String, java.lang.String>();
        reader.readStartDocument();
        var type_i = org.bobstuff.bobbson.BsonType.NOT_SET;
        while ((type_i = reader.readBsonType()) != org.bobstuff.bobbson.BsonType.END_OF_DOCUMENT) {
          map.put(reader.currentFieldName(), converter_java_lang_String().read(reader));
        }
        reader.readEndDocument();
        result.setMap(map);
      } else {
        reader.skipValue();
      }
      if (!readAllValues && setDoubleCheck && setCheck && mapDoubleCheck && listCheck && listDoubleCheck && mapCheck) {
        reader.skipContext();
        break;
      }
                        """,
        code.toString());
  }

  @Test
  public void testGenerateParserCodeConverter() {
    var code = sut.generateParserCode(sampleConverterStructInfo);
    assertEquals(
        """
                    var range = reader.getFieldName();
                    if (!nameCheck && range.equalsArray(nameBytes, 417)) {
                      nameCheck = readAllValues ? false : true;
                      result.setName(converter_name.read(reader));
                    } else {
                      reader.skipValue();
                    }
                    if (!readAllValues && nameCheck) {
                      reader.skipContext();
                      break;
                    }
                        """,
        code.toString());
  }

  @Test
  public void testGenerateReadMethod() {
    var code = sut.generateReadMethod(sampleStructInfo, Tools.types());
    assertEquals(
        """
      public org.bobstuff.bobbson.processor.ParserGeneratorTest.Sample read(
          org.bobstuff.bobbson.BsonReader reader) {
        if (reader.getCurrentBsonType() == BsonType.NULL) {
          reader.readNull();
          return null;
        }
        org.bobstuff.bobbson.processor.ParserGeneratorTest.Sample result = new org.bobstuff.bobbson.processor.ParserGeneratorTest.Sample();
        reader.readStartDocument();
        var type = org.bobstuff.bobbson.BsonType.NOT_SET;
        var readAllValues = false;
        boolean nameCheck = false;
        while ((type = reader.readBsonType()) != org.bobstuff.bobbson.BsonType.END_OF_DOCUMENT) {
          var range = reader.getFieldName();
          if (!nameCheck && range.equalsArray(nameBytes, 417)) {
            nameCheck = readAllValues ? false : true;
            result.setName(converter_java_lang_String().read(reader));
          } else {
            reader.skipValue();
          }
          if (!readAllValues && nameCheck) {
            reader.skipContext();
            break;
          }
        }
        reader.readEndDocument();
        return result;
      }
                        """,
        code.toString());
  }

  @Test
  public void testGenerateWriteMethodNoKey() {
    var result = sut.generateWriteMethodNoKey(ClassName.get(String.class));
    assertEquals(
        """
       public void write(org.bobstuff.bobbson.writer.BsonWriter writer, java.lang.String obj) {
         this.write(writer, (byte[]) null, obj);
       }
                         """,
        result.toString());
  }

  @Test
  public void testGenerateWriteMethodNoKeyDifferentType() {
    var result = sut.generateWriteMethodNoKey(ClassName.get(URL.class));
    assertEquals(
        """
       public void write(org.bobstuff.bobbson.writer.BsonWriter writer, java.net.URL obj) {
         this.write(writer, (byte[]) null, obj);
       }
                         """,
        result.toString());
  }

  @Test
  public void testGenerateWriterMapCode() {
    var result = sut.generateWriterMapCode(sampleCollectionsStructInfo.attributes.get("map"));
    assertEquals(
        """
       writer.writeStartDocument(mapBytes);
       for (var e : obj.getMap().entrySet()) {
         converter_java_lang_String().write(writer, e.getKey(), e.getValue());
       }
       writer.writeEndDocument();
                         """,
        result.toString());
  }

  @Test
  public void testGenerateWriterMapDoubleCode() {
    var result = sut.generateWriterMapCode(sampleCollectionsStructInfo.attributes.get("mapDouble"));
    assertEquals(
        """
       writer.writeStartDocument(mapDoubleBytes);
       for (var e : obj.getMapDouble().entrySet()) {
         converter_java_lang_Double().write(writer, e.getKey(), e.getValue());
       }
       writer.writeEndDocument();
                         """,
        result.toString());
  }

  @Test
  public void testGenerateWriterListCode() {
    var result =
        sut.generateWriterCollectionCode(sampleCollectionsStructInfo.attributes.get("list"));
    assertEquals(
        """
                         writer.writeStartArray(listBytes);
                         for (var e : obj.getList()) {
                           converter_java_lang_String().write(writer, e);
                         }
                         writer.writeEndArray();
                                           """,
        result.toString());
  }

  @Test
  public void testGenerateWriterListDoubleCode() {
    var result =
        sut.generateWriterCollectionCode(sampleCollectionsStructInfo.attributes.get("listDouble"));
    assertEquals(
        """
                         writer.writeStartArray(listDoubleBytes);
                         for (var e : obj.getListDouble()) {
                           converter_java_lang_Double().write(writer, e);
                         }
                         writer.writeEndArray();
                                           """,
        result.toString());
  }

  @Test
  public void testGenerateWriterSetCode() {
    var result =
        sut.generateWriterCollectionCode(sampleCollectionsStructInfo.attributes.get("set"));
    assertEquals(
        """
                         writer.writeStartArray(setBytes);
                         for (var e : obj.getSet()) {
                           converter_java_lang_String().write(writer, e);
                         }
                         writer.writeEndArray();
                                           """,
        result.toString());
  }

  @Test
  public void testGenerateWriterSetDoubleCode() {
    var result =
        sut.generateWriterCollectionCode(sampleCollectionsStructInfo.attributes.get("setDouble"));
    assertEquals(
        """
                         writer.writeStartArray(setDoubleBytes);
                         for (var e : obj.getSetDouble()) {
                           converter_java_lang_Double().write(writer, e);
                         }
                         writer.writeEndArray();
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
                         public void write(org.bobstuff.bobbson.writer.BsonWriter writer, byte[] key,
                             org.bobstuff.bobbson.processor.ParserGeneratorTest.Sample obj) {
                           if (obj == null) {
                             writer.writeNull(key);
                             return;
                           }
                           if (key != null) {
                             writer.writeStartDocument(key);
                           } else {
                             writer.writeStartDocument();
                           }
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
        result.lookupMethods.get(Tools.typeMirrors().type(String.class)).toString());
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
        result.lookupMethods.get(Tools.typeMirrors().type(String.class)).toString());
    assertEquals(
        """
                         public org.bobstuff.bobbson.BobBsonConverter<java.lang.Double> converter_java_lang_Double() {
                           if (converter_java_lang_Double == null) {
                             converter_java_lang_Double = (org.bobstuff.bobbson.BobBsonConverter<java.lang.Double>) bobBson.tryFindConverter(java.lang.Double.class);
                           }
                           return converter_java_lang_Double;
                         }
                         """,
        result.lookupMethods.get(Tools.typeMirrors().type(Double.class)).toString());
  }

  @Case("first")
  @CompiledBson
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
  @CompiledBson
  static class SampleConverter {
    @BsonConverter(target = CustomConverter.class)
    private String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  @Case("multipleFields")
  @CompiledBson
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

  @Case("collections")
  @CompiledBson
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
