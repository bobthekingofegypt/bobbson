package org.bobstuff.bobbson.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Type;
import org.bobstuff.bobbson.BobBson;
import org.bobstuff.bobbson.BobBsonConverter;
import org.bobstuff.bobbson.annotations.BsonAttribute;
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
}
