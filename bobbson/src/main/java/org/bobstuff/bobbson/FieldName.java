package org.bobstuff.bobbson;

public interface FieldName {
  int weakHash();

  String name();

  boolean equalsArray(byte[] array);

  boolean equalsArray(byte[] array, int weakHash);
}
