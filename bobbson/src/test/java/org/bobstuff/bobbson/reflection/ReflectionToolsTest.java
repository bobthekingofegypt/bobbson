package org.bobstuff.bobbson.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ReflectionToolsTest {

  @Test
  public void testParseBeanFieldsIgnoresNoSetters() throws Exception {
    var result = ReflectionTools.parseBeanFields(NoSetters.class);
    assertEquals(0, result.size());
  }

  @Test
  public void testParseBeanFieldsOneSetter() throws Exception {
    var result = ReflectionTools.parseBeanFields(OneSetter.class);
    assertEquals(1, result.size());
    var field = result.get(0);
    assertEquals("name", field.getName());
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
}
