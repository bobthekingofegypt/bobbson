package org.bobstuff.bobbson.processor;

import java.nio.charset.StandardCharsets;

public class HashUtils {
  public static int generateHash(String value) {
    int hash = 0;
    var bytes = value.getBytes(StandardCharsets.UTF_8);
    for (byte b : bytes) {
      hash += b;
    }
    return hash;
  }
}
