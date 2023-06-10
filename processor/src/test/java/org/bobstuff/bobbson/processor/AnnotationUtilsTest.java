package org.bobstuff.bobbson.processor;

import com.karuslabs.elementary.junit.Cases;
import com.karuslabs.elementary.junit.Tools;
import com.karuslabs.elementary.junit.ToolsExtension;
import com.karuslabs.elementary.junit.annotations.Case;
import com.karuslabs.elementary.junit.annotations.Introspect;
import org.bobstuff.bobbson.annotations.GenerateBobBsonConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ToolsExtension.class)
@Introspect
public class AnnotationUtilsTest {
  @Test
  void find_annotation_exists(Cases cases) {
    var compileElement = Tools.elements().getTypeElement(GenerateBobBsonConverter.class.getName());
    var compileType = Tools.types().getDeclaredType(compileElement);
    var first = cases.one("first");
    var am = AnnotationUtils.findAnnotationMirror(first, compileType, Tools.types());
    Assertions.assertNotNull(am);
  }

  @Test
  void find_annotation_doesnt_exist(Cases cases) {
    var compileElement = Tools.elements().getTypeElement(GenerateBobBsonConverter.class.getName());
    var compileType = Tools.types().getDeclaredType(compileElement);
    var sut = cases.one("second");
    var am = AnnotationUtils.findAnnotationMirror(sut, compileType, Tools.types());
    Assertions.assertNull(am);
  }

  @Case("first")
  @GenerateBobBsonConverter
  static class Sample {}

  @Case("second")
  static class SampleNoAnnotation {}
}
