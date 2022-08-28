package org.bobstuff.bobbson;

public interface FieldName {
  String name();

  boolean equalsArray(byte[] array);

  boolean equalsArray(byte[] array, int weakHash);
}
