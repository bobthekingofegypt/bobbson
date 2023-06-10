package org.bobstuff.bobbson.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.karuslabs.elementary.junit.Cases;
import com.karuslabs.elementary.junit.Tools;
import com.karuslabs.elementary.junit.ToolsExtension;
import com.karuslabs.elementary.junit.annotations.Case;
import com.karuslabs.elementary.junit.annotations.Introspect;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import org.bobstuff.bobbson.annotations.GenerateBobBsonConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ToolsExtension.class)
@Introspect
public class StructInfoTest {
  Element sampleMultipleFields;
  StructInfo sampleMultipleFieldsStructInfo;

  @BeforeEach
  public void setUp(Cases cases) {
    sampleMultipleFields = cases.one("multipleFields");
    var analysis =
        new Analysis(Tools.types(), Tools.elements(), new BobMessager(Tools.messager(), false));
    var result = analysis.analyse(new HashSet<>(List.of(sampleMultipleFields)));
    sampleMultipleFieldsStructInfo =
        result.get("org.bobstuff.bobbson.processor.StructInfoTest.SampleMultipleFields");
  }

  @Test
  public void testBooleanLogicAttributes() {
    assertEquals(
        "mapEmbeddedCheck && occupationCheck && aliasesCheck && setDoublesCheck && nameCheck &&"
            + " embeddedCheck && ageCheck",
        sampleMultipleFieldsStructInfo.getAttributeReadAllBooleanLogic());
  }

  public static class TestEmbedded {}

  @Case("multipleFields")
  @GenerateBobBsonConverter
  static class SampleMultipleFields {
    private String name;
    private int age;
    private String occupation;
    private List<String> aliases;
    private Set<Double> setDoubles;
    private Map<String, TestEmbedded> mapEmbedded;
    private List<TestEmbedded> embedded;

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

    public List<String> getAliases() {
      return aliases;
    }

    public void setAliases(List<String> aliases) {
      this.aliases = aliases;
    }

    public List<TestEmbedded> getEmbedded() {
      return embedded;
    }

    public void setEmbedded(List<TestEmbedded> embedded) {
      this.embedded = embedded;
    }

    public Set<Double> getSetDoubles() {
      return setDoubles;
    }

    public void setSetDoubles(Set<Double> setDoubles) {
      this.setDoubles = setDoubles;
    }

    public Map<String, TestEmbedded> getMapEmbedded() {
      return mapEmbedded;
    }

    public void setMapEmbedded(Map<String, TestEmbedded> mapEmbedded) {
      this.mapEmbedded = mapEmbedded;
    }
  }
}
