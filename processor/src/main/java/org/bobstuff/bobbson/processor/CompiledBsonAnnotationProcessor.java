package org.bobstuff.bobbson.processor;

import java.io.Writer;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import org.bobstuff.bobbson.annotations.GenerateBobBsonConverter;

@SupportedAnnotationTypes({
  "org.bobstuff.bobbson.annotations.GenerateBobBsonConverter",
})
@SuppressWarnings("initialization")
public class CompiledBsonAnnotationProcessor extends AbstractProcessor {
  private BobMessager messager;
  private Types types;
  private Elements elements;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    messager = new BobMessager(processingEnv.getMessager(), true);
    types = processingEnv.getTypeUtils();
    elements = processingEnv.getElementUtils();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver() || annotations.isEmpty()) {
      return false;
    }
    messager.debug("running the processor for a round");

    Set<? extends Element> compiledBsonInstances =
        roundEnv.getElementsAnnotatedWith(GenerateBobBsonConverter.class);
    messager.debug("found " + compiledBsonInstances.size() + " items marked as BsonCompiler");
    if (compiledBsonInstances.isEmpty()) {
      return false;
    }

    for (var type : compiledBsonInstances) {
      messager.debug(" -- instance type " + type);
    }

    var analysis = new Analysis(types, elements, messager);
    var structs = analysis.analyse(compiledBsonInstances);

    for (StructInfo structInfo : structs.values()) {
      String classNamePath = ClasspathUtils.findConverterName(structInfo);
      try {
        JavaFileObject converterFile =
            processingEnv.getFiler().createSourceFile(classNamePath, structInfo.element);
        try (Writer writer = converterFile.openWriter()) {
          if (structInfo.isEnum()) {
            EnumGenerator enumGenerator = new EnumGenerator();
            messager.debug("struct " + structInfo.getClassName() + ", is an enum");
            enumGenerator.generate(structInfo, writer, types, elements);
          } else {
            ParserGenerator parserGenerator = new ParserGenerator();
            parserGenerator.generate(structInfo, writer, types, elements);
          }
        }
      } catch (Exception e) {
        messager.error("failed writing out file : " + e.getMessage());
        messager.error(e);
      }
    }

    return true;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
}
