package org.bobstuff.bobbson.processor;

import com.karuslabs.elementary.junit.Cases;
import com.karuslabs.elementary.junit.Tools;
import com.karuslabs.elementary.junit.ToolsExtension;
import com.karuslabs.elementary.junit.annotations.Case;
import com.karuslabs.elementary.junit.annotations.Introspect;
import java.util.Collections;
import javax.lang.model.element.TypeElement;
import org.bobstuff.bobbson.annotations.CompiledBson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ToolsExtension.class)
@Introspect
public class ClasspathUtilsTest {
  @Test
  void find_annotation_doesnt_exist(Cases cases) {
    var compileElement = Tools.elements().getTypeElement(CompiledBson.class.getName());
    var compileType = Tools.types().getDeclaredType(compileElement);
    var sample = cases.one("first");
    var am = AnnotationUtils.findAnnotationMirror(sample, compileType, Tools.types());

    StructInfo structInfo =
        new StructInfo(
            (TypeElement) sample,
            compileType,
            "struct-1",
            Tools.elements().getBinaryName((TypeElement) sample).toString(),
            am,
            Collections.emptyMap());

    var result = ClasspathUtils.findConverterName(structInfo);

    Assertions.assertEquals(
        "org.bobstuff.bobbson.processor._ClasspathUtilsTest$Sample_BobBsonConverterRegister",
        result);
  }

  @Case("first")
  @CompiledBson
  static class Sample {}
}
