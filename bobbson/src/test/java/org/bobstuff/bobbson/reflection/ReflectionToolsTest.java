package org.bobstuff.bobbson.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.Type;
import java.util.Comparator;
import org.bobstuff.bobbson.BobBson;
import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.annotations.BsonAttribute;
import org.bobstuff.bobbson.annotations.BsonConverter;
import org.bobstuff.bobbson.annotations.BsonWriterOptions;
import org.bobstuff.bobbson.converters.IntegerBsonConverter;
import org.bobstuff.bobbson.converters.StringBsonConverter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ReflectionToolsTest {

  @Test
  public void testParseBeanFieldsIgnoresNoSetters() throws Exception {
    var bobBson = Mockito.mock(BobBson.class);
    var result = ReflectionTools.parseBeanFields(NoSetters.class, bobBson);
    assertEquals(0, result.size());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testParseBeanFieldsCustomConverter() throws Exception {
    var bobBson = Mockito.mock(BobBson.class);
    Mockito.when(bobBson.tryFindConverter((Type) String.class))
        .thenReturn((BobBsonConverter) new StringBsonConverter());
    var result = ReflectionTools.parseBeanFields(CustomConverterField.class, bobBson);
    assertEquals(1, result.size());
    var field = result.get(0);
    assertEquals("names", field.getName());
    assertEquals(IntegerBsonConverter.class, field.getConverter().getClass());
    assertEquals(String.class.toString(), field.getClazz().toString());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testParseBeanFieldsOneSetter() throws Exception {
    var bobBson = Mockito.mock(BobBson.class);
    Mockito.when(bobBson.tryFindConverter((Type) String.class))
        .thenReturn((BobBsonConverter) new StringBsonConverter());
    var result = ReflectionTools.parseBeanFields(OneSetter.class, bobBson);
    assertEquals(1, result.size());
    var field = result.get(0);
    assertEquals("name", field.getName());
  }

  @Test
  public void testParseBeanFieldsAlias() throws Exception {
    var bobBson = Mockito.mock(BobBson.class);
    Mockito.when(bobBson.tryFindConverter((Type) String.class))
        .thenReturn((BobBsonConverter) new StringBsonConverter());
    var result = ReflectionTools.parseBeanFields(AliasFields.class, bobBson);
    assertEquals(1, result.size());
    var field = result.get(0);
    assertEquals("notnames", field.getAlias());
  }

  @Test
  public void testParseBeanFieldsDontIncludeTransient() throws Exception {
    var bobBson = Mockito.mock(BobBson.class);
    Mockito.when(bobBson.tryFindConverter((Type) String.class))
        .thenReturn((BobBsonConverter) new StringBsonConverter());
    var result = ReflectionTools.parseBeanFields(TransientField.class, bobBson);
    assertEquals(1, result.size());
    var field = result.get(0);
    assertEquals("name", field.getName());
  }

  @Test
  public void testParseBeanFieldsNoFieldButGetterAnnotated() throws Exception {
    var bobBson = Mockito.mock(BobBson.class);
    Mockito.when(bobBson.tryFindConverter((Type) String.class))
        .thenReturn((BobBsonConverter) new StringBsonConverter());
    var result = ReflectionTools.parseBeanFields(NoFieldAnnotated.class, bobBson);
    assertEquals(2, result.size());
    result.sort(Comparator.comparing(ReflectionField::getName));
    var field = result.get(0);
    assertEquals("happy", field.getName());
    field = result.get(1);
    assertEquals("names", field.getName());
  }

  @Test
  public void testParseBeanFieldsDontWriteNull() throws Exception {
    var bobBson = Mockito.mock(BobBson.class);
    Mockito.when(bobBson.tryFindConverter((Type) String.class))
        .thenReturn((BobBsonConverter) new StringBsonConverter());
    var result = ReflectionTools.parseBeanFields(DontWriteNull.class, bobBson);
    assertEquals(1, result.size());
    var field = result.get(0);
    assertFalse(field.isWriteNull());
  }

  public static class NoSetters {
    private String name;
    private int age;

    public String getName() {
      return name;
    }

    public int getAge() {
      return age;
    }
  }

  public static class TransientField {
    private String name;
    private transient int age;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public void setAge(int age) {
      this.age = age;
    }

    public int getAge() {
      return age;
    }
  }

  public static class OneSetter {
    private String name;
    private int age;

    public String getName() {
      return name;
    }

    public int getAge() {
      return age;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  public static class AliasFields {
    @BsonAttribute("notnames")
    private String names;

    public String getNames() {
      return names;
    }

    public void setNames(String names) {
      this.names = names;
    }
  }

  public static class CustomConverterField {
    @BsonConverter(IntegerBsonConverter.class)
    private String names;

    public String getNames() {
      return names;
    }

    public void setNames(String names) {
      this.names = names;
    }
  }

  public static class DontWriteNull {
    @BsonWriterOptions(writeNull = false)
    private String names;

    public String getNames() {
      return names;
    }

    public void setNames(String names) {
      this.names = names;
    }
  }

  public static class NoFieldAnnotated {
    @BsonAttribute("happy")
    public boolean isHappy() {
      return true;
    }

    public void setHappy(boolean h) {}

    @BsonAttribute("names")
    public String getNames() {
      return "";
    }

    public void setNames(String names) {}
  }
}
