package org.bobstuff.bobbson.processor;

import static org.junit.jupiter.api.Assertions.*;

import com.karuslabs.elementary.junit.Cases;
import com.karuslabs.elementary.junit.Tools;
import com.karuslabs.elementary.junit.ToolsExtension;
import com.karuslabs.elementary.junit.annotations.Case;
import com.karuslabs.elementary.junit.annotations.Introspect;
import java.util.HashSet;
import java.util.List;
import org.bobstuff.bobbson.annotations.BsonAttribute;
import org.bobstuff.bobbson.annotations.BsonConverter;
import org.bobstuff.bobbson.annotations.GenerateBobBsonConverter;
import org.bobstuff.bobbson.converters.StringBsonConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ToolsExtension.class)
@Introspect
public class AnalysisTest {
  @Test
  void testSimpleAnalysis(Cases cases) {
    var sample = cases.one("first");

    var sut =
        new Analysis(Tools.types(), Tools.elements(), new BobMessager(Tools.messager(), false));
    var result = sut.analyse(new HashSet<>(List.of(sample)));
    var si = result.get("org.bobstuff.bobbson.processor.AnalysisTest.Sample");

    assertEquals("org.bobstuff.bobbson.processor.AnalysisTest$Sample", si.binaryName);
    assertEquals(
        "org.bobstuff.bobbson.annotations.GenerateBobBsonConverter",
        si.annotation.getAnnotationType().toString());
    assertEquals(
        "org.bobstuff.bobbson.annotations.GenerateBobBsonConverter", si.discoveredBy.toString());

    var attributes = si.attributes;
    assertTrue(attributes.containsKey("name"));
    var name = attributes.get("name");
    assertEquals("getName", name.getReadMethod().getSimpleName().toString());
    assertEquals("java.lang.String", name.getReadMethod().getReturnType().toString());
    assertEquals("setName", name.getWriteMethod().getSimpleName().toString());
    assertEquals(
        "java.lang.String", name.getWriteMethod().getParameters().get(0).asType().toString());
    assertEquals("void", name.getWriteMethod().getReturnType().toString());
    assertEquals(FieldType.OBJECT, name.fieldType);
    assertNull(name.getConverterType());
  }

  @Test
  void testSimpleListAnalysis(Cases cases) {
    var sample = cases.one("second");

    var sut =
        new Analysis(Tools.types(), Tools.elements(), new BobMessager(Tools.messager(), false));
    var result = sut.analyse(new HashSet<>(List.of(sample)));
    var si = result.get("org.bobstuff.bobbson.processor.AnalysisTest.SampleList");

    assertEquals("org.bobstuff.bobbson.processor.AnalysisTest$SampleList", si.binaryName);
    assertEquals(
        "org.bobstuff.bobbson.annotations.GenerateBobBsonConverter",
        si.annotation.getAnnotationType().toString());
    assertEquals(
        "org.bobstuff.bobbson.annotations.GenerateBobBsonConverter", si.discoveredBy.toString());

    var attributes = si.attributes;
    assertTrue(attributes.containsKey("names"));
    var name = attributes.get("names");
    assertEquals("getNames", name.getReadMethod().getSimpleName().toString());
    assertEquals(
        "java.util.List<java.lang.String>", name.getReadMethod().getReturnType().toString());
    assertEquals("setNames", name.getWriteMethod().getSimpleName().toString());
    assertEquals(
        "java.util.List<java.lang.String>",
        name.getWriteMethod().getParameters().get(0).asType().toString());
    assertEquals("void", name.getWriteMethod().getReturnType().toString());
    assertEquals(FieldType.LIST, name.fieldType);
    //    assertNull(name.annotation);
    //    assertNull(name.converter);
    //    assertNull(name.converterType);
  }

  @Test
  public void testSimpleAliasAnalysis(Cases cases) {
    var sample = cases.one("alias");

    var sut =
        new Analysis(Tools.types(), Tools.elements(), new BobMessager(Tools.messager(), false));
    var result = sut.analyse(new HashSet<>(List.of(sample)));
    var si = result.get("org.bobstuff.bobbson.processor.AnalysisTest.SampleAlias");

    assertEquals("org.bobstuff.bobbson.processor.AnalysisTest$SampleAlias", si.binaryName);
    assertEquals(
        "org.bobstuff.bobbson.annotations.GenerateBobBsonConverter",
        si.annotation.getAnnotationType().toString());
    assertEquals(
        "org.bobstuff.bobbson.annotations.GenerateBobBsonConverter", si.discoveredBy.toString());

    var attributes = si.attributes;
    assertTrue(attributes.containsKey("name"));
    var name = attributes.get("name");
    assertEquals("getName", name.getReadMethod().getSimpleName().toString());
    assertEquals("java.lang.String", name.getReadMethod().getReturnType().toString());
    assertEquals("setName", name.getWriteMethod().getSimpleName().toString());
    assertEquals(
        "java.lang.String", name.getWriteMethod().getParameters().get(0).asType().toString());
    assertEquals("void", name.getWriteMethod().getReturnType().toString());
    assertEquals("getName", name.getReadMethod().getSimpleName().toString());
    assertEquals("notcalledname", name.getAliasName());
    //    assertFalse(name.list);
    //    assertFalse(name.set);
    //    assertFalse(name.map);
    //    assertNotNull(name.annotation);
    //    assertNull(name.converter);
    //    assertNull(name.converterType);
  }

  @Test
  public void testIsIsGetterByLombok(Cases cases) {
    var sample = cases.one("isiscase");

    var sut =
        new Analysis(Tools.types(), Tools.elements(), new BobMessager(Tools.messager(), false));
    var result = sut.analyse(new HashSet<>(List.of(sample)));
    var si = result.get("org.bobstuff.bobbson.processor.AnalysisTest.IsIsCase");

    assertEquals("org.bobstuff.bobbson.processor.AnalysisTest$IsIsCase", si.binaryName);
    assertEquals(
        "org.bobstuff.bobbson.annotations.GenerateBobBsonConverter",
        si.annotation.getAnnotationType().toString());
    assertEquals(
        "org.bobstuff.bobbson.annotations.GenerateBobBsonConverter", si.discoveredBy.toString());

    var attributes = si.attributes;
    assertTrue(attributes.containsKey("isName"));
    var name = attributes.get("isName");
    assertEquals("isName", name.getReadMethod().getSimpleName().toString());
    assertEquals("boolean", name.getReadMethod().getReturnType().toString());
    assertEquals("setName", name.getWriteMethod().getSimpleName().toString());
    assertEquals("boolean", name.getWriteMethod().getParameters().get(0).asType().toString());
    assertEquals("void", name.getWriteMethod().getReturnType().toString());
    //    assertFalse(name.list);
    //    assertFalse(name.set);
    //    assertFalse(name.map);
    //    assertNull(name.annotation);
    //    assertNull(name.converter);
    //    assertNull(name.converterType);
  }

  @Test
  public void testSimpleConverterAnalysis(Cases cases) {
    var sample = cases.one("converter");

    var sut =
        new Analysis(Tools.types(), Tools.elements(), new BobMessager(Tools.messager(), false));
    var result = sut.analyse(new HashSet<>(List.of(sample)));
    var si = result.get("org.bobstuff.bobbson.processor.AnalysisTest.SampleConverter");

    assertEquals("org.bobstuff.bobbson.processor.AnalysisTest$SampleConverter", si.binaryName);
    assertEquals(
        "org.bobstuff.bobbson.annotations.GenerateBobBsonConverter",
        si.annotation.getAnnotationType().toString());
    assertEquals(
        "org.bobstuff.bobbson.annotations.GenerateBobBsonConverter", si.discoveredBy.toString());

    var attributes = si.attributes;
    assertTrue(attributes.containsKey("name"));
    var name = attributes.get("name");
    assertEquals("getName", name.getReadMethod().getSimpleName().toString());
    assertEquals("java.lang.String", name.getReadMethod().getReturnType().toString());
    assertEquals("setName", name.getWriteMethod().getSimpleName().toString());
    assertEquals(
        "java.lang.String", name.getWriteMethod().getParameters().get(0).asType().toString());
    assertEquals("void", name.getWriteMethod().getReturnType().toString());
    assertEquals("getName", name.getReadMethod().getSimpleName().toString());
    assertEquals("name", name.getAliasName());
    //    assertFalse(name.list);
    //    assertFalse(name.set);
    //    assertFalse(name.map);
    //    assertNull(name.annotation);
    //    assertNotNull(name.converter);
    //    assertNotNull(name.converterType);
    //    assertEquals(
    //
    // "@org.bobstuff.bobbson.annotations.BsonConverter(org.bobstuff.bobbson.converters.StringBsonConverter.class)",
    //        name.converter.toString());
    //    assertEquals(
    //        "org.bobstuff.bobbson.converters.StringBsonConverter", name.converterType.toString());
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

  @Case("second")
  @GenerateBobBsonConverter
  static class SampleList {
    private List<String> names;

    public List<String> getNames() {
      return names;
    }

    public void setNames(List<String> names) {
      this.names = names;
    }
  }

  @Case("alias")
  @GenerateBobBsonConverter
  static class SampleAlias {
    @BsonAttribute("notcalledname")
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
    @BsonConverter(value = StringBsonConverter.class)
    private String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  @Case("isiscase")
  @GenerateBobBsonConverter
  static class IsIsCase {
    private boolean isName;

    public boolean isName() {
      return isName;
    }

    public void setName(boolean name) {
      this.isName = name;
    }
  }
}
