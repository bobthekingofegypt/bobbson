package org.bobstuff.bobbson.processor;

public class ClasspathUtils {
  public static String findConverterName(StructInfo structInfo) {
    int dotIndex = structInfo.binaryName.lastIndexOf('.');
    String className = structInfo.binaryName.substring(dotIndex + 1);
    if (dotIndex == -1) {
      return String.format("_%s_BobBsonConverterRegister", className);
    }
    String packageName = structInfo.binaryName.substring(0, dotIndex);
    // TODO understand how sealed classes effect loading
    return String.format("%s._%s_BobBsonConverterRegister", packageName, className);
  }
}
